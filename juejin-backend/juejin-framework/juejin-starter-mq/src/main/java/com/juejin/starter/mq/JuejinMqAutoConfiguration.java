package com.juejin.starter.mq;

import com.juejin.starter.mq.constants.MqConstants;
import com.juejin.starter.mq.template.MqTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * MQ自动配置类
 *
 * @author juejin
 */
@Configuration
@ConditionalOnClass(RabbitTemplate.class)
@Import({
        MqConstants.class,
        MqTemplate.class
})
public class JuejinMqAutoConfiguration {

}
