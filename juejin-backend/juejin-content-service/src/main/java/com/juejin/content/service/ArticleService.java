package com.juejin.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.juejin.common.vo.PageResult;
import com.juejin.content.dto.ArticleCreateDTO;
import com.juejin.content.dto.ArticleUpdateDTO;
import com.juejin.content.entity.Article;
import com.juejin.content.vo.ArticleVO;

import java.util.List;

/**
 * 文章Service接口
 *
 * @author juejin
 */
public interface ArticleService extends IService<Article> {

    /**
     * 创建文章
     *
     * @param dto 文章创建DTO
     * @param authorId 作者ID
     * @return 文章VO
     */
    ArticleVO createArticle(ArticleCreateDTO dto, Long authorId);

    /**
     * 更新文章
     *
     * @param dto 文章更新DTO
     * @param authorId 作者ID
     * @return 文章VO
     */
    ArticleVO updateArticle(ArticleUpdateDTO dto, Long authorId);

    /**
     * 获取文章详情
     *
     * @param id 文章ID
     * @return 文章VO
     */
    ArticleVO getArticleById(Long id);

    /**
     * 删除文章
     *
     * @param id 文章ID
     * @param authorId 作者ID
     * @return 是否成功
     */
    boolean deleteArticle(Long id, Long authorId);

    /**
     * 分页查询文章列表
     *
     * @param page 页码
     * @param size 每页数量
     * @param categoryId 分类ID
     * @return 分页结果
     */
    PageResult<ArticleVO> getArticleList(Integer page, Integer size, Long categoryId);

    /**
     * 获取用户文章列表
     *
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 分页结果
     */
    PageResult<ArticleVO> getUserArticles(Long userId, Integer page, Integer size);

    /**
     * 获取热门文章
     *
     * @param limit 数量限制
     * @return 文章列表
     */
    List<ArticleVO> getHotArticles(Integer limit);

    /**
     * 提交审核
     *
     * @param id       文章ID
     * @param authorId 作者ID
     * @return 是否成功
     */
    boolean submitForReview(Long id, Long authorId);

    /**
     * 审核通过（管理员操作）
     *
     * @param id        文章ID
     * @param auditorId 审核人ID
     * @return 是否成功
     */
    boolean approveArticle(Long id, Long auditorId);

    /**
     * 审核驳回（管理员操作）
     *
     * @param id        文章ID
     * @param auditorId 审核人ID
     * @param reason    驳回原因
     * @return 是否成功
     */
    boolean rejectArticle(Long id, Long auditorId, String reason);

    /**
     * 获取文章历史版本列表
     *
     * @param articleId 文章ID
     * @return 版本列表
     */
    java.util.List<com.juejin.content.vo.ArticleVersionVO> getArticleVersions(Long articleId);

    /**
     * 回滚到指定版本
     *
     * @param articleId 文章ID
     * @param versionId 版本ID
     * @param authorId  作者ID
     * @return 回滚后的文章
     */
    ArticleVO rollbackToVersion(Long articleId, Long versionId, Long authorId);

    /**
     * 搜索文章（MySQL LIKE 方式，后续可迁移至 Elasticsearch）
     *
     * @param keyword 搜索关键词
     * @param page    页码
     * @param size    每页数量
     * @return 搜索结果
     */
    PageResult<ArticleVO> searchArticles(String keyword, Integer page, Integer size);

}
