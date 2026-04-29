package com.juejin.social.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.juejin.common.exception.BusinessException;
import com.juejin.common.feign.ContentFeignClient;
import com.juejin.common.result.ErrorCode;
import com.juejin.common.vo.PageResult;
import com.juejin.social.dto.FavoriteFolderDTO;
import com.juejin.social.dto.FavoriteRecordDTO;
import com.juejin.social.entity.FavoriteFolder;
import com.juejin.social.entity.FavoriteRecord;
import com.juejin.social.mapper.FavoriteFolderMapper;
import com.juejin.social.mapper.FavoriteRecordMapper;
import com.juejin.social.service.FavoriteService;
import com.juejin.social.vo.FavoriteFolderVO;
import com.juejin.social.vo.FavoriteRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 收藏Service实现类
 *
 * @author juejin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl extends ServiceImpl<FavoriteFolderMapper, FavoriteFolder> implements FavoriteService {

    private final FavoriteFolderMapper favoriteFolderMapper;
    private final FavoriteRecordMapper favoriteRecordMapper;
    private final ContentFeignClient contentFeignClient;

    private static final int MAX_FOLDER_COUNT = 20;

    @Override
    public List<FavoriteFolderVO> getFolders(Long userId) {
        // 确保默认收藏夹存在
        ensureDefaultFolder(userId);

        LambdaQueryWrapper<FavoriteFolder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FavoriteFolder::getUserId, userId)
               .orderByAsc(FavoriteFolder::getIsDefault)
               .orderByAsc(FavoriteFolder::getSortOrder);
        List<FavoriteFolder> folders = favoriteFolderMapper.selectList(wrapper);
        return folders.stream().map(this::convertFolderToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FavoriteFolderVO createFolder(Long userId, FavoriteFolderDTO dto) {
        // 检查收藏夹数量上限
        LambdaQueryWrapper<FavoriteFolder> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(FavoriteFolder::getUserId, userId);
        if (favoriteFolderMapper.selectCount(countWrapper) >= MAX_FOLDER_COUNT) {
            throw new BusinessException(ErrorCode.FOLDER_LIMIT_EXCEEDED);
        }

        // 检查名称重复
        LambdaQueryWrapper<FavoriteFolder> nameWrapper = new LambdaQueryWrapper<>();
        nameWrapper.eq(FavoriteFolder::getUserId, userId)
                   .eq(FavoriteFolder::getName, dto.getName());
        if (favoriteFolderMapper.selectCount(nameWrapper) > 0) {
            throw new BusinessException(ErrorCode.FOLDER_ALREADY_EXISTS);
        }

        FavoriteFolder folder = new FavoriteFolder();
        folder.setUserId(userId);
        folder.setName(dto.getName());
        folder.setDescription(dto.getDescription());
        folder.setCoverImage(dto.getCoverImage());
        folder.setIsPublic(dto.getIsPublic() != null ? dto.getIsPublic() : 1);
        folder.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        folder.setArticleCount(0);
        folder.setIsDefault(0);

        save(folder);
        log.info("Favorite folder created: userId={}, folder={}", userId, folder.getName());

        return convertFolderToVO(folder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FavoriteFolderVO updateFolder(Long userId, Long folderId, FavoriteFolderDTO dto) {
        FavoriteFolder folder = favoriteFolderMapper.selectById(folderId);
        if (folder == null || !folder.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FOLDER_NOT_FOUND);
        }

        // 检查名称重复（排除自身）
        if (dto.getName() != null) {
            LambdaQueryWrapper<FavoriteFolder> nameWrapper = new LambdaQueryWrapper<>();
            nameWrapper.eq(FavoriteFolder::getUserId, userId)
                       .eq(FavoriteFolder::getName, dto.getName())
                       .ne(FavoriteFolder::getId, folderId);
            if (favoriteFolderMapper.selectCount(nameWrapper) > 0) {
                throw new BusinessException(ErrorCode.FOLDER_ALREADY_EXISTS);
            }
            folder.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            folder.setDescription(dto.getDescription());
        }
        if (dto.getCoverImage() != null) {
            folder.setCoverImage(dto.getCoverImage());
        }
        if (dto.getIsPublic() != null) {
            folder.setIsPublic(dto.getIsPublic());
        }
        if (dto.getSortOrder() != null) {
            folder.setSortOrder(dto.getSortOrder());
        }

        updateById(folder);
        return convertFolderToVO(folder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFolder(Long userId, Long folderId) {
        FavoriteFolder folder = favoriteFolderMapper.selectById(folderId);
        if (folder == null || !folder.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FOLDER_NOT_FOUND);
        }
        if (folder.getIsDefault() == 1) {
            throw new BusinessException(ErrorCode.CANNOT_DELETE_DEFAULT_FOLDER);
        }

        // 删除收藏夹，同时删除该收藏夹下的所有收藏记录
        LambdaQueryWrapper<FavoriteRecord> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.eq(FavoriteRecord::getFolderId, folderId);
        favoriteRecordMapper.delete(recordWrapper);

        removeById(folderId);
        log.info("Favorite folder deleted: userId={}, folder={}", userId, folder.getName());
        return true;
    }

    // ==================== 收藏操作 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FavoriteRecordVO addFavorite(Long userId, FavoriteRecordDTO dto) {
        // 检查是否已收藏
        LambdaQueryWrapper<FavoriteRecord> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(FavoriteRecord::getUserId, userId)
                    .eq(FavoriteRecord::getArticleId, dto.getArticleId());
        if (favoriteRecordMapper.selectCount(existWrapper) > 0) {
            throw new BusinessException(ErrorCode.ALREADY_FAVORITED);
        }

        // 确定收藏夹
        Long folderId = dto.getFolderId();
        if (folderId == null) {
            // 使用默认收藏夹
            FavoriteFolder defaultFolder = ensureDefaultFolder(userId);
            folderId = defaultFolder.getId();
        } else {
            // 验证收藏夹归属
            FavoriteFolder folder = favoriteFolderMapper.selectById(folderId);
            if (folder == null || !folder.getUserId().equals(userId)) {
                throw new BusinessException(ErrorCode.FOLDER_NOT_FOUND);
            }
        }

        // 创建收藏记录
        FavoriteRecord record = new FavoriteRecord();
        record.setUserId(userId);
        record.setArticleId(dto.getArticleId());
        record.setFolderId(folderId);
        favoriteRecordMapper.insert(record);

        // 更新收藏夹文章计数（使用 update 语句确保并发安全）
        FavoriteFolder folder = favoriteFolderMapper.selectById(folderId);
        folder.setArticleCount(folder.getArticleCount() + 1);
        favoriteFolderMapper.updateById(folder);

        log.info("Article favorited: userId={}, articleId={}, folderId={}", userId, dto.getArticleId(), folderId);

        // 跨服务同步：增加文章收藏数
        syncArticleFavoriteCount(dto.getArticleId(), true);

        FavoriteRecordVO vo = new FavoriteRecordVO();
        BeanUtils.copyProperties(record, vo);
        vo.setFolderName(folder.getName());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeFavorite(Long userId, Long articleId) {
        LambdaQueryWrapper<FavoriteRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FavoriteRecord::getUserId, userId)
               .eq(FavoriteRecord::getArticleId, articleId);
        FavoriteRecord record = favoriteRecordMapper.selectOne(wrapper);
        if (record == null) {
            throw new BusinessException(ErrorCode.NOT_FAVORITED);
        }

        favoriteRecordMapper.deleteById(record.getId());

        // 更新收藏夹文章计数
        FavoriteFolder folder = favoriteFolderMapper.selectById(record.getFolderId());
        if (folder != null && folder.getArticleCount() > 0) {
            folder.setArticleCount(folder.getArticleCount() - 1);
            favoriteFolderMapper.updateById(folder);
        }

        log.info("Article unfavorited: userId={}, articleId={}", userId, articleId);

        // 跨服务同步：减少文章收藏数
        syncArticleFavoriteCount(articleId, false);

        return true;
    }

    @Override
    public PageResult<FavoriteRecordVO> getFavoritesByFolder(Long folderId, Integer page, Integer size) {
        int offset = (page - 1) * size;
        // 查询总数
        long total = favoriteRecordMapper.countByFolderId(folderId);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), total, page, size);
        }

        // 查询收藏夹名称
        FavoriteFolder folder = favoriteFolderMapper.selectById(folderId);
        String folderName = folder != null ? folder.getName() : "";

        // 分页查询文章ID列表
        List<Long> articleIds = favoriteRecordMapper.selectArticleIdsByFolder(folderId, offset, size);
        List<FavoriteRecordVO> records = articleIds.stream().map(aid -> {
            FavoriteRecordVO vo = new FavoriteRecordVO();
            vo.setArticleId(aid);
            vo.setFolderId(folderId);
            vo.setFolderName(folderName);
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(records, total, page, size);
    }

    @Override
    public boolean isFavorited(Long userId, Long articleId) {
        LambdaQueryWrapper<FavoriteRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FavoriteRecord::getUserId, userId)
               .eq(FavoriteRecord::getArticleId, articleId);
        return favoriteRecordMapper.selectCount(wrapper) > 0;
    }

    // ==================== 内部方法 ====================

    /**
     * 确保用户有默认收藏夹，没有则创建
     */
    private FavoriteFolder ensureDefaultFolder(Long userId) {
        LambdaQueryWrapper<FavoriteFolder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FavoriteFolder::getUserId, userId)
               .eq(FavoriteFolder::getIsDefault, 1);
        FavoriteFolder defaultFolder = favoriteFolderMapper.selectOne(wrapper);
        if (defaultFolder == null) {
            defaultFolder = new FavoriteFolder();
            defaultFolder.setUserId(userId);
            defaultFolder.setName("默认收藏夹");
            defaultFolder.setIsDefault(1);
            defaultFolder.setIsPublic(1);
            defaultFolder.setSortOrder(0);
            defaultFolder.setArticleCount(0);
            save(defaultFolder);
            log.info("Default favorite folder created for userId={}", userId);
        }
        return defaultFolder;
    }

    /**
     * 跨服务同步文章收藏数（异步调用，失败不影响主流程）
     */
    private void syncArticleFavoriteCount(Long articleId, boolean isIncrement) {
        try {
            if (isIncrement) {
                contentFeignClient.incrementFavoriteCount(articleId);
            } else {
                contentFeignClient.decrementFavoriteCount(articleId);
            }
        } catch (Exception e) {
            log.warn("Failed to sync article favorite count: articleId={}, error={}", articleId, e.getMessage());
        }
    }

    /**
     * 收藏夹实体转VO
     */
    private FavoriteFolderVO convertFolderToVO(FavoriteFolder folder) {
        FavoriteFolderVO vo = new FavoriteFolderVO();
        BeanUtils.copyProperties(folder, vo);
        return vo;
    }

}
