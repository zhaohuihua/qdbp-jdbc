package com.gitee.qdbp.jdbc.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
        Map<String, Object> map = (JSONObject) JSON.toJSON(entity);
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
}
