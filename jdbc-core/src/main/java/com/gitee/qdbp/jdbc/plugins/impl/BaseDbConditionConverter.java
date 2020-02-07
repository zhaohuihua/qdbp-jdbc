package com.gitee.qdbp.jdbc.plugins.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.gitee.qdbp.able.jdbc.condition.DbUpdate;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.able.jdbc.condition.DbWhere.EmptiableWhere;
import com.gitee.qdbp.able.jdbc.utils.FieldTools;
import com.gitee.qdbp.jdbc.model.AllFieldColumn;
import com.gitee.qdbp.jdbc.plugins.DbConditionConverter;
import com.gitee.qdbp.jdbc.utils.DbTools;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * JavaBean到数据库条件的转换
 *
 * @author zhaohuihua
 * @version 200207
 */
public abstract class BaseDbConditionConverter implements DbConditionConverter {

    /** 允许数组的字段名后缀 **/
    private List<String> whereArrayFields = new ArrayList<>();

    public BaseDbConditionConverter() {
        addWhereArrayFields("In", "NotIn", "Between", "NotBetween");
    }

    /** 将Java对象转换为Map **/
    protected abstract Map<String, Object> convertBeanToMap(Object bean);

    @Override
    public Map<String, Object> convertBeanToInsertMap(Object bean) {
        return convertBeanToMap(bean);
    }

    @Override
    public DbWhere convertBeanToDbWhere(Object bean) {
        Map<String, Object> map = convertBeanToMap(bean);
        return DbWhere.parse(map, EmptiableWhere.class);
    }

    @Override
    public DbUpdate convertBeanToDbUpdate(Object bean) {
        Map<String, Object> map = convertBeanToMap(bean);
        return DbUpdate.parse(map, DbUpdate.class);
    }

    /**
     * 从请求参数request.getParameterMap()中构建Where对象<br>
     * 只会包含clazz注解中通过@JoyInColumn指定的字段名<br>
     * 应注意, 此时参数由前端传入, 条件不可控, 也有可能条件为空, 需要仔细检查条件内容, 防止越权操作 <pre>
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
     * @param emptiable 是否允许条件为空
     * @param clazz 实体类
     * @return Where对象
     */
    @Override
    public <T> DbWhere parseParamsToDbWhere(Map<String, String[]> params, Class<T> clazz) {
        AllFieldColumn<?> allFields = DbTools.parseToAllFieldColumn(clazz);
        List<String> fieldNames = allFields.getFieldNames();
        Map<String, Object> map = parseMapWithWhitelist(params, fieldNames, whereArrayFields);
        return DbWhere.parse(map, EmptiableWhere.class);
    }

    /**
     * 从请求参数request.getParameterMap()中构建Update对象<br>
     * 只会包含clazz注解中通过@JoyInColumn指定的字段名<br>
     * 应注意, 此时参数由前端传入, 条件不可控, 也有可能条件为空, 需要仔细检查条件内容, 防止越权操作 <pre>
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
    @Override
    public <T> DbUpdate parseParamsToDbUpdate(Map<String, String[]> params, Class<T> clazz) {
        VerifyTools.requireNonNull(params, "params");
        AllFieldColumn<?> allFields = DbTools.parseToAllFieldColumn(clazz);
        List<String> fieldNames = allFields.getFieldNames();
        Map<String, Object> map = parseMapWithWhitelist(params, fieldNames, null);
        return DbUpdate.parse(map, DbUpdate.class);
    }

    /**
     * 将请求参数转换为Map对象
     * 
     * @param params 请求参数, required
     * @param excludeFields 排除的字段名, optional
     * @param allowArraySuffixes 允许数组的字段名后缀, optional
     * @return Map对象
     */
    protected Map<String, Object> parseMapWithBlacklist(Map<String, String[]> params, List<String> excludeFields,
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
    protected Map<String, Object> parseMapWithWhitelist(Map<String, String[]> params, List<String> includeFields,
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

    protected boolean isEndsWith(String fieldName, List<String> suffixes) {
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

    /** 获取允许数组的字段名后缀 **/
    public List<String> getWhereArrayFields() {
        return whereArrayFields;
    }

    /** 设置允许数组的字段名后缀 **/
    public void setWhereArrayFields(List<String> fields) {
        this.whereArrayFields.clear();
        this.whereArrayFields.addAll(fields);
    }

    /** 增加允许数组的字段名后缀 **/
    public void addWhereArrayFields(String... fields) {
        if (fields == null || fields.length == 0) {
            return;
        }
        for (String field : fields) {
            this.whereArrayFields.add(field);
        }
    }

    /** 增加允许数组的字段名后缀 **/
    public void addWhereArrayFields(List<String> fields) {
        if (fields == null || fields.isEmpty()) {
            return;
        }
        this.whereArrayFields.addAll(fields);
    }
}
