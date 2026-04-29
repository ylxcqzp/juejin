package com.juejin.social.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.juejin.social.dto.LikeDTO;
import com.juejin.social.entity.LikeRecord;
import com.juejin.social.vo.LikeVO;

/**
 * 点赞Service接口
 *
 * @author juejin
 */
public interface LikeService extends IService<LikeRecord> {

    /**
     * 点赞/取消点赞（切换操作）
     *
     * @param userId 用户ID
     * @param dto    点赞信息
     * @return 当前点赞状态
     */
    LikeVO toggleLike(Long userId, LikeDTO dto);

    /**
     * 查询点赞列表
     *
     * @param targetId   目标ID
     * @param targetType 目标类型
     * @param page       页码
     * @param size       每页数量
     * @return 点赞用户列表
     */
    java.util.List<LikeVO> getLikeList(Long targetId, Integer targetType, Integer page, Integer size);

    /**
     * 检查是否已点赞
     *
     * @param userId     用户ID
     * @param targetId   目标ID
     * @param targetType 目标类型
     * @return 是否已点赞
     */
    boolean isLiked(Long userId, Long targetId, Integer targetType);

}
