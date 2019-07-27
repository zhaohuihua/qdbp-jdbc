package com.gitee.qdbp.jdbc.plugins.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import com.gitee.qdbp.jdbc.plugins.TableNameScans;

/**
 * 从静态字段名获取表名
 *
 * @author zhaohuihua
 * @version 190727
 */
public class StaticFieldTableNameScans implements TableNameScans {

    private String fieldName = "TABLE";

    /** 默认构造函数 **/
    public StaticFieldTableNameScans() {
    }

    /** 构造函数(静态字段名) **/
    public StaticFieldTableNameScans(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public String scanTableName(Class<?> clazz) {
        Field field;
        try {
            field = clazz.getField(fieldName);
            field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            String fmt = "TableNameScans, field '%s' not found in class[%s]";
            throw new IllegalStateException(String.format(fmt, fieldName, clazz.getSimpleName()));
        }

        if (!Modifier.isStatic(field.getModifiers())) {
            String fmt = "TableNameScans, '%s' isn't a static field in class[%s]";
            throw new IllegalStateException(String.format(fmt, fieldName, clazz.getSimpleName()));
        }
        if (!CharSequence.class.isAssignableFrom(field.getType())) {
            String fmt = "TableNameScans, '%s' return type isn't String in class[%s]";
            throw new IllegalStateException(String.format(fmt, fieldName, clazz.getSimpleName()));
        }
        try {
            Object fieldValue = field.get(clazz);
            if (fieldValue == null) {
                String fmt = "TableNameScans, static field '%s' value is blank in class[%s]";
                throw new IllegalStateException(String.format(fmt, fieldName, clazz.getSimpleName()));
            }
            return fieldValue.toString();
        } catch (IllegalArgumentException | IllegalAccessException e) {
            String fmt = "TableNameScans, failed to static field '%s' value in class[%s]";
            throw new IllegalStateException(String.format(fmt, fieldName, clazz.getSimpleName()), e);
        }
    }
}
