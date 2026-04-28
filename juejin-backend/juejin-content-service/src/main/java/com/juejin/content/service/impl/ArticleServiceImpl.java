package com.juejin.content.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.juejin.common.exception.BusinessException;
import com.juejin.common.result.ErrorCode;
import com.juejin.common.vo.PageResult;
import com.juejin.content.dto.ArticleCreateDTO;
import com.juejin.content.dto.ArticleUpdateDTO;
import com.juejin.content.entity.Article;
import com.juejin.content.entity.Category;
import com.juejin.content.mapper.ArticleMapper;
import com.juejin.content.mapper.CategoryMapper;
import com.juejin.content.service.ArticleService;
import com.juejin.content.vo.ArticleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleVO createArticle(ArticleCreateDTO dto, Long authorId) {
        // 检查分类是否存在
        Category category = categoryMapper.selectById(dto.getCategoryId());
        if (category == null) {
            throw new BusinessException(ErrorCode.TAG_NOT_FOUND);
        }

        // 创建文章
        Article article = new Article();
        article.setAuthorId(authorId);
        article.setTitle(dto.getTitle());
        article.setContent(dto.getContent());
        article.setSummary(generateSummary(dto.getContent()));
        article.setCoverImage(dto.getCoverImage());
        article.setCategoryId(dto.getCategoryId());
        article.setStatus(2); // 直接发布
        article.setPublishTime(LocalDateTime.now());
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

        // 更新分类文章数
        categoryMapper.incrementArticleCount(dto.getCategoryId());

        log.info("Article created: {}, author: {}", article.getId(), authorId);

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

        // 更新文章
        article.setTitle(dto.getTitle());
        article.setContent(dto.getContent());
        article.setSummary(generateSummary(dto.getContent()));
        article.setCoverImage(dto.getCoverImage());
        article.setCategoryId(dto.getCategoryId());
        article.setIsOriginal(dto.getIsOriginal());
        article.setSourceUrl(dto.getSourceUrl());
        article.setVersion(article.getVersion() + 1);

        updateById(article);

        // 清除缓存
        redisTemplate.delete("article:" + dto.getId());

        log.info("Article updated: {}, author: {}", article.getId(), authorId);

        return convertToVO(article);
    }

    @Override
    public ArticleVO getArticleById(Long id) {
        // 先从缓存获取
        String cacheKey = "article:" + id;
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

        // 清除缓存
        redisTemplate.delete("article:" + id);

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

        return new PageResult<>(records, pageParam.getTotal(), pageParam.getCurrent(), pageParam.getSize());
    }

    @Override
    public PageResult<ArticleVO> getUserArticles(Long userId, Integer page, Integer size) {
        Page<Article> pageParam = new Page<>(page, size);

        lambdaQuery()
                .eq(Article::getAuthorId, userId)
                .eq(Article::getStatus, 2)
                .orderByDesc(Article::getPublishTime)
                .page(pageParam);

        List<ArticleVO> records = pageParam.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(records, pageParam.getTotal(), pageParam.getCurrent(), pageParam.getSize());
    }

    @Override
    public List<ArticleVO> getHotArticles(Integer limit) {
        List<Article> articles = articleMapper.selectHotArticles(limit);
        return articles.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 生成摘要
     */
    private String generateSummary(String content) {
        if (!StringUtils.hasText(content)) {
            return "";
        }
        // 去除Markdown标记，取前200字符
        String plainText = content.replaceAll("[#*`\\[\\]()]", "").trim();
        if (plainText.length() > 200) {
            return plainText.substring(0, 200) + "...";
        }
        return plainText;
    }

    /**
     * 转换为VO
     */
    private ArticleVO convertToVO(Article article) {
        ArticleVO vo = new ArticleVO();
        BeanUtils.copyProperties(article, vo);

        // 获取分类名称
        if (article.getCategoryId() != null) {
            Category category = categoryMapper.selectById(article.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }

        // 标签列表（简化处理）
        vo.setTags(new ArrayList<>());

        return vo;
    }

}
