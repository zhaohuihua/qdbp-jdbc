package com.gitee.qdbp.jdbc.plugins.impl;

import java.lang.reflect.Field;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import com.gitee.qdbp.able.matches.StringMatcher;
import com.gitee.qdbp.jdbc.model.PrimaryKeyFieldColumn;
import com.gitee.qdbp.jdbc.model.SimpleFieldColumn;
import com.gitee.qdbp.jdbc.plugins.NameConverter;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 扫描&#64;Table/&#64;Column/&#64;Id注解<br>
 * 如果useMissAnnotationField=true, 则只需要&#64;Table/&#64;Id注解
 *
 * @author zhaohuihua
 * @version 190601
 */
public class PersistenceAnnotationTableScans extends BaseTableInfoScans {

    /** 是否使用无注解的字段 **/
    private boolean useMissAnnotationField = false;
    /** 名称转换处理器 **/
    private NameConverter nameConverter;
    /** 查找主键字段的处理器(仅在useMissAnnotationField=true时使用) **/
    private StringMatcher primaryKeyMatcher;

    /** 是否使用无注解的字段 **/
    public boolean isUseMissAnnotationField() {
        return useMissAnnotationField;
    }

    /** 是否使用无注解的字段 **/
    public void setUseMissAnnotationField(boolean useMissAnnotationField) {
        this.useMissAnnotationField = useMissAnnotationField;
    }

    /** 名称转换处理器 **/
    public NameConverter getNameConverter() {
        return nameConverter;
    }

    /** 名称转换处理器 **/
    public void setNameConverter(NameConverter nameConverter) {
        this.nameConverter = nameConverter;
    }

    /** 查找ID的处理器 **/
    public StringMatcher getPrimaryKeyMatcher() {
        return primaryKeyMatcher;
    }

    /** 查找ID的处理器 **/
    public void setPrimaryKeyMatcher(StringMatcher primaryKeyMatcher) {
        this.primaryKeyMatcher = primaryKeyMatcher;
    }

    @Override
    public String scanTableName(Class<?> clazz) {
        Table annotation = clazz.getAnnotation(Table.class);
        if (annotation != null && VerifyTools.isNotBlank(annotation.name())) {
            return annotation.name();
        } else if (nameConverter == null) {
            return clazz.getSimpleName();
        } else {
            return nameConverter.beanNameToTableName(clazz.getSimpleName());
        }
    }

    @Override
    protected SimpleFieldColumn scanColumn(Field field, Class<?> clazz) {
        Column annotation = field.getAnnotation(Column.class);
        if (useMissAnnotationField || annotation != null) {
            String fieldName = field.getName();
            String columnName = annotation == null ? null : annotation.name();
            if (VerifyTools.isBlank(columnName)) {
                columnName = fieldName;
                if (nameConverter != null) {
                    columnName = nameConverter.fieldNameToColumnName(fieldName);
                }
            }
            return new SimpleFieldColumn(fieldName, columnName);
        }

        return null;
    }

    @Override
    protected PrimaryKeyFieldColumn scanPrimaryKey(Field field, SimpleFieldColumn column, Class<?> clazz) {
        String fieldName = field.getName();
        Id idAnnotation = field.getAnnotation(Id.class);
        boolean isPrimaryKey = idAnnotation != null
                || useMissAnnotationField && primaryKeyMatcher != null && primaryKeyMatcher.matches(fieldName);
        if (isPrimaryKey) {
            if (column != null) {
                return column.to(PrimaryKeyFieldColumn.class);
            } else {
                column = scanColumn(field, clazz);
                if (column != null) {
                    return column.to(PrimaryKeyFieldColumn.class);
                } else {
                    String columnName = fieldName;
                    if (nameConverter != null) {
                        columnName = nameConverter.fieldNameToColumnName(fieldName);
                    }
                    return new PrimaryKeyFieldColumn(fieldName, columnName);
                }
            }
        }
        return null;
    }
}
