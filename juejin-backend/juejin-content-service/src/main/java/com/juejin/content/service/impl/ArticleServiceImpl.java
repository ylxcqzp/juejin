package com.juejin.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.juejin.common.constants.RedisKey;
import com.juejin.common.exception.BusinessException;
import com.juejin.common.feign.UserFeignClient;
import com.juejin.common.feign.UserInfoVO;
import com.juejin.common.result.ErrorCode;
import com.juejin.common.result.Result;
import com.juejin.common.vo.PageResult;
import com.juejin.content.dto.ArticleCreateDTO;
import com.juejin.content.dto.ArticleUpdateDTO;
import com.juejin.content.entity.Article;
import com.juejin.content.entity.ArticleTag;
import com.juejin.content.entity.ArticleVersion;
import com.juejin.content.entity.AuditRecord;
import com.juejin.content.entity.Category;
import com.juejin.content.entity.Tag;
import com.juejin.content.mapper.ArticleMapper;
import com.juejin.content.mapper.ArticleTagMapper;
import com.juejin.content.mapper.ArticleVersionMapper;
import com.juejin.content.mapper.AuditRecordMapper;
import com.juejin.content.mapper.CategoryMapper;
import com.juejin.content.mapper.TagMapper;
import com.juejin.content.service.ArticleService;
import com.juejin.content.vo.ArticleVO;
import com.juejin.content.vo.ArticleVersionVO;
import com.juejin.content.vo.TagVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 文章Service实现类
 *
 * @author juejin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    private final ArticleMapper articleMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final ArticleTagMapper articleTagMapper;
    private final ArticleVersionMapper articleVersionMapper;
    private final AuditRecordMapper auditRecordMapper;
    private final UserFeignClient userFeignClient;
    private final RedisTemplate<String, Object> redisTemplate;

    /** 标签最多允许选择的数量 */
    private static final int MAX_TAG_COUNT = 5;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleVO createArticle(ArticleCreateDTO dto, Long authorId) {
        // 检查分类是否存在
        Category category = categoryMapper.selectById(dto.getCategoryId());
        if (category == null) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        // 校验标签数量
        if (dto.getTagIds() != null && dto.getTagIds().size() > MAX_TAG_COUNT) {
            throw new BusinessException(ErrorCode.TAG_LIMIT_EXCEEDED);
        }

        // 创建文章
        Article article = new Article();
        article.setAuthorId(authorId);
        article.setTitle(dto.getTitle());
        article.setContent(dto.getContent());
        article.setSummary(generateSummary(dto.getContent()));
        article.setCoverImage(dto.getCoverImage());
        article.setCategoryId(dto.getCategoryId());
        // 文章状态：提交审核（而非直接发布）
        article.setStatus(1);
        article.setPublishTime(null);
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setCommentCount(0);
        article.setFavoriteCount(0);
        article.setShareCount(0);
        article.setIsTop(0);
        article.setIsEssence(0);
        article.setIsOriginal(dto.getIsOriginal() != null ? dto.getIsOriginal() : 1);
        article.setSourceUrl(dto.getSourceUrl());
        article.setCopyright(1);
        article.setVersion(1);

        save(article);

        // 保存初始版本快照
        saveArticleVersion(article);

        // 处理标签关联
        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            saveArticleTags(article.getId(), dto.getTagIds());
        }

        // 创建审核记录（机器审核）
        createAuditRecord(article.getId(), "article", null, 1, 1, null);

        log.info("Article created and submitted for review: {}, author: {}", article.getId(), authorId);
        return convertToVO(article);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleVO updateArticle(ArticleUpdateDTO dto, Long authorId) {
        Article article = getById(dto.getId());
        if (article == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }

        // 检查权限
        if (!article.getAuthorId().equals(authorId)) {
            throw new BusinessException(ErrorCode.NO_EDIT_PERMISSION);
        }

        // 校验标签数量
        if (dto.getTagIds() != null && dto.getTagIds().size() > MAX_TAG_COUNT) {
            throw new BusinessException(ErrorCode.TAG_LIMIT_EXCEEDED);
        }

        // 更新文章
        article.setTitle(dto.getTitle());
        article.setContent(dto.getContent());
        article.setSummary(generateSummary(dto.getContent()));
        article.setCoverImage(dto.getCoverImage());
        article.setCategoryId(dto.getCategoryId());
        article.setIsOriginal(dto.getIsOriginal());
        article.setSourceUrl(dto.getSourceUrl());
        // 已发布文章编辑后重新进入审核
        boolean wasPublished = article.getStatus() == 2;
        article.setVersion(article.getVersion() + 1);
        article.setStatus(1);
        article.setRejectReason(null);

        updateById(article);

        // 保存新版本快照
        saveArticleVersion(article);

        // 处理标签关联
        if (dto.getTagIds() != null) {
            deleteArticleTags(article.getId());
            if (!dto.getTagIds().isEmpty()) {
                saveArticleTags(article.getId(), dto.getTagIds());
            }
        }

        // 如果之前已发布，更新分类计数（重新审核期间暂时减少）
        if (wasPublished) {
            categoryMapper.decrementArticleCount(article.getCategoryId());
        }

        // 清除缓存
        redisTemplate.delete(RedisKey.ARTICLE_DETAIL + dto.getId());

        log.info("Article updated and re-submitted for review: {}, author: {}", article.getId(), authorId);

        return convertToVO(article);
    }

    @Override
    public ArticleVO getArticleById(Long id) {
        // 先从缓存获取
        String cacheKey = RedisKey.ARTICLE_DETAIL + id;
        ArticleVO cached = (ArticleVO) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        Article article = getById(id);
        if (article == null || article.getStatus() != 2) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }

        // 增加浏览量
        articleMapper.incrementViewCount(id);
        article.setViewCount(article.getViewCount() + 1);

        ArticleVO vo = convertToVO(article);

        // 缓存文章
        redisTemplate.opsForValue().set(cacheKey, vo, 10, TimeUnit.MINUTES);

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteArticle(Long id, Long authorId) {
        Article article = getById(id);
        if (article == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }

        // 检查权限
        if (!article.getAuthorId().equals(authorId)) {
            throw new BusinessException(ErrorCode.NO_EDIT_PERMISSION);
        }

        // 逻辑删除
        boolean result = removeById(id);

        // 更新分类文章数
        categoryMapper.decrementArticleCount(article.getCategoryId());

        // 删除标签关联并减少标签文章计数
        deleteArticleTags(id);

        // 清除缓存
        redisTemplate.delete(RedisKey.ARTICLE_DETAIL + id);

        log.info("Article deleted: {}, author: {}", id, authorId);

        return result;
    }

    @Override
    public PageResult<ArticleVO> getArticleList(Integer page, Integer size, Long categoryId) {
        Page<Article> pageParam = new Page<>(page, size);

        lambdaQuery()
                .eq(categoryId != null, Article::getCategoryId, categoryId)
                .eq(Article::getStatus, 2)
                .orderByDesc(Article::getIsTop)
                .orderByDesc(Article::getPublishTime)
                .page(pageParam);

        List<ArticleVO> records = pageParam.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(records, pageParam.getTotal(), (int) pageParam.getCurrent(), (int) pageParam.getSize());
    }

    @Override
    public PageResult<ArticleVO> getUserArticles(Long userId, Integer page, Integer size) {
        Page<Article> pageParam = new Page<>(page, size);

        lambdaQuery()
                .eq(Article::getAuthorId, userId)
                .orderByDesc(Article::getPublishTime)
                .page(pageParam);

        List<ArticleVO> records = pageParam.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(records, pageParam.getTotal(), (int) pageParam.getCurrent(), (int) pageParam.getSize());
    }

    @Override
    public List<ArticleVO> getHotArticles(Integer limit) {
        List<Article> articles = articleMapper.selectHotArticles(limit);
        return articles.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    // ==================== 内部辅助方法 ====================

    /**
     * 保存文章标签关联，并更新标签文章计数
     *
     * @param articleId 文章ID
     * @param tagIds    标签ID列表
     */
    private void saveArticleTags(Long articleId, List<Long> tagIds) {
        for (Long tagId : tagIds) {
            // 校验标签是否存在
            Tag tag = tagMapper.selectById(tagId);
            if (tag == null) {
                throw new BusinessException(ErrorCode.TAG_NOT_FOUND);
            }
            // 创建关联记录
            ArticleTag articleTag = new ArticleTag();
            articleTag.setArticleId(articleId);
            articleTag.setTagId(tagId);
            articleTagMapper.insert(articleTag);
            // 更新标签文章计数
            tagMapper.incrementArticleCount(tagId);
        }
    }

    /**
     * 删除文章的标签关联，并减少标签文章计数
     *
     * @param articleId 文章ID
     */
    private void deleteArticleTags(Long articleId) {
        // 查询当前文章关联的所有标签ID
        List<Long> existingTagIds = articleTagMapper.selectTagIdsByArticleId(articleId);
        // 逻辑删除关联记录
        LambdaQueryWrapper<ArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleTag::getArticleId, articleId);
        articleTagMapper.delete(wrapper);
        // 减少对应标签的文章计数
        for (Long tagId : existingTagIds) {
            tagMapper.decrementArticleCount(tagId);
        }
    }

    /**
     * 生成摘要：去除Markdown标记，取前200字符
     *
     * @param content 文章内容
     * @return 摘要文本
     */
    private String generateSummary(String content) {
        if (!StringUtils.hasText(content)) {
            return "";
        }
        // 去除常用 Markdown 语法标记
        String plainText = content.replaceAll("[#*`(){}\\[\\]]", "").trim();
        if (plainText.length() > 200) {
            return plainText.substring(0, 200) + "...";
        }
        return plainText;
    }

    /**
     * 将文章实体转换为VO，填充分类名称、标签列表和作者信息
     *
     * @param article 文章实体
     * @return 文章VO
     */
    private ArticleVO convertToVO(Article article) {
        ArticleVO vo = new ArticleVO();
        BeanUtils.copyProperties(article, vo);

        // 填充分类名称
        if (article.getCategoryId() != null) {
            Category category = categoryMapper.selectById(article.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }

        // 填充标签列表：从 article_tag 关联表查询标签ID，再查标签详情
        List<Long> tagIds = articleTagMapper.selectTagIdsByArticleId(article.getId());
        if (tagIds != null && !tagIds.isEmpty()) {
            List<Tag> tags = tagMapper.selectBatchIds(tagIds);
            List<TagVO> tagVOs = tags.stream().map(tag -> {
                TagVO tagVO = new TagVO();
                BeanUtils.copyProperties(tag, tagVO);
                return tagVO;
            }).collect(Collectors.toList());
            vo.setTags(tagVOs);
        } else {
            vo.setTags(new ArrayList<>());
        }

        // 填充作者昵称和头像：通过 Feign 调用用户服务
        fillAuthorInfo(vo, article.getAuthorId());

        return vo;
    }

    /**
     * 通过 Feign 客户端填充作者基本信息（昵称和头像）
     * 调用失败时静默降级，不影响主流程
     */
    private void fillAuthorInfo(ArticleVO vo, Long authorId) {
        try {
            Result<UserInfoVO> result = userFeignClient.getUserById(authorId);
            if (result != null && result.getData() != null) {
                UserInfoVO userInfo = result.getData();
                vo.setAuthorNickname(userInfo.getNickname());
                vo.setAuthorAvatar(userInfo.getAvatar());
            }
        } catch (Exception e) {
            log.debug("Failed to fetch author info for userId: {}, reason: {}", authorId, e.getMessage());
        }
    }

    // ==================== 审核相关方法 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitForReview(Long id, Long authorId) {
        Article article = getById(id);
        if (article == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }
        if (!article.getAuthorId().equals(authorId)) {
            throw new BusinessException(ErrorCode.NO_EDIT_PERMISSION);
        }
        // 只有草稿(0)或驳回(3)状态可以提交审核
        if (article.getStatus() != 0 && article.getStatus() != 3) {
            throw new BusinessException(ErrorCode.ALREADY_PUBLISHED);
        }

        article.setStatus(1);
        article.setRejectReason(null);
        updateById(article);

        // 保存版本快照
        saveArticleVersion(article);

        // 创建机器审核记录
        createAuditRecord(article.getId(), "article", null, 1, 1, null);

        log.info("Article submitted for review: {}, author: {}", id, authorId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approveArticle(Long id, Long auditorId) {
        Article article = getById(id);
        if (article == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }
        // 只有审核中(1)状态可以批准
        if (article.getStatus() != 1) {
            throw new BusinessException(ErrorCode.ARTICLE_UNPUBLISHED);
        }

        // 发布文章
        article.setStatus(2);
        article.setPublishTime(LocalDateTime.now());
        article.setVersion(article.getVersion() + 1);
        updateById(article);

        // 更新分类文章数
        categoryMapper.incrementArticleCount(article.getCategoryId());

        // 保存发布版本
        saveArticleVersion(article);

        // 创建审核记录（通过）
        createAuditRecord(article.getId(), "article", auditorId, 2, 1, "审核通过");

        // 清除缓存
        redisTemplate.delete(RedisKey.ARTICLE_DETAIL + id);

        log.info("Article approved: {}, auditor: {}", id, auditorId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rejectArticle(Long id, Long auditorId, String reason) {
        Article article = getById(id);
        if (article == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }
        if (article.getStatus() != 1) {
            throw new BusinessException(ErrorCode.ARTICLE_UNPUBLISHED);
        }

        article.setStatus(3);
        article.setRejectReason(reason);
        updateById(article);

        // 创建审核记录（驳回）
        createAuditRecord(article.getId(), "article", auditorId, 2, 2, reason);

        // 清除缓存
        redisTemplate.delete(RedisKey.ARTICLE_DETAIL + id);

        log.info("Article rejected: {}, auditor: {}, reason: {}", id, auditorId, reason);
        return true;
    }

    // ==================== 版本管理方法 ====================

    @Override
    public List<ArticleVersionVO> getArticleVersions(Long articleId) {
        List<ArticleVersion> versions = articleVersionMapper.selectByArticleId(articleId);
        return versions.stream().map(v -> {
            ArticleVersionVO vo = new ArticleVersionVO();
            BeanUtils.copyProperties(v, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleVO rollbackToVersion(Long articleId, Long versionId, Long authorId) {
        Article article = getById(articleId);
        if (article == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }
        if (!article.getAuthorId().equals(authorId)) {
            throw new BusinessException(ErrorCode.NO_EDIT_PERMISSION);
        }

        // 查找目标版本
        ArticleVersion targetVersion = articleVersionMapper.selectById(versionId);
        if (targetVersion == null || !targetVersion.getArticleId().equals(articleId)) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }

        // 回滚内容
        article.setTitle(targetVersion.getTitle());
        article.setContent(targetVersion.getContent());
        article.setVersion(article.getVersion() + 1);
        article.setStatus(1); // 回滚后重新进入审核
        updateById(article);

        // 保存新版本（标记为回滚版本）
        saveArticleVersion(article);

        // 清除缓存
        redisTemplate.delete(RedisKey.ARTICLE_DETAIL + articleId);

        log.info("Article rolled back to version: articleId={}, versionId={}, toVersion={}",
                articleId, versionId, targetVersion.getVersion());
        return convertToVO(article);
    }

    @Override
    public PageResult<ArticleVO> searchArticles(String keyword, Integer page, Integer size) {
        // 使用 MySQL LIKE 进行全文搜索（初期方案，后续可迁移至 Elasticsearch）
        Page<Article> pageParam = new Page<>(page, size);
        lambdaQuery()
                .like(Article::getTitle, keyword)
                .or()
                .like(Article::getSummary, keyword)
                .eq(Article::getStatus, 2)
                .orderByDesc(Article::getPublishTime)
                .page(pageParam);

        List<ArticleVO> records = pageParam.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return PageResult.of(records, pageParam.getTotal(), (int) pageParam.getCurrent(), (int) pageParam.getSize());
    }

    // ==================== 内部辅助方法 ====================

    /**
     * 保存文章版本快照
     */
    private void saveArticleVersion(Article article) {
        ArticleVersion version = new ArticleVersion();
        version.setArticleId(article.getId());
        version.setVersion(article.getVersion());
        version.setTitle(article.getTitle());
        version.setContent(article.getContent());
        version.setContentHtml(article.getContentHtml());
        articleVersionMapper.insert(version);
    }

    /**
     * 创建审核记录
     *
     * @param targetId    目标ID
     * @param targetType  目标类型
     * @param auditorId   审核人ID（机器审核为null）
     * @param auditType   审核类型：1-机器 2-人工
     * @param auditResult 审核结果：1-通过 2-驳回
     * @param reason      审核原因
     */
    private void createAuditRecord(Long targetId, String targetType, Long auditorId,
                                   Integer auditType, Integer auditResult, String reason) {
        AuditRecord record = new AuditRecord();
        record.setTargetId(targetId);
        record.setTargetType(targetType);
        record.setAuditorId(auditorId);
        record.setAuditType(auditType);
        record.setAuditResult(auditResult);
        record.setAuditReason(reason);
        record.setRiskLevel(1);
        auditRecordMapper.insert(record);
    }

    // ==================== 草稿相关实现（合并到 article.status=0） ====================

    @Override
    public PageResult<ArticleVO> getUserDrafts(Long userId, Integer page, Integer size) {
        Page<Article> pageParam = new Page<>(page, size);
        lambdaQuery()
                .eq(Article::getAuthorId, userId)
                .eq(Article::getStatus, 0)
                .orderByDesc(Article::getUpdateTime)
                .page(pageParam);

        List<ArticleVO> records = pageParam.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return PageResult.of(records, pageParam.getTotal(), (int) pageParam.getCurrent(), (int) pageParam.getSize());
    }

    @Override
    public ArticleVO getDraftById(Long userId, Long draftId) {
        Article article = getById(draftId);
        if (article == null || !article.getAuthorId().equals(userId) || article.getStatus() != 0) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }
        return convertToVO(article);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleVO createDraft(ArticleCreateDTO dto, Long userId) {
        Article article = new Article();
        article.setAuthorId(userId);
        article.setTitle(dto.getTitle());
        article.setContent(dto.getContent());
        if (dto.getContent() != null) {
            article.setSummary(generateSummary(dto.getContent()));
        }
        article.setCoverImage(dto.getCoverImage());
        article.setCategoryId(dto.getCategoryId());
        article.setStatus(0); // 草稿
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setCommentCount(0);
        article.setFavoriteCount(0);
        article.setShareCount(0);
        article.setIsTop(0);
        article.setIsEssence(0);
        article.setIsOriginal(dto.getIsOriginal() != null ? dto.getIsOriginal() : 1);
        article.setSourceUrl(dto.getSourceUrl());
        article.setCopyright(1);
        article.setVersion(1);

        save(article);

        // 处理标签关联
        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            saveArticleTags(article.getId(), dto.getTagIds());
        }

        log.info("Draft created: userId={}, draftId={}", userId, article.getId());
        return convertToVO(article);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleVO updateDraft(Long draftId, ArticleUpdateDTO dto, Long userId) {
        Article article = getById(draftId);
        if (article == null || !article.getAuthorId().equals(userId) || article.getStatus() != 0) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }

        // 只更新提供的字段
        if (dto.getTitle() != null) article.setTitle(dto.getTitle());
        if (dto.getContent() != null) {
            article.setContent(dto.getContent());
            article.setSummary(generateSummary(dto.getContent()));
        }
        if (dto.getCoverImage() != null) article.setCoverImage(dto.getCoverImage());
        if (dto.getCategoryId() != null) article.setCategoryId(dto.getCategoryId());
        if (dto.getIsOriginal() != null) article.setIsOriginal(dto.getIsOriginal());
        if (dto.getSourceUrl() != null) article.setSourceUrl(dto.getSourceUrl());

        updateById(article);

        // 处理标签关联（全量替换）
        if (dto.getTagIds() != null) {
            deleteArticleTags(article.getId());
            if (!dto.getTagIds().isEmpty()) {
                saveArticleTags(article.getId(), dto.getTagIds());
            }
        }

        log.info("Draft updated: userId={}, draftId={}", userId, draftId);
        return convertToVO(article);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDraft(Long draftId, Long userId) {
        Article article = getById(draftId);
        if (article == null || !article.getAuthorId().equals(userId) || article.getStatus() != 0) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }

        removeById(draftId);

        // 删除标签关联
        deleteArticleTags(draftId);

        // 清除缓存
        redisTemplate.delete(RedisKey.ARTICLE_DETAIL + draftId);

        log.info("Draft deleted: userId={}, draftId={}", userId, draftId);
        return true;
    }

}
