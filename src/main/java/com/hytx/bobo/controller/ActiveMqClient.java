package com.hytx.bobo.controller;

import javax.jms.Queue;
import javax.jms.Topic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hytx.bobo.config.JMSProducer;

@RestController
@RequestMapping("/api")
public class ActiveMqClient {

    @Autowired
    private Topic topic;

    @Autowired
    private Queue queue;

    @Autowired
    private JMSProducer jmsProducer;

    @GetMapping("/sendconvert")
    public void sendWithConvert(){
    	jmsProducer.sendMessage(queue, "生产者使用activeMq发送消息");
    	jmsProducer.sendMessage(topic, "生产者使用activeMq发送消息");
    }
    
    
}