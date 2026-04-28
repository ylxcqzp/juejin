package com.juejin.starter.swagger;

import com.juejin.starter.swagger.config.Knife4jConfig;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Swagger自动配置类
 *
 * @author juejin
 */
@Configuration
@ConditionalOnClass(OpenAPI.class)
@Import({
        Knife4jConfig.class
})
public class JuejinSwaggerAutoConfiguration {

}
