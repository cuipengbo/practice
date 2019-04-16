//package com.hytx.bobo.config.even;
//
//import com.winhxd.b2c.common.cache.Cache;
//import com.winhxd.b2c.common.cache.RedisLock;
//import com.winhxd.b2c.common.constant.CacheName;
//import com.winhxd.b2c.common.mq.event.EventMessageListener;
//import com.winhxd.b2c.common.mq.event.EventMessageSender;
//import com.winhxd.b2c.common.mq.event.EventType;
//import com.winhxd.b2c.common.mq.event.EventTypeHandler;
//import com.winhxd.b2c.common.mq.support.MessageQueueConfig;
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.amqp.core.*;
//import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
//import org.springframework.amqp.rabbit.connection.ConnectionFactory;
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
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Import;
//import org.springframework.core.annotation.AnnotationUtils;
//import org.springframework.util.ReflectionUtils;
//
//import java.io.IOException;
//import java.lang.reflect.Method;
//import java.nio.charset.StandardCharsets;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//
///**
// * 事件消息配置类
// *
// * @author lixiaodong
// */
//@Import(MessageQueueConfig.class)
//public class EventMessageConfig implements BeanPostProcessor, BeanFactoryAware {
//    private static final Logger logger = LoggerFactory.getLogger(MessageQueueConfig.class);
//    /**
//     * 事件消息锁最长有效时间
//     */
//    private static final int LOCK_EXPIRES = 12000;
//    /**
//     * 尝试获取事件消息锁超时时间
//     */
//    private static final int LOCK_TRY_MS = 6000;
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
//    @Bean
//    public RabbitTemplate eventRabbitTemplate(CachingConnectionFactory normalCachingConnectionFactory) {
//        RabbitTemplate rabbitTemplate = new RabbitTemplate(normalCachingConnectionFactory);
//        rabbitTemplate.setMandatory(true);
//        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
//            EventCorrelationData data = (EventCorrelationData) correlationData;
//            if (ack) {
//                String idKey = CacheName.EVENT_MESSAGE_ID + data.getEventType().toString();
//                String bodyKey = CacheName.EVENT_MESSAGE_BODY + data.getEventType().toString();
//                cache.zrem(idKey, data.getId());
//                cache.hdel(bodyKey, data.getId());
//                logger.info("事件消息发送成功: {} - {}", data.getEventType(), data.getId());
//            } else {
//                logger.warn("事件消息发送失败: {} - {}, {}", data.getEventType(), data.getId(), cause);
//            }
//        });
//        return rabbitTemplate;
//    }
//
//    @Bean
//    public EventMessageSender eventMessageSender() {
//        return new EventMessageSender();
//    }
//
//    @Bean
//    public List<Declarable> declarableEventList() {
//        List<Declarable> list = new ArrayList<>();
//        for (EventTypeHandler handler : EventTypeHandler.values()) {
//            EventType dest = handler.getEventType();
//            FanoutExchange exchange = new FanoutExchange(dest.toString(), true, false);
//            list.add(exchange);
//            Queue queue = new Queue(handler.toString(), true, false, false);
//            Binding binding = BindingBuilder.bind(queue).to(exchange);
//            list.add(queue);
//            list.add(binding);
//        }
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
//            EventMessageListener annotation = AnnotationUtils.getAnnotation(method, EventMessageListener.class);
//            if (annotation != null) {
//                EventTypeHandler eventHandler = annotation.value();
//                Class<?> eventObjectClass = eventHandler.getEventType().getEventObjectClass();
//                Class<?>[] parameterTypes = method.getParameterTypes();
//                if (parameterTypes == null || parameterTypes.length != 2
//                        || !String.class.isAssignableFrom(parameterTypes[0])
//                        || !eventObjectClass.isAssignableFrom(parameterTypes[1])) {
//                    throw new IllegalArgumentException("事件消息监听方法参数错误: " + targetClass.getCanonicalName() + "#" + method.getName());
//                }
//                SimpleMessageListenerContainer listenerContainer = new SimpleMessageListenerContainer(connectionFactory);
//                listenerContainer.setQueueNames(eventHandler.toString());
//                listenerContainer.setAcknowledgeMode(AcknowledgeMode.AUTO);
//                if (StringUtils.isNotBlank(annotation.concurrency())) {
//                    listenerContainer.setConcurrency(annotation.concurrency());
//                }
//                listenerContainer.setMessageListener(new EventChannelAwareMessageListener(bean, method, eventHandler));
//                beanFactory.registerSingleton(beanName + "#" + method.getName(), listenerContainer);
//            }
//        }, ReflectionUtils.USER_DECLARED_METHODS);
//        return bean;
//    }
//
//    private class EventChannelAwareMessageListener implements MessageListener {
//        private Object bean;
//        private Method method;
//        EventTypeHandler eventHandler;
//
//        public EventChannelAwareMessageListener(Object bean, Method method, EventTypeHandler eventHandler) {
//            this.bean = bean;
//            this.method = method;
//            this.eventHandler = eventHandler;
//        }
//
//        @Override
//        public void onMessage(Message message) {
//            String body = new String(message.getBody(), StandardCharsets.UTF_8);
//            EventMessageHelper.EventTransferObject<?> transferObject;
//            try {
//                transferObject = EventMessageHelper.toTransferObject(body, eventHandler.getEventType().getEventObjectClass());
//            } catch (IOException e) {
//                logger.error("事件消息接收异常: " + e.toString() + ", body: " + body, e);
//                return;
//            }
//            String key = CacheName.EVENT_MESSAGE_HANDLER + eventHandler.toString() + ":" + transferObject.getEventKey();
//            logger.info("事件消息接收同步: {}", key);
//            RedisLock redisLock = new RedisLock(cache, key, LOCK_EXPIRES);
//            if (!redisLock.tryLock(LOCK_TRY_MS, TimeUnit.MILLISECONDS)) {
//                logger.warn("事件消息接收超时: {}", key);
//                throw new RuntimeException("事件消息接收超时: " + key);
//            }
//            logger.info("事件消息处理开始: {}", key);
//            try {
//                method.invoke(bean, transferObject.getEventKey(), transferObject.getEventObject());
//                logger.info("事件消息处理完成: {}", key);
//            } catch (Exception e) {
//                String msg = "事件消息处理异常: " + key + ", " + e.toString();
//                logger.error(msg + ", " + body, e);
//                String errorKey = CacheName.EVENT_MESSAGE_HANDLER_ERROR + eventHandler.toString() + ":" + transferObject.getEventKey();
//                Long count = cache.incr(errorKey);
//                if (count == 1) {
//                    cache.expire(errorKey, MAX_ERROR_INTERVAL);
//                }
//                if (count >= MAX_ERROR_COUNT) {
//                    cache.del(errorKey);
//                    logger.warn("事件消息处理超过异常次数: {}, {}", key, body);
//                } else {
//                    throw new RuntimeException(msg, e);
//                }
//            } finally {
//                redisLock.unlock();
//            }
//        }
//    }
//}