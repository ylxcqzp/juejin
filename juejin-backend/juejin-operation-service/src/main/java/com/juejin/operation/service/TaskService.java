package com.juejin.operation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.juejin.operation.entity.Task;
import com.juejin.operation.vo.TaskVO;

import java.util.List;

/**
 * 任务Service接口
 *
 * @author juejin
 */
public interface TaskService extends IService<Task> {

    /**
     * 获取用户所有任务（含进度和完成状态）
     *
     * @param userId 用户ID
     * @return 任务列表（含用户进度）
     */
    List<TaskVO> getUserTasks(Long userId);

    /**
     * 按类型获取任务
     *
     * @param userId 用户ID
     * @param type   任务类型：newbie/daily
     * @return 任务列表
     */
    List<TaskVO> getTasksByType(Long userId, String type);

    /**
     * 更新任务进度（由其他服务调用）
     *
     * @param userId   用户ID
     * @param taskCode 任务代码
     * @param delta    进度增量
     */
    void updateProgress(Long userId, String taskCode, int delta);

    /**
     * 领取任务奖励
     *
     * @param userId 用户ID
     * @param taskId 任务ID
     * @return 领取结果
     */
    TaskVO claimReward(Long userId, Long taskId);

}
