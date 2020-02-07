package com.gitee.qdbp.jdbc.utils;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.JSONSerializable;
import com.alibaba.fastjson.serializer.JavaBeanSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.util.TypeUtils;
import com.gitee.qdbp.tools.utils.ConvertTools;

/**
 * Json工具类<br>
 * 为去除对qdbp-tools.jar的依赖, 复制自qdbp-tools.jar的JsonTools
 *
 * @author zhaohuihua
 * @version 180621
 */
abstract class JsonTools {

    /**
     * 将Map转换为Java对象
     * 
     * @param <T> 目标类型
     * @param map Map
     * @param clazz 目标Java类
     * @return Java对象
     */
    public static <T> T mapToBean(Map<String, ?> map, Class<T> clazz) {
        @SuppressWarnings("unchecked")
        Map<String, Object> json = (Map<String, Object>) map;
        return TypeUtils.castToJavaBean(json, clazz, ParserConfig.getGlobalInstance());
    }

    /**
     * 将Java对象转换为Map<br>
     * copy from fastjson JSON.toJSON(), 保留enum和date
     * 
     * @param bean JavaBean对象
     * @return Map
     */
    public static Map<String, Object> beanToMap(Object bean) {
        if (bean == null) {
            return null;
        }
        return beanToMap(bean, true, true);
    }

    /**
     * 将Java对象转换为Map
     * 
     * @param object Java对象
     * @param deep 是否递归转换子对象
     * @param clearBlankValue 是否清除空值
     * @return Map
     */
    public static Map<String, Object> beanToMap(Object object, boolean deep, boolean clearBlankValue) {
        if (object == null) {
            return null;
        }
        Map<String, Object> map = getBeanFieldValuesMap(object, deep, SerializeConfig.getGlobalInstance());
        if (clearBlankValue) {
            ConvertTools.clearBlankValue(map);
        }
        return map;
    }

    protected static JSONObject getBeanFieldValuesMap(Object bean, boolean deep, SerializeConfig config) {
        if (bean == null) {
            return null;
        }

        if (bean instanceof JSONObject) {
            return (JSONObject) bean;
        }

        if (bean instanceof JSONArray) {
            throw new IllegalArgumentException(bean.getClass().getSimpleName() + " can't convert to map.");
        }

        if (bean instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) bean;

            Map<String, Object> innerMap;
            if (map instanceof LinkedHashMap) {
                innerMap = new LinkedHashMap<>(map.size());
            } else if (map instanceof TreeMap) {
                innerMap = new TreeMap<>();
            } else {
                innerMap = new HashMap<>(map.size());
            }

            JSONObject json = new JSONObject(innerMap);

            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String jsonKey = TypeUtils.castToString(entry.getKey());
                Object jsonValue = !deep ? entry.getValue() : beanToJson(entry.getValue(), config);
                json.put(jsonKey, jsonValue);
            }

            return json;
        }

        if (bean instanceof Collection) {
            throw new IllegalArgumentException(bean.getClass().getSimpleName() + " can't convert to map.");
        }

        if (bean instanceof JSONSerializable) {
            String string = JSON.toJSONString(bean);
            Object json = JSON.parse(string);
            return getBeanFieldValuesMap(json, deep, config);
        }

        Class<?> clazz = bean.getClass();

        if (clazz.isEnum()) {
            throw new IllegalArgumentException(bean.getClass().getSimpleName() + " can't convert to map.");
        }
        if (clazz == String.class) {
            throw new IllegalArgumentException(bean.getClass().getSimpleName() + " can't convert to map.");
        }
        if (CharSequence.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException(bean.getClass().getSimpleName() + " can't convert to map.");
        }
        if (isPrimitive(clazz)) {
            throw new IllegalArgumentException(bean.getClass().getSimpleName() + " can't convert to map.");
        }
        if (clazz.isArray()) {
            throw new IllegalArgumentException(bean.getClass().getSimpleName() + " can't convert to map.");
        }

        ObjectSerializer serializer = config.getObjectWriter(clazz);
        if (serializer instanceof JavaBeanSerializer) {
            JavaBeanSerializer javaBeanSerializer = (JavaBeanSerializer) serializer;

            JSONObject json = new JSONObject();
            try {
                Map<String, Object> values = javaBeanSerializer.getFieldValuesMap(bean);
                for (Map.Entry<String, Object> entry : values.entrySet()) {
                    Object jsonValue = !deep ? entry.getValue() : beanToJson(entry.getValue(), config);
                    json.put(entry.getKey(), jsonValue);
                }
            } catch (Exception e) {
                throw new IllegalArgumentException(bean.getClass().getSimpleName() + " can't convert to map.", e);
            }
            return json;
        }

        throw new IllegalArgumentException(bean.getClass().getSimpleName() + " can't convert to map.");
    }

    private static Object beanToJson(Object bean, SerializeConfig config) {
        if (bean == null) {
            return null;
        }

        if (bean instanceof JSON) {
            return bean;
        }

        if (bean instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) bean;

            Map<String, Object> innerMap;
            if (map instanceof LinkedHashMap) {
                innerMap = new LinkedHashMap<>(map.size());
            } else if (map instanceof TreeMap) {
                innerMap = new TreeMap<>();
            } else {
                innerMap = new HashMap<>(map.size());
            }

            JSONObject json = new JSONObject(innerMap);

            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String jsonKey = TypeUtils.castToString(entry.getKey());
                Object jsonValue = beanToJson(entry.getValue(), config);
                json.put(jsonKey, jsonValue);
            }

            return json;
        }

        if (bean instanceof Collection) {
            Collection<?> collection = (Collection<?>) bean;

            JSONArray array = new JSONArray(collection.size());

            for (Object item : collection) {
                Object jsonValue = beanToJson(item, config);
                array.add(jsonValue);
            }

            return array;
        }

        if (bean instanceof JSONSerializable) {
            String json = JSON.toJSONString(bean);
            return JSON.parse(json);
        }

        Class<?> clazz = bean.getClass();

        if (clazz.isEnum()) {
            // return ((Enum<?>) bean).name();
            return bean;
        }
        if (clazz == String.class) {
            return bean;
        }
        if (CharSequence.class.isAssignableFrom(clazz)) {
            return bean.toString();
        }
        if (isPrimitive(clazz)) {
            return bean;
        }

        if (clazz.isArray()) {
            int len = Array.getLength(bean);

            JSONArray array = new JSONArray(len);

            for (int i = 0; i < len; ++i) {
                Object item = Array.get(bean, i);
                Object jsonValue = beanToJson(item, config);
                array.add(jsonValue);
            }

            return array;
        }

        ObjectSerializer serializer = config.getObjectWriter(clazz);
        if (serializer instanceof JavaBeanSerializer) {
            JavaBeanSerializer javaBeanSerializer = (JavaBeanSerializer) serializer;

            JSONObject json = new JSONObject();
            try {
                Map<String, Object> values = javaBeanSerializer.getFieldValuesMap(bean);
                for (Map.Entry<String, Object> entry : values.entrySet()) {
                    json.put(entry.getKey(), beanToJson(entry.getValue(), config));
                }
            } catch (Exception e) {
                throw new JSONException("BeanConvertToJsonError", e);
            }
            return json;
        }

        String text = JSON.toJSONString(bean);
        return JSON.parse(text);
    }

    private static boolean isPrimitive(Class<?> clazz) {
        // @formatter:off
        return clazz.isPrimitive()
            || clazz.isEnum()
            || clazz == Boolean.class
            || clazz == Character.class
            || clazz == String.class
            || Number.class.isAssignableFrom(clazz)
            || Date.class.isAssignableFrom(clazz);
        // @formatter:on
    }

}
