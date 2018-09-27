package com.hytx.bobo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class JMSConsumer {
    private final static Logger logger = LoggerFactory.getLogger(JMSConsumer.class);

    @JmsListener(destination = JmsConfig.TOPIC,containerFactory = "jmsListenerContainerTopic")
    public void onTopicMessage(String msg) {
        logger.info("接收到topic消息：{}",msg);
    }

    @JmsListener(destination = JmsConfig.QUEUE,containerFactory = "jmsListenerContainerQueue")
    public void onQueueMessage(String msg) {
        logger.info("接收到queue消息：{}",msg);
    }
    
}