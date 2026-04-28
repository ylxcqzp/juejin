package com.juejin.common.feign;

import com.juejin.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 用户服务Feign客户端
 *
 * @author juejin
 */
@FeignClient(name = "juejin-user-service", path = "/api/v1/users")
public interface UserFeignClient {

    /**
     * 根据ID获取用户信息
     *
     * @param userId 用户ID
     * @param token 认证Token
     * @return 用户信息
     */
    @GetMapping("/{id}")
    Result<UserInfoVO> getUserById(@PathVariable("id") Long userId, 
                                   @RequestHeader("Authorization") String token);

    /**
     * 检查用户是否存在
     *
     * @param userId 用户ID
     * @return 是否存在
     */
    @GetMapping("/{id}/exists")
    Result<Boolean> checkUserExists(@PathVariable("id") Long userId);

}
