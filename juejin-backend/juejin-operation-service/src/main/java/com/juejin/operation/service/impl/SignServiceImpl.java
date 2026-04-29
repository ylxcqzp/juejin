package com.juejin.operation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.juejin.common.exception.BusinessException;
import com.juejin.common.result.ErrorCode;
import com.juejin.operation.entity.SignRecord;
import com.juejin.operation.mapper.SignRecordMapper;
import com.juejin.operation.service.SignService;
import com.juejin.operation.vo.SignVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 签到Service实现类
 *
 * @author juejin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SignServiceImpl extends ServiceImpl<SignRecordMapper, SignRecord> implements SignService {

    private final SignRecordMapper signRecordMapper;
    private final RedissonClient redissonClient;

    /** 基础签到奖励（掘力值） */
    private static final int BASE_SIGN_POINTS = 1;

    /** 连续签到 7 天额外奖励 */
    private static final int BONUS_7_DAYS = 5;

    /** 连续签到 30 天额外奖励 */
    private static final int BONUS_30_DAYS = 20;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SignVO signIn(Long userId) {
        LocalDate today = LocalDate.now();

        // 使用分布式锁防止重复签到
        RLock lock = redissonClient.getLock("sign:lock:" + userId);
        try {
            if (!lock.tryLock(5, 30, TimeUnit.SECONDS)) {
                throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS);
            }

            // 检查今天是否已签到
            SignRecord todayRecord = signRecordMapper.selectByUserIdAndDate(userId, today);
            if (todayRecord != null) {
                throw new BusinessException(ErrorCode.ALREADY_SIGNED);
            }

            // 计算连续签到天数：查询最近记录
            List<SignRecord> recentRecords = signRecordMapper.selectRecentByUserId(userId, 31);
            int continuousDays = calculateContinuousDays(recentRecords);

            // 计算签到奖励
            int pointsEarned = BASE_SIGN_POINTS;
            String bonusDesc = null;

            if (continuousDays > 0 && continuousDays % 30 == 0) {
                pointsEarned += BONUS_30_DAYS;
                bonusDesc = "连续签到30天，额外+" + BONUS_30_DAYS;
            } else if (continuousDays > 0 && continuousDays % 7 == 0) {
                pointsEarned += BONUS_7_DAYS;
                bonusDesc = "连续签到7天，额外+" + BONUS_7_DAYS;
            }

            // 创建签到记录
            SignRecord record = new SignRecord();
            record.setUserId(userId);
            record.setSignDate(today);
            record.setContinuousDays(continuousDays + 1);
            record.setPointsEarned(pointsEarned);
            save(record);

            log.info("User signed in: userId={}, date={}, continuousDays={}, points={}",
                    userId, today, continuousDays + 1, pointsEarned);

            // 构建返回VO
            SignVO vo = new SignVO();
            vo.setSigned(true);
            vo.setSignDate(today);
            vo.setContinuousDays(continuousDays + 1);
            vo.setPointsEarned(pointsEarned);
            vo.setBonusDesc(bonusDesc);
            vo.setSignedDates(getMonthSignedDates(userId, today));

            return vo;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public SignVO getSignStatus(Long userId) {
        LocalDate today = LocalDate.now();

        // 查询今天是否已签到
        SignRecord todayRecord = signRecordMapper.selectByUserIdAndDate(userId, today);
        List<SignRecord> recentRecords = signRecordMapper.selectRecentByUserId(userId, 31);
        int continuousDays = calculateContinuousDays(recentRecords);

        SignVO vo = new SignVO();
        vo.setSigned(todayRecord != null);
        vo.setSignDate(today);
        vo.setContinuousDays(continuousDays);
        vo.setSignedDates(getMonthSignedDates(userId, today));
        return vo;
    }

    /**
     * 计算连续签到天数（从最近一条记录向前计算）
     */
    private int calculateContinuousDays(List<SignRecord> records) {
        if (records.isEmpty()) {
            return 0;
        }
        // 记录按日期倒序排列
        int continuous = 0;
        LocalDate expectedDate = LocalDate.now().minusDays(1); // 从昨天开始检查
        for (SignRecord record : records) {
            if (record.getSignDate().equals(expectedDate)) {
                continuous++;
                expectedDate = expectedDate.minusDays(1);
            } else if (record.getSignDate().equals(expectedDate.plusDays(1))) {
                // 第一项是昨天（连续开始）
                continuous = 1;
                expectedDate = record.getSignDate().minusDays(1);
            } else {
                break;
            }
        }
        return continuous;
    }

    /**
     * 获取当月已签到日期列表
     */
    private List<LocalDate> getMonthSignedDates(Long userId, LocalDate date) {
        YearMonth yearMonth = YearMonth.of(date.getYear(), date.getMonth());
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        LambdaQueryWrapper<SignRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SignRecord::getUserId, userId)
               .between(SignRecord::getSignDate, start, end)
               .orderByAsc(SignRecord::getSignDate);
        List<SignRecord> records = signRecordMapper.selectList(wrapper);

        List<LocalDate> dates = new ArrayList<>();
        for (SignRecord record : records) {
            dates.add(record.getSignDate());
        }
        return dates;
    }

}
