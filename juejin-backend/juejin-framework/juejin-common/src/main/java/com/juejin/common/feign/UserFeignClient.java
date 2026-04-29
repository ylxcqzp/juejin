package com.juejin.common.feign;

import com.juejin.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 用户服务Feign客户端
 * 认证信息由 FeignConfig 请求拦截器自动传递，无需手动指定
 *
 * @author juejin
 */
@FeignClient(name = "juejin-user-service", path = "/api/v1/users")
public interface UserFeignClient {

    /**
     * 根据ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    Result<UserInfoVO> getUserById(@PathVariable("id") Long userId);

    /**
     * 检查用户是否存在
     *
     * @param userId 用户ID
     * @return 是否存在
     */
    @GetMapping("/{id}/exists")
    Result<Boolean> checkUserExists(@PathVariable("id") Long userId);

    /**
     * 增加用户掘力值（内部调用，用于签到奖励、任务奖励等）
     *
     * @param userId 用户ID
     * @param points 要增加的掘力值
     * @return 增加后的总掘力值
     */
    @PostMapping("/{id}/points/add")
    Result<Integer> addPoints(@PathVariable("id") Long userId, @RequestParam("points") Integer points);

}
