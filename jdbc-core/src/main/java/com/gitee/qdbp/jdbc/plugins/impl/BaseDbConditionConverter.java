package com.gitee.qdbp.jdbc.plugins.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.gitee.qdbp.able.jdbc.condition.DbField;
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
 * @version 20200207
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
        Map<String, Object> map = convertBeanToMap(bean);
        // clearEmptyString=true: 值为空字符串的字段, 表示用户未填写, 应予清除
        // 因为这里的bean对象极有可能来自于controller的参数
        // 如果不清除, 将会生成NULL的VALUES语句, 数据库设置的默认值就不会生效
        clearBlankValue(map, true, true);
        return map;
    }

    @Override
    public DbWhere convertBeanToDbWhere(Object bean) {
        Map<String, Object> map = convertBeanToMap(bean);
        // clearEmptyString=true: 值为空字符串的Wherre字段, 表示用户未填写, 应予清除
        // 因为这里的bean对象极有可能来自于controller的参数
        // 如果不清除, 生成的SQL语句就会带有很多的FIELD1 IS NULL and FIELD2 IS NULL这样的条件
        clearBlankValue(map, true, true);
        return parseMapToDbWhere(map, EmptiableWhere.class);
    }

    @Override
    public DbUpdate convertBeanToDbUpdate(Object bean) {
        Map<String, Object> map = convertBeanToMap(bean);
        // clearEmptyString=false: 值为空字符串的Update字段, 表示设置为NULL, 应予保留
        // 如果清除了, 用户想要将已经有值的字段清空将变得难以处理
        clearBlankValue(map, true, false);
        return parseMapToDbUpdate(map, DbUpdate.class);
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
     * @param beanType JavaBean类型
     * @return Where对象
     */
    @Override
    public <T> DbWhere parseParamsToDbWhere(Map<String, String[]> params, Class<T> beanType) {
        AllFieldColumn<?> allFields = DbTools.parseToAllFieldColumn(beanType);
        List<String> fieldNames = allFields.getFieldNames();
        Map<String, Object> map = parseMapWithWhitelist(params, fieldNames, whereArrayFields);
        return parseMapToDbWhere(map, EmptiableWhere.class);
    }

    /**
     * 从map中获取参数构建DbWhere对象
     * 
     * @param <T> DbWhere泛型
     * @param map Map参数
     * @param instanceType DbWhere对象类型
     * @return DbWhere对象实例
     */
    protected <T extends DbWhere> T parseMapToDbWhere(Map<String, Object> map, Class<T> instanceType) {
        // clearEmptyString=true: 值为空字符串的Wherre字段, 表示用户未填写, 应予清除
        // 如果不清除, 生成的SQL语句就会带有很多的FIELD1 IS NULL and FIELD2 IS NULL这样的条件
        List<DbField> fields = parseMapToDbFields(map, true);
        return DbWhere.ofFields(fields, instanceType);
    }

    /**
     * 从map中获取参数构建DbUpdate对象
     * 
     * @param <T> DbUpdate泛型
     * @param map Map参数
     * @param instanceType DbUpdate对象类型
     * @return DbUpdate对象实例
     */
    protected <T extends DbUpdate> T parseMapToDbUpdate(Map<String, Object> map, Class<T> instanceType) {
        // clearEmptyString=false: 值为空字符串的Update字段, 表示设置为NULL, 应予保留
        // 如果清除了, 用户想要将已经有值的字段清空将变得难以处理
        List<DbField> fields = parseMapToDbFields(map, false);
        return DbUpdate.ofFields(fields, instanceType);
    }

    /**
     * 将Map转换为DbField列表
     * 
     * @param map Map
     * @param clearEmptyString 是否清除值为空字符串的字段
     * @return DbField列表
     */
    protected List<DbField> parseMapToDbFields(Map<String, Object> map, boolean clearEmptyString) {
        List<DbField> fields = new ArrayList<>();
        if (map != null) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (VerifyTools.isBlank(key) || value == null) {
                    continue;
                }
                if (clearEmptyString && "".equals(value)) {
                    continue;
                }
                int index = key.lastIndexOf('$');
                if (index < 0) {
                    fields.add(new DbField(null, key, value));
                } else {
                    String field = key.substring(0, index);
                    String operate = key.substring(index + 1);
                    fields.add(new DbField(operate, field, value));
                }
            }
        }
        return fields;
    }

    /**
     * 清除Map中的空值
     * 
     * @param map Map对象
     * @param clearNullValue 是否清除null值
     * @param clearEmptyString 是否清除空字符串
     */
    @SuppressWarnings("unchecked")
    protected void clearBlankValue(Map<String, Object> map, boolean clearNullValue, boolean clearEmptyString) {
        if (map == null) {
            return;
        }
        Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            Object value = entry.getValue();
            if (VerifyTools.isBlank(value)) {
                iterator.remove();
            } else if (value instanceof Map) {
                clearBlankValue((Map<String, Object>) value, clearNullValue, clearEmptyString);
            } else if (value instanceof Collection) {
                Collection<?> collection = (Collection<?>) value;
                for (Object item : collection) {
                    if (value instanceof Map) {
                        clearBlankValue((Map<String, Object>) item, clearNullValue, clearEmptyString);
                    }
                }
            }
        }
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
     * @param beanType JavaBean类型
     * @return Update对象
     */
    @Override
    public <T> DbUpdate parseParamsToDbUpdate(Map<String, String[]> params, Class<T> beanType) {
        VerifyTools.requireNonNull(params, "params");
        AllFieldColumn<?> allFields = DbTools.parseToAllFieldColumn(beanType);
        List<String> fieldNames = allFields.getFieldNames();
        Map<String, Object> map = parseMapWithWhitelist(params, fieldNames, null);
        return parseMapToDbUpdate(map, DbUpdate.class);
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
