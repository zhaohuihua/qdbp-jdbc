package com.gitee.qdbp.jdbc.plugins.impl;

import java.util.Map;
import com.gitee.qdbp.jdbc.condition.DbUpdate;
import com.gitee.qdbp.jdbc.condition.DbWhere;
import com.gitee.qdbp.jdbc.model.AllFieldColumn;
import com.gitee.qdbp.jdbc.model.SimpleFieldColumn;
import com.gitee.qdbp.jdbc.plugins.ModelDataHandler;
import com.gitee.qdbp.jdbc.utils.FieldTools;
import com.gitee.qdbp.tools.utils.ReflectTools;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 实体业务处理工具
 *
 * @author zhaohuihua
 * @version 190602
 */
public abstract class BaseModelDataHandler implements ModelDataHandler {

    protected void fillValueIfAbsent(DbWhere where, String fieldName, String tableAlias, Object value,
            AllFieldColumn<?> allFields) {
        if (where == null || VerifyTools.isBlank(fieldName) || VerifyTools.isBlank(value)) {
            return;
        }
        String fullFieldName = FieldTools.toTableFieldName(fieldName, tableAlias);
        if (allFields.containsByFieldName(fullFieldName) && !where.contains(fullFieldName)) {
            where.on(fullFieldName, "=", value);
        }
    }

    protected void fillValueIfAbsent(DbWhere where, String fieldName, Object value, AllFieldColumn<?> allFields) {
        if (where == null || VerifyTools.isBlank(fieldName) || VerifyTools.isBlank(value)) {
            return;
        }
        if (allFields.containsByFieldName(fieldName) && !where.contains(fieldName)) {
            where.on(fieldName, "=", value);
        }
    }

    protected void fillValueIfAbsent(Map<String, Object> target, SimpleFieldColumn field, Object value) {
        if (target == null || field == null || VerifyTools.isBlank(value)) {
            return;
        }

        String fieldName = field.getFieldName();
        if (!target.containsKey(fieldName)) {
            target.put(fieldName, value);
        }
    }

    protected void fillValueIfAbsent(Map<String, Object> target, String fieldName, Object value,
            AllFieldColumn<?> allFields) {
        if (target == null || VerifyTools.isBlank(fieldName) || VerifyTools.isBlank(value)) {
            return;
        }

        if (allFields.containsByFieldName(fieldName) && !target.containsKey(fieldName)) {
            target.put(fieldName, value);
        }
    }

    protected void fillValueIfAbsent(DbUpdate ud, String fieldName, Object value, AllFieldColumn<?> allFields) {
        if (ud == null || VerifyTools.isBlank(fieldName) || VerifyTools.isBlank(value)) {
            return;
        }
        if (allFields.containsByFieldName(fieldName) && !ud.contains(fieldName)) {
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
