package com.github.open.courier.core.converter;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 消息json转换器
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MessageJsonConverter {

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        mapper.setSerializationInclusion(Include.NON_NULL);
    }

    /**
     * Object转换json
     */
    public static <T> String toJson(T obj) {

        if (null == obj) {
            return null;
        }

        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("toJson error, obj:{}", obj, e);
            return null;
        }
    }

    /**
     * json转Object
     */
    public static <T> T toObject(String json, Class<? extends T> objClass) {

        if (StringUtils.isBlank(json)) {
            return null;
        }

        try {
            return mapper.readValue(json, objClass);
        } catch (Exception e) {
            log.error("toObject error, json:{}", json, e);
            return null;
        }
    }

    /**
     * json转JsonNode
     */
    public static JsonNode toNode(String json) {

        if (StringUtils.isBlank(json)) {
            return null;
        }

        try {
            return mapper.readTree(json);
        } catch (Exception e) {
            log.error("toNode error, json:{}", json, e);
            return null;
        }
    }

    /**
     * json转Object
     */
    public static <T> T toObject(String json, TypeReference<T> typeReference) {

        if (StringUtils.isEmpty(json)) {
            return null;
        }

        try {
            return mapper.readValue(json, typeReference);
        } catch (Exception e) {
            log.error("toObject error, json:{}", json, e);
        }

        return null;
    }
}
