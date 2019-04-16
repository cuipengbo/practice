package com.hytx.bobo.config.even;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * °ïÖúÀà
 *
 * @author lixiaodong
 */
public class EventMessageHelper {
    private static final Logger logger = LoggerFactory.getLogger(EventMessageHelper.class);

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static <T> String toJson(String eventKey, T eventObject) {
        try {
            EventTransferObject<T> eto = new EventTransferObject<>(eventKey, eventObject);
            return objectMapper.writeValueAsString(eto);
        } catch (JsonProcessingException e) {
            logger.error("EventMessageHelper->toJson", e);
        }
        return null;
    }

    public static <T> EventTransferObject<T> toTransferObject(String json, Class<T> clazz) throws IOException {
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(EventTransferObject.class, clazz);
        return objectMapper.readValue(json, javaType);
    }

    public static class EventTransferObject<T> {
        private String eventKey;
        private T eventObject;

        public EventTransferObject() {
        }

        public EventTransferObject(String eventKey, T eventObject) {
            this.eventKey = eventKey;
            this.eventObject = eventObject;
        }

        public String getEventKey() {
            return eventKey;
        }

        public void setEventKey(String eventKey) {
            this.eventKey = eventKey;
        }

        public T getEventObject() {
            return eventObject;
        }

        public void setEventObject(T eventObject) {
            this.eventObject = eventObject;
        }
    }
}
