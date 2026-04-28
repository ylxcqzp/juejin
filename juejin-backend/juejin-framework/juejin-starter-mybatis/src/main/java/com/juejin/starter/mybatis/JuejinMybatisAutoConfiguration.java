package com.juejin.starter.mybatis;

import com.juejin.starter.mybatis.config.MybatisPlusConfig;
import com.juejin.starter.mybatis.handler.CustomMetaObjectHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * MyBatis-Plus自动配置类
 *
 * @author juejin
 */
@Configuration
@ConditionalOnClass(com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor.class)
@Import({
        MybatisPlusConfig.class,
        CustomMetaObjectHandler.class
})
public class JuejinMybatisAutoConfiguration {

}
