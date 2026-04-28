package com.juejin.starter.mq.template;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * MQ消息发送模板类
 *
 * @author juejin
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MqTemplate {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送消息到指定队列
     */
    public void send(String queue, Object message) {
        rabbitTemplate.convertAndSend(queue, message);
        log.debug("Send message to queue [{}]: {}", queue, message);
    }

    /**
     * 发送消息到指定交换机和路由键
     */
    public void send(String exchange, String routingKey, Object message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
        log.debug("Send message to exchange [{}] with routingKey [{}]: {}", exchange, routingKey, message);
    }

    /**
     * 发送延迟消息
     */
    public void sendDelay(String exchange, String routingKey, Object message, int delayMillis) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message, msg -> {
            msg.getMessageProperties().setDelay(delayMillis);
            return msg;
        });
        log.debug("Send delay message to exchange [{}] with routingKey [{}], delay [{}]ms: {}", 
                exchange, routingKey, delayMillis, message);
    }

}
