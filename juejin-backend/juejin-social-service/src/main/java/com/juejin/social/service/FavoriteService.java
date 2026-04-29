package com.juejin.social.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.juejin.common.vo.PageResult;
import com.juejin.social.dto.FavoriteFolderDTO;
import com.juejin.social.dto.FavoriteRecordDTO;
import com.juejin.social.entity.FavoriteFolder;
import com.juejin.social.entity.FavoriteRecord;
import com.juejin.social.vo.FavoriteFolderVO;
import com.juejin.social.vo.FavoriteRecordVO;

/**
 * 收藏Service接口（收藏夹管理 + 收藏操作）
 *
 * @author juejin
 */
public interface FavoriteService extends IService<FavoriteFolder> {

    // ==================== 收藏夹管理 ====================

    /**
     * 获取用户的所有收藏夹
     *
     * @param userId 用户ID
     * @return 收藏夹列表
     */
    java.util.List<FavoriteFolderVO> getFolders(Long userId);

    /**
     * 创建收藏夹
     *
     * @param userId 用户ID
     * @param dto    收藏夹信息
     * @return 创建的收藏夹
     */
    FavoriteFolderVO createFolder(Long userId, FavoriteFolderDTO dto);

    /**
     * 更新收藏夹
     *
     * @param userId   用户ID
     * @param folderId 收藏夹ID
     * @param dto      更新信息
     * @return 更新后的收藏夹
     */
    FavoriteFolderVO updateFolder(Long userId, Long folderId, FavoriteFolderDTO dto);

    /**
     * 删除收藏夹（不能删除默认收藏夹）
     *
     * @param userId   用户ID
     * @param folderId 收藏夹ID
     * @return 是否成功
     */
    boolean deleteFolder(Long userId, Long folderId);

    // ==================== 收藏操作 ====================

    /**
     * 收藏文章
     *
     * @param userId 用户ID
     * @param dto    收藏信息
     * @return 收藏记录
     */
    FavoriteRecordVO addFavorite(Long userId, FavoriteRecordDTO dto);

    /**
     * 取消收藏
     *
     * @param userId    用户ID
     * @param articleId 文章ID
     * @return 是否成功
     */
    boolean removeFavorite(Long userId, Long articleId);

    /**
     * 分页查询收藏夹中的文章
     *
     * @param folderId 收藏夹ID
     * @param page     页码
     * @param size     每页数量
     * @return 收藏记录列表
     */
    PageResult<FavoriteRecordVO> getFavoritesByFolder(Long folderId, Integer page, Integer size);

    /**
     * 检查用户是否已收藏某文章
     *
     * @param userId    用户ID
     * @param articleId 文章ID
     * @return 是否已收藏
     */
    boolean isFavorited(Long userId, Long articleId);

}
