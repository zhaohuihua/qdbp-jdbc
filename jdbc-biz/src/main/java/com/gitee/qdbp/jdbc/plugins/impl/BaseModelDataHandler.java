package com.gitee.qdbp.jdbc.plugins.impl;

import java.util.Map;
import com.gitee.qdbp.jdbc.condition.DbUpdate;
import com.gitee.qdbp.jdbc.condition.DbWhere;
import com.gitee.qdbp.jdbc.model.FieldColumn;
import com.gitee.qdbp.jdbc.plugins.ModelDataHandler;
import com.gitee.qdbp.tools.utils.ReflectTools;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 实体业务处理工具
 *
 * @author zhaohuihua
 * @version 190602
 */
public abstract class BaseModelDataHandler implements ModelDataHandler {

    protected boolean containsField(String fieldName, Map<String, String> fieldColumnMap) {
        if (VerifyTools.isBlank(fieldName)) {
            return false;
        }
        return fieldColumnMap.containsKey(fieldName);
    }

    protected void fillValueIfAbsent(Map<String, Object> target, FieldColumn field, Object value) {
        if (target == null || field == null || VerifyTools.isBlank(value)) {
            return;
        }

        String fieldName = field.getFieldName();
        if (!target.containsKey(fieldName)) {
            target.put(fieldName, value);
        }
    }

    protected void fillValueIfAbsent(Map<String, Object> target, String fieldName, Object value,
            Map<String, String> fieldColumnMap) {
        if (target == null || VerifyTools.isBlank(fieldName) || VerifyTools.isBlank(value)) {
            return;
        }

        if (fieldColumnMap.containsKey(fieldName) && !target.containsKey(fieldName)) {
            target.put(fieldName, value);
        }
    }

    protected void fillValueIfAbsent(DbWhere where, String fieldName, Object value,
            Map<String, String> fieldColumnMap) {
        if (where == null || VerifyTools.isBlank(fieldName) || VerifyTools.isBlank(value)) {
            return;
        }
        if (fieldColumnMap.containsKey(fieldName) && !where.contains(fieldName)) {
            where.on(fieldName, "=", value);
        }
    }

    protected void fillValueIfAbsent(DbUpdate ud, String fieldName, Object value, Map<String, String> fieldColumnMap) {
        if (ud == null || VerifyTools.isBlank(fieldName) || VerifyTools.isBlank(value)) {
            return;
        }
        if (fieldColumnMap.containsKey(fieldName) && !ud.contains(fieldName)) {
            ud.set(fieldName, value);
        }
    }

    protected void setFieldValueIfAbsent(Object target, String fieldName, Object value) {
        if (target == null || VerifyTools.isBlank(fieldName) || VerifyTools.isBlank(value)) {
            return;
        }
        ReflectTools.setFieldValueIfAbsent(target, fieldName, value, false);
    }

}
