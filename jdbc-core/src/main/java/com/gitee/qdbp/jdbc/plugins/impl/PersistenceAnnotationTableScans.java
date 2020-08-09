package com.gitee.qdbp.jdbc.plugins.impl;

import java.lang.reflect.Field;
import javax.persistence.Column;
import javax.persistence.Id;
import com.gitee.qdbp.able.matches.StringMatcher;
import com.gitee.qdbp.jdbc.model.PrimaryKeyFieldColumn;
import com.gitee.qdbp.jdbc.model.SimpleFieldColumn;
import com.gitee.qdbp.jdbc.plugins.NameConverter;
import com.gitee.qdbp.jdbc.plugins.TableNameScans;
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

    /** 默认构造函数 **/
    public PersistenceAnnotationTableScans() {
        this.setTableNameScans(new SimpleTableNameScans());
        this.setNameConverter(new SimpleNameConverter());
    }

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
        this.handleNameConverterAware();
    }

    /** 查找ID的处理器 **/
    public StringMatcher getPrimaryKeyMatcher() {
        return primaryKeyMatcher;
    }

    /** 查找ID的处理器 **/
    public void setPrimaryKeyMatcher(StringMatcher primaryKeyMatcher) {
        this.primaryKeyMatcher = primaryKeyMatcher;
    }

    /** 设置表名扫描类 **/
    public void setTableNameScans(TableNameScans tableNameScans) {
        super.setTableNameScans(tableNameScans);
        this.handleNameConverterAware();
    }

    /** 处理NameConverterAware **/
    protected void handleNameConverterAware() {
        TableNameScans tableNameScans = this.getTableNameScans();
        if (this.nameConverter != null && tableNameScans instanceof NameConverter.Aware) {
            ((NameConverter.Aware) tableNameScans).setNameConverter(this.nameConverter);
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
            SimpleFieldColumn column = new SimpleFieldColumn(fieldName, columnName);
            // 扫描@ColumnDefault注解声明的默认值
            scanColumnDefault(field, column);
            // 解析@Column注解中声明的信息
            if (annotation != null) {
                parseColumnAnnotation(column, annotation);
            }
            return column;
        }

        return null;
    }
    
    /** 解析@Column注解中声明的信息 **/
    protected void parseColumnAnnotation(SimpleFieldColumn column, Column annotation) {
        if (annotation != null) {
            // column.setColumnNullable(annotation.nullable());
            // column.setColumnInsertable(annotation.insertable());
            // column.setColumnUpdatable(annotation.updatable());
            // column.setColumnDefinition(annotation.columnDefinition());
            // column.setColumnLength(annotation.length());
            // column.setColumnPrecision(annotation.precision());
            // column.setColumnScale(annotation.scale());
            if (VerifyTools.isNotBlank(annotation.columnDefinition())) {
                parseColumnDefinition(column, annotation.columnDefinition());
            }
        }
    }

    /** 从列定义中解析列属性 **/
    // columnDefinition="Decimal(10,2) default 1.00"
    // columnDefinition="TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
    // columnDefinition="VARCHAR(20) DEFAULT 'N/A'" // 字段串要用单引号括起来
    protected void parseColumnDefinition(SimpleFieldColumn column, String columnDefinition) {
        // TODO 从列定义中解析列属性
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
