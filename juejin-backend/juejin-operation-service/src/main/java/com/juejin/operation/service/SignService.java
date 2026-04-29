package com.juejin.operation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.juejin.operation.entity.SignRecord;
import com.juejin.operation.vo.SignVO;

/**
 * 签到Service接口
 *
 * @author juejin
 */
public interface SignService extends IService<SignRecord> {

    /**
     * 每日签到
     *
     * @param userId 用户ID
     * @return 签到结果
     */
    SignVO signIn(Long userId);

    /**
     * 查询签到状态
     *
     * @param userId 用户ID
     * @return 签到状态
     */
    SignVO getSignStatus(Long userId);

}
