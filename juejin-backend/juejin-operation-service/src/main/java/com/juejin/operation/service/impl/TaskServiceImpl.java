package com.juejin.operation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.juejin.common.exception.BusinessException;
import com.juejin.common.feign.UserFeignClient;
import com.juejin.common.result.ErrorCode;
import com.juejin.operation.entity.Task;
import com.juejin.operation.entity.UserTask;
import com.juejin.operation.mapper.TaskMapper;
import com.juejin.operation.mapper.UserTaskMapper;
import com.juejin.operation.service.TaskService;
import com.juejin.operation.vo.TaskVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 任务Service实现类
 *
 * @author juejin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task> implements TaskService {

    private final TaskMapper taskMapper;
    private final UserTaskMapper userTaskMapper;
    private final UserFeignClient userFeignClient;

    @Override
    public List<TaskVO> getUserTasks(Long userId) {
        List<Task> allTasks = taskMapper.selectAllEnabled();
        return buildTaskVOList(userId, allTasks);
    }

    @Override
    public List<TaskVO> getTasksByType(Long userId, String type) {
        List<Task> tasks = taskMapper.selectByType(type);
        return buildTaskVOList(userId, tasks);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProgress(Long userId, String taskCode, int delta) {
        // 查找对应任务
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getTaskCode, taskCode)
               .eq(Task::getStatus, 1);
        Task task = taskMapper.selectOne(wrapper);
        if (task == null) {
            return; // 任务不存在或已禁用，静默退出
        }

        LocalDate today = LocalDate.now();

        // 查找或创建用户任务记录
        UserTask userTask;
        if ("daily".equals(task.getType())) {
            userTask = findOrCreateDailyUserTask(userId, task.getId(), today);
        } else {
            userTask = findOrCreateUserTask(userId, task.getId());
        }

        // 已完成则不更新
        if (userTask.getIsCompleted() == 1) {
            return;
        }

        // 更新进度
        userTask.setProgress(userTask.getProgress() + delta);
        if (userTask.getProgress() >= task.getConditionValue()) {
            userTask.setProgress(task.getConditionValue());
            userTask.setIsCompleted(1);
            userTask.setCompleteTime(LocalDateTime.now());
            log.info("Task completed: userId={}, taskCode={}, taskId={}", userId, taskCode, task.getId());
        }

        if (userTask.getId() == null) {
            userTaskMapper.insert(userTask);
        } else {
            userTaskMapper.updateById(userTask);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TaskVO claimReward(Long userId, Long taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null || task.getStatus() != 1) {
            throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
        }

        LambdaQueryWrapper<UserTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserTask::getUserId, userId)
               .eq(UserTask::getTaskId, taskId);
        UserTask userTask = userTaskMapper.selectOne(wrapper);
        if (userTask == null || userTask.getIsCompleted() == 0) {
            throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
        }
        if (userTask.getIsClaimed() == 1) {
            throw new BusinessException(ErrorCode.TASK_ALREADY_COMPLETED);
        }

        // 领取奖励
        userTask.setIsClaimed(1);
        userTask.setClaimTime(LocalDateTime.now());
        userTaskMapper.updateById(userTask);

        // 通过 Feign 调用 user-service 增加掘力值
        try {
            userFeignClient.addPoints(userId, task.getPointsReward());
        } catch (Exception e) {
            log.error("Failed to add points via Feign: userId={}, points={}", userId, task.getPointsReward(), e);
        }

        log.info("Task reward claimed: userId={}, taskId={}, points={}", userId, taskId, task.getPointsReward());

        return buildSingleTaskVO(userId, task, userTask);
    }

    /**
     * 构建任务VO列表（含用户进度信息）
     */
    private List<TaskVO> buildTaskVOList(Long userId, List<Task> tasks) {
        return tasks.stream().map(task -> {
            UserTask userTask = findUserTaskRecord(userId, task.getId());
            return buildSingleTaskVO(userId, task, userTask);
        }).collect(Collectors.toList());
    }

    private TaskVO buildSingleTaskVO(Long userId, Task task, UserTask userTask) {
        TaskVO vo = new TaskVO();
        vo.setId(task.getId());
        vo.setName(task.getName());
        vo.setDescription(task.getDescription());
        vo.setType(task.getType());
        vo.setTaskCode(task.getTaskCode());
        vo.setConditionValue(task.getConditionValue());
        vo.setPointsReward(task.getPointsReward());
        vo.setBadgeId(task.getBadgeId());
        vo.setSortOrder(task.getSortOrder());
        if (userTask != null) {
            vo.setProgress(userTask.getProgress());
            vo.setIsCompleted(userTask.getIsCompleted() == 1);
            vo.setIsClaimed(userTask.getIsClaimed() == 1);
        } else {
            vo.setProgress(0);
            vo.setIsCompleted(false);
            vo.setIsClaimed(false);
        }
        return vo;
    }

    /**
     * 查找用户任务记录
     */
    private UserTask findUserTaskRecord(Long userId, Long taskId) {
        // 日常任务取今日记录
        LambdaQueryWrapper<UserTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserTask::getUserId, userId)
               .eq(UserTask::getTaskId, taskId);
        // 按日期降序，取第一条
        List<UserTask> records = userTaskMapper.selectList(wrapper
                .orderByDesc(UserTask::getTaskDate)
                .last("LIMIT 1"));
        return records.isEmpty() ? null : records.get(0);
    }

    /**
     * 查找或创建用户任务记录（非日常任务）
     */
    private UserTask findOrCreateUserTask(Long userId, Long taskId) {
        UserTask record = userTaskMapper.selectByUserIdAndTaskId(userId, taskId);
        if (record == null) {
            record = new UserTask();
            record.setUserId(userId);
            record.setTaskId(taskId);
            record.setProgress(0);
            record.setIsCompleted(0);
            record.setIsClaimed(0);
            record.setTaskDate(LocalDate.now());
        }
        return record;
    }

    /**
     * 查找或创建日常任务记录（当日）
     */
    private UserTask findOrCreateDailyUserTask(Long userId, Long taskId, LocalDate today) {
        List<UserTask> records = userTaskMapper.selectByUserIdAndDate(userId, today);
        UserTask record = records.stream()
                .filter(r -> r.getTaskId().equals(taskId))
                .findFirst()
                .orElse(null);
        if (record == null) {
            record = new UserTask();
            record.setUserId(userId);
            record.setTaskId(taskId);
            record.setProgress(0);
            record.setIsCompleted(0);
            record.setIsClaimed(0);
            record.setTaskDate(today);
        }
        return record;
    }

}
