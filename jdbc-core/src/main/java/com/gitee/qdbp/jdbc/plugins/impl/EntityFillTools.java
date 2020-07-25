package com.gitee.qdbp.jdbc.plugins.impl;

import java.util.Map;
import com.gitee.qdbp.able.jdbc.condition.DbUpdate;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.jdbc.model.AllFieldColumn;
import com.gitee.qdbp.jdbc.model.SimpleFieldColumn;
import com.gitee.qdbp.tools.utils.ReflectTools;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 实体填充工具类
 *
 * @author zhaohuihua
 * @version 20200725
 */
public class EntityFillTools {

    /** 工具类, 禁止实例化 **/
    private EntityFillTools() {
    }

    /**
     * 如果指定字段(fieldName)不存在, 则在WHERE条件中增加字段值条件
     * 
     * @param where WHERE条件
     * @param fieldName 字段名
     * @param fieldvalue 字段值
     * @param allFields 操作对象的所有字段
     */
    public static void fillValueIfAbsent(DbWhere where, String fieldName, Object fieldValue,
            AllFieldColumn<?> allFields) {
        if (where == null || VerifyTools.isBlank(fieldName) || VerifyTools.isBlank(fieldValue)) {
            return;
        }
        if (allFields.containsByFieldName(fieldName) && !where.contains(fieldName)) {
            where.on(fieldName, "=", fieldValue);
        }
    }

    /**
     * 如果指定字段不存在, 则在WHERE条件中增加字段值条件
     * 
     * @param where WHERE条件
     * @param field 字段对象
     * @param fieldvalue 字段值
     * @param allFields 操作对象的所有字段
     */
    public static void fillValueIfAbsent(Map<String, Object> target, SimpleFieldColumn field, Object value) {
        if (target == null || field == null || VerifyTools.isBlank(value)) {
            return;
        }

        String fieldName = field.getFieldName();
        if (!target.containsKey(fieldName)) {
            target.put(fieldName, value);
        }
    }

    /**
     * 如果指定字段(fieldName)不存在, 则在target对象中增加字段值条件
     * 
     * @param target 目标对象
     * @param fieldName 字段名
     * @param fieldvalue 字段值
     * @param allFields 操作对象的所有字段
     */
    public static void fillValueIfAbsent(Map<String, Object> target, String fieldName, Object fieldValue,
            AllFieldColumn<?> allFields) {
        if (target == null || VerifyTools.isBlank(fieldName) || VerifyTools.isBlank(fieldValue)) {
            return;
        }

        if (allFields.containsByFieldName(fieldName) && !target.containsKey(fieldName)) {
            target.put(fieldName, fieldValue);
        }
    }

    /**
     * 如果指定字段(fieldName)不存在, 则在目标对象中增加字段值条件
     * 
     * @param ud 目标对象
     * @param fieldName 字段名
     * @param fieldvalue 字段值
     * @param allFields 操作对象的所有字段
     */
    public static void fillValueIfAbsent(DbUpdate ud, String fieldName, Object fieldValue, AllFieldColumn<?> allFields) {
        if (ud == null || VerifyTools.isBlank(fieldName) || VerifyTools.isBlank(fieldValue)) {
            return;
        }
        if (allFields.containsByFieldName(fieldName) && !ud.contains(fieldName)) {
            ud.set(fieldName, fieldValue);
        }
    }

    /**
     * 如果指定字段(fieldName)为空, 则在目标对象中增加字段值条件
     * 
     * @param target 目标对象
     * @param fieldName 字段名
     * @param fieldvalue 字段值
     * @param allFields 操作对象的所有字段
     */
    public static void setFieldValueIfAbsent(Object target, String fieldName, Object fieldvalue) {
        if (target == null || VerifyTools.isBlank(fieldName) || VerifyTools.isBlank(fieldvalue)) {
            return;
        }
        ReflectTools.setFieldValueIfAbsent(target, fieldName, fieldvalue, false);
    }
}
