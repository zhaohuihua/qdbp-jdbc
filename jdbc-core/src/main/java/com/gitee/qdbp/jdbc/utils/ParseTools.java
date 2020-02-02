package com.gitee.qdbp.jdbc.utils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
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
import com.gitee.qdbp.able.jdbc.condition.DbUpdate;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.able.jdbc.utils.FieldTools;
import com.gitee.qdbp.jdbc.model.AllFieldColumn;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 解析工具类
 *
 * @author zhaohuihua
 * @version 190703
 */
public class ParseTools {

    /** 分页/排序对象的通用字段 **/
    private static List<String> COMMON_FIELDS = Arrays.asList("_", "extra", "offset", "pageSize", "skip", "rows",
        "page", "needCount", "paging", "ordering");
    /** 允许数组的字段名后缀 **/
    private static List<String> WHERE_ARRAY_FIELDS = Arrays.asList("In", "NotIn", "Between", "NotBetween");

    /**
     * 将Java对象转换为Where对象
     * 
     * @param entity Java对象
     * @return Where对象
     */
    public static DbWhere parseWhereFromEntity(Object entity) {
        if (entity == null) {
            return null;
        }
        // 不能直接用fastjson的JSON.toJSON转换, 会导致日期/枚举被转换为基本类型
        // Map<String, Object> map = (JSONObject) JSON.toJSON(entity);
        Map<String, Object> map = beanToMap(entity);
        return DbWhere.from(map);
    }

    /**
     * 将Java对象转换为Update对象
     * 
     * @param entity Java对象
     * @return Update对象
     */
    public static DbUpdate parseUpdateFromEntity(Object entity) {
        if (entity == null) {
            return null;
        }
        Map<String, Object> map = (JSONObject) JSON.toJSON(entity);
        return DbUpdate.from(map);
    }

    /**
     * 从请求参数中构建Where对象<br>
     * 只会包含clazz注解中通过@JoyInColumn指定的字段名
     * 
     * @param params 请求参数
     * @param clazz 实体类
     * @return Where对象
     */
    public static <T> DbWhere parseWhereFromParams(Map<String, String[]> params, Class<T> clazz) {
        AllFieldColumn<?> allFields = DbTools.parseToAllFieldColumn(clazz);
        List<String> fieldNames = allFields.getFieldNames();
        Map<String, Object> map = parseMapWithWhitelist(params, fieldNames, WHERE_ARRAY_FIELDS);
        return DbWhere.from(map);
    }

    /**
     * 从请求参数中构建Where对象<br>
     * <pre>
     * 转换规则:
        fieldName$Equals(=), fieldName$NotEquals(!=), 
        fieldName$LessThen(<), fieldName$LessEqualsThen(<=), 
        fieldName$GreaterThen(>), fieldName$GreaterEqualsThen(>=), 
        fieldName$IsNull, fieldName$IsNotNull, 
        fieldName$Like, fieldName$NotLike, fieldName$Starts, fieldName$Ends, 
        fieldName$In, fieldName$NotIn, fieldName$Between
     * </pre>
     * 
     * @param params 请求参数
     * @param excludeDefault 是否排除默认的公共字段<br>
     *            extra, offset, pageSize, skip, rows, page, needCount, paging, orderings
     * @param excludeFields 排除的字段名, optional
     * @return Where对象
     */
    public static DbWhere parseWhereFromParams(Map<String, String[]> params, boolean excludeDefault,
            String... excludeFields) {
        List<String> realExcludeFields = new ArrayList<String>();
        if (excludeDefault) {
            realExcludeFields.addAll(COMMON_FIELDS);
        }
        if (VerifyTools.isNotBlank(excludeFields)) {
            for (String string : excludeFields) {
                realExcludeFields.add(string);
            }
        }
        Map<String, Object> map = parseMapWithBlacklist(params, realExcludeFields, WHERE_ARRAY_FIELDS);
        return DbWhere.from(map);
    }

    /**
     * 从请求参数中构建Update对象<br>
     * 只会包含clazz注解中通过@JoyInColumn指定的字段名 <pre>
     * 转换规则:
        fieldName 或 fieldName$Equals(=)
        fieldName$Add(增加值)
        fieldName$ToNull(转换为空)
     * </pre>
     * 
     * @param params 请求参数
     * @param clazz 实体类
     * @return Update对象
     */
    public static <T> DbUpdate parseUpdateFromParams(Map<String, String[]> params, Class<T> clazz) {
        AllFieldColumn<?> allFields = DbTools.parseToAllFieldColumn(clazz);
        List<String> fieldNames = allFields.getFieldNames();
        Map<String, Object> map = parseMapWithWhitelist(params, fieldNames, null);
        return DbUpdate.from(map);
    }

    /**
     * 从请求参数中构建Update对象
     * 
     * @param params 请求参数
     * @param excludeDefault 是否排除默认的公共字段<br>
     *            extra, offset, pageSize, skip, rows, page, needCount, paging, orderings
     * @param excludeFields 排除的字段名, optional
     * @return Update对象
     */
    public static DbUpdate parseUpdateFromParams(Map<String, String[]> params, boolean excludeDefault,
            String... excludeFields) {
        List<String> realExcludeFields = new ArrayList<String>();
        if (excludeDefault) {
            realExcludeFields.addAll(COMMON_FIELDS);
        }
        if (VerifyTools.isNotBlank(excludeFields)) {
            for (String string : excludeFields) {
                realExcludeFields.add(string);
            }
        }
        Map<String, Object> map = parseMapWithBlacklist(params, realExcludeFields, null);
        return DbUpdate.from(map);
    }

    /**
     * 将请求参数转换为Map对象
     * 
     * @param params 请求参数, required
     * @param excludeFields 排除的字段名, optional
     * @param allowArraySuffixes 允许数组的字段名后缀, optional
     * @return Map对象
     */
    public static Map<String, Object> parseMapWithBlacklist(Map<String, String[]> params, List<String> excludeFields,
            List<String> allowArraySuffixes) {
        if (params == null) {
            return null;
        }

        Map<String, Object> resultMap = new HashMap<String, Object>();
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            if (VerifyTools.isAnyBlank(entry.getKey(), entry.getValue())) {
                continue;
            }
            String fieldName = entry.getKey();
            if (fieldName.endsWith("[]")) {
                fieldName = fieldName.substring(0, fieldName.length() - 2);
            }
            String realFieldName = fieldName;
            int dollarLastIndex = fieldName.lastIndexOf('$');
            if (dollarLastIndex > 0) {
                realFieldName = fieldName.substring(0, dollarLastIndex);
            }
            if (FieldTools.contains(excludeFields, realFieldName)) {
                continue;
            }
            if (allowArraySuffixes != null && isEndsWith(fieldName, allowArraySuffixes)) {
                resultMap.put(fieldName, entry.getValue());
            } else {
                resultMap.put(fieldName, entry.getValue()[0]);
            }
        }
        return resultMap;
    }

    /**
     * 将请求参数转换为Map对象
     * 
     * @param params 请求参数, required
     * @param includeFields 有效的字段名, required
     * @param allowArraySuffixes 允许数组的字段名后缀, optional
     * @return Map对象
     */
    public static Map<String, Object> parseMapWithWhitelist(Map<String, String[]> params, List<String> includeFields,
            List<String> allowArraySuffixes) {
        if (params == null) {
            return null;
        }

        Map<String, Object> resultMap = new HashMap<String, Object>();
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            if (VerifyTools.isAnyBlank(entry.getKey(), entry.getValue())) {
                continue;
            }
            String fieldName = entry.getKey();
            if (fieldName.endsWith("[]")) {
                fieldName = fieldName.substring(0, fieldName.length() - 2);
            }
            String realFieldName = fieldName;
            int dollarLastIndex = fieldName.lastIndexOf('$');
            if (dollarLastIndex > 0) {
                realFieldName = fieldName.substring(0, dollarLastIndex);
            }
            if (!FieldTools.contains(includeFields, realFieldName)) {
                continue;
            }
            if (allowArraySuffixes != null && isEndsWith(fieldName, allowArraySuffixes)) {
                resultMap.put(fieldName, entry.getValue());
            } else {
                resultMap.put(fieldName, entry.getValue()[0]);
            }
        }
        return resultMap;
    }

    private static boolean isEndsWith(String fieldName, List<String> suffixes) {
        if (VerifyTools.isBlank(suffixes)) {
            return false;
        }
        for (String suffix : suffixes) {
            if (fieldName.endsWith('$' + suffix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 将Map转换为Java对象<br>
     * 为了不依赖qdbp-tools.jar, 从JsonTools复制而来
     * 
     * @param <T> 目标类型
     * @param map Map
     * @param clazz 目标Java类
     * @return Java对象
     * @see JsonTools#convert(Map, Class)
     */
    public static <T> T mapToBean(Map<String, ?> map, Class<T> clazz) {
        @SuppressWarnings("unchecked")
        Map<String, Object> json = (Map<String, Object>) map;
        return TypeUtils.castToJavaBean(json, clazz, ParserConfig.getGlobalInstance());
    }

    /**
     * 将Java对象转换为Map<br>
     * 为了不依赖qdbp-tools.jar, 从JsonTools复制而来<br>
     * copy from fastjson JSON.toJSON(), 保留enum和date
     * 
     * @param bean JavaBean对象
     * @return Map
     * @see JsonTools#beanToMap(Object)
     */
    public static Map<String, Object> beanToMap(Object bean) {
        if (bean == null) {
            return null;
        }

        Object json = beanToJson(bean, SerializeConfig.getGlobalInstance());
        if (json instanceof JSONObject) {
            return (JSONObject) json;
        } else {
            throw new IllegalArgumentException(bean.getClass().getSimpleName() + " can't convert to map.");
        }
    }

    protected static Object beanToJson(Object bean, SerializeConfig config) {
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
                Object jsonValue = beanToMap(entry.getValue());
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
