//package com.hytx.bobo.config.even;
//
//import com.winhxd.b2c.common.cache.Cache;
//import com.winhxd.b2c.common.constant.CacheName;
//import com.winhxd.b2c.common.mq.MQDestination;
//import com.winhxd.b2c.common.mq.MQHandler;
//import com.winhxd.b2c.common.mq.StringMessageListener;
//import com.winhxd.b2c.common.mq.StringMessageSender;
//import com.winhxd.b2c.common.mq.support.zipkin.ZipkinRabbitConfig;
//import com.winhxd.b2c.common.util.MessageSendUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.amqp.core.*;
//import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
//import org.springframework.amqp.rabbit.connection.ConnectionFactory;
//import org.springframework.amqp.rabbit.core.RabbitAdmin;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
//import org.springframework.aop.support.AopUtils;
//import org.springframework.beans.BeansException;
//import org.springframework.beans.factory.BeanFactory;
//import org.springframework.beans.factory.BeanFactoryAware;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.beans.factory.config.BeanPostProcessor;
//import org.springframework.beans.factory.support.DefaultListableBeanFactory;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Import;
//import org.springframework.context.annotation.Primary;
//import org.springframework.core.annotation.AnnotationUtils;
//import org.springframework.util.DigestUtils;
//import org.springframework.util.ReflectionUtils;
//
//import java.lang.reflect.InvocationTargetException;
//import java.nio.charset.StandardCharsets;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * MQ普通消息配置类
// *
// * @author lixiaodong
// */
//@Import(ZipkinRabbitConfig.class)
//public class MessageQueueConfig implements BeanPostProcessor, BeanFactoryAware {
//    private static final Logger logger = LoggerFactory.getLogger(MessageQueueConfig.class);
//    /**
//     * 消费异常最大次数,达到该次数后,消息不会返回至mq
//     */
//    private static final long MAX_ERROR_COUNT = 50;
//    /**
//     * 消费异常统计时间窗口,单位秒
//     */
//    private static final int MAX_ERROR_INTERVAL = 600;
//
//    private DefaultListableBeanFactory beanFactory;
//
//    @Autowired
//    @Qualifier("normalCachingConnectionFactory")
//    private ConnectionFactory connectionFactory;
//
//    @Autowired
//    private Cache cache;
//
//    @Bean(name = "normalMessageQueueProperties")
//    @ConfigurationProperties(prefix = "mq.normal")
//    public MessageQueueProperties normalMessageQueueProperties() {
//        return new MessageQueueProperties();
//    }
//
//    @Bean(name = "normalCachingConnectionFactory")
//    @Primary
//    public CachingConnectionFactory normalCachingConnectionFactory(
//            @Qualifier("normalMessageQueueProperties") MessageQueueProperties normalMessageQueueProperties) {
//        CachingConnectionFactory factory = new CachingConnectionFactory();
//        factory.setPublisherConfirms(true);
//        factory.setAddresses(normalMessageQueueProperties.getAddress());
//        factory.setUsername(normalMessageQueueProperties.getUsername());
//        factory.setPassword(normalMessageQueueProperties.getPassword());
//        if (StringUtils.isNotBlank(normalMessageQueueProperties.getVirtualHost())) {
//            factory.setVirtualHost(normalMessageQueueProperties.getVirtualHost());
//        }
//        return factory;
//    }
//
//    @Bean
//    @Primary
//    public RabbitTemplate normalRabbitTemplate(
//            @Qualifier("normalCachingConnectionFactory") CachingConnectionFactory normalCachingConnectionFactory) {
//        RabbitTemplate rabbitTemplate = new RabbitTemplate(normalCachingConnectionFactory);
//        return rabbitTemplate;
//    }
//    @Bean
//    @Primary
//    public RabbitAdmin normalRabbitAdmin(
//            @Qualifier("normalCachingConnectionFactory") CachingConnectionFactory normalCachingConnectionFactory) {
//        RabbitAdmin rabbitAdmin = new RabbitAdmin(normalCachingConnectionFactory);
//        return rabbitAdmin;
//    }
//
//    @Bean
//    public StringMessageSender stringMessageSender() {
//        return new StringMessageSender();
//    }
//
//    @Bean
//    public MessageSendUtils messageSendUtils() {
//        return new MessageSendUtils();
//    }
//
//    @Bean
//    public List<Declarable> declarableList() {
//        List<Declarable> list = new ArrayList<>();
//        for (MQHandler listener : MQHandler.values()) {
//            MQDestination dest = listener.getDestination();
//            FanoutExchange exchange = new FanoutExchange(dest.toString(), true, false);
//            exchange.setDelayed(dest.isDelayed());
//            list.add(exchange);
//            Queue queue = new Queue(listener.toString(), true, false, false);
//            Binding binding = BindingBuilder.bind(queue).to(exchange);
//            list.add(queue);
//            list.add(binding);
//        }
//
//        return list;
//    }
//
//    @Override
//    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
//        this.beanFactory = (DefaultListableBeanFactory) beanFactory;
//    }
//
//    @Override
//    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
//        Class<?> targetClass = AopUtils.getTargetClass(bean);
//        ReflectionUtils.doWithMethods(targetClass, method -> {
//            StringMessageListener annotation = AnnotationUtils.getAnnotation(method, StringMessageListener.class);
//            if (annotation != null) {
//                Class<?>[] parameterTypes = method.getParameterTypes();
//                if (parameterTypes == null || parameterTypes.length != 1 || !String.class.isAssignableFrom(parameterTypes[0])) {
//                    throw new IllegalArgumentException("StringMessageListener参数仅支持String: " + targetClass.getCanonicalName() + "#" + method.getName());
//                }
//                SimpleMessageListenerContainer listenerContainer = new SimpleMessageListenerContainer(connectionFactory);
//                String queueName = annotation.value().toString();
//                listenerContainer.setQueueNames(queueName);
//                listenerContainer.setAcknowledgeMode(AcknowledgeMode.AUTO);
//                if (StringUtils.isNotBlank(annotation.concurrency())) {
//                    listenerContainer.setConcurrency(annotation.concurrency());
//                }
//                listenerContainer.setMessageListener(message -> {
//                    String body = new String(message.getBody(), StandardCharsets.UTF_8);
//                                        try {
//                        method.invoke(bean, body);
//                    } catch (IllegalAccessException e) {
//                        logger.error("MQ消费异常:" + body, e);
//                    } catch (InvocationTargetException e) {
//                        logger.error("MQ消费异常:" + body, e.getCause());
//                        String errorKey = CacheName.MQ_HANDLER_ERROR + queueName + ":" + DigestUtils.md5DigestAsHex(body.getBytes(StandardCharsets.UTF_8));
//                        Long count = cache.incr(errorKey);
//                        if (count == 1) {
//                            cache.expire(errorKey, MAX_ERROR_INTERVAL);
//                        }
//                        if (count >= MAX_ERROR_COUNT) {
//                            cache.del(errorKey);
//                            logger.warn("MQ消费超过异常次数: " + body);
//                        } else {
//                            throw new RuntimeException("MQ消费异常", e);
//                        }
//                    }
//                });
//                beanFactory.registerSingleton(beanName + "#" + method.getName(), listenerContainer);
//            }
//        }, ReflectionUtils.USER_DECLARED_METHODS);
//        return bean;
//    }
//}
