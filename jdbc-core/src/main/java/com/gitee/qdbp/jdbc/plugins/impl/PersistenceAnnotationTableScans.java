package com.gitee.qdbp.jdbc.plugins.impl;

import java.lang.reflect.Field;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Id;
import com.gitee.qdbp.able.matches.StringMatcher;
import com.gitee.qdbp.jdbc.model.SimpleFieldColumn;
import com.gitee.qdbp.jdbc.plugins.NameConverter;
import com.gitee.qdbp.jdbc.plugins.TableNameScans;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 扫描&#64;Table/&#64;Column/&#64;Id注解<br>
 * 没有&#64;Column注解的字段, 不会出现在新增/修改的字段列表中, 但会作为查询结果集映射字段<br>
 * 但如果所有字段都没有&#64;Column注解, 则将insertable/updatable全部设置为true<br>
 * 也就是说, 要么完全没有@Column注解; 只要出现了一个没有注解的字段, 则所有数据库对应字段都需要有注解<br>
 * 因为表关联查询所定义的结果类, 一般是不会有@Column字段的, 此时应认为是查询全部字段<br>
 *
 * @author zhaohuihua
 * @version 190601
 */
public class PersistenceAnnotationTableScans extends BaseTableInfoScans {

    /** 名称转换处理器 **/
    private NameConverter nameConverter;
    /** 查找主键字段的处理器 **/
    private StringMatcher primaryKeyMatcher;

    /** 默认构造函数 **/
    public PersistenceAnnotationTableScans() {
        this.setTableNameScans(new SimpleTableNameScans());
        this.setNameConverter(new SimpleNameConverter());
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
    protected void onAfterScanColumns(List<SimpleFieldColumn> allColumns) {
        super.onAfterScanColumns(allColumns);

        boolean existColumnAnnotation = false;
        for (SimpleFieldColumn item : allColumns) {
            if (item instanceof FieldColumn && ((FieldColumn) item).hasColumnAnnotation()) {
                existColumnAnnotation = true;
            }
        }

        // 如果所有字段都没有@Column注解, 则将insertable/updatable全部设置为true
        // 也就是说, 要么完全没有@Column注解; 只要出现了一个有注解的字段, 则所有数据库对应字段都需要有注解
        // 因为表关联查询所定义的结果类, 一般是不会有@Column字段的, 此时应认为是查询全部字段
        if (!existColumnAnnotation) {
            for (SimpleFieldColumn item : allColumns) {
                item.setColumnInsertable(true);
                item.setColumnUpdatable(true);
            }
        }
    }

    protected static class FieldColumn extends SimpleFieldColumn {

        /** serialVersionUID **/
        private static final long serialVersionUID = 1L;
        private boolean hasColumnAnnotation;

        protected FieldColumn() {
        }

        public FieldColumn(String fieldName, String columnName, Column annotation) {
            super(fieldName, columnName);
            this.hasColumnAnnotation = annotation != null;
            this.setColumnInsertable(annotation == null ? false : annotation.insertable());
            this.setColumnUpdatable(annotation == null ? false : annotation.updatable());
        }

        public boolean hasColumnAnnotation() {
            return hasColumnAnnotation;
        }

        @Override
        public FieldColumn copy() {
            FieldColumn instance = new FieldColumn();
            copyTo(instance);
            return instance;
        }

        protected void copyTo(SimpleFieldColumn instance) {
            super.copyTo(instance);
            if (instance instanceof FieldColumn) {
                FieldColumn real = (FieldColumn) instance;
                real.hasColumnAnnotation = this.hasColumnAnnotation;
            }
        }
    }

    @Override
    protected SimpleFieldColumn scanColumn(Field field, Class<?> clazz) {
        // 获取@Column注解
        Column annotation = field.getAnnotation(Column.class);
        String fieldName = field.getName();
        // 获取列名
        String columnName = annotation == null ? null : annotation.name();
        if (VerifyTools.isBlank(columnName)) {
            columnName = fieldName;
            if (nameConverter != null) {
                columnName = nameConverter.fieldNameToColumnName(fieldName);
            }
        }
        // 生成列信息对象
        FieldColumn column = new FieldColumn(fieldName, columnName, annotation);
        // 判断是不是主键
        scanPrimaryKey(column, field, clazz);
        // 扫描@ColumnDefault注解声明的默认值
        scanColumnDefault(field, column);
        // 解析@Column注解中声明的信息
        if (annotation != null) {
            parseColumnAnnotation(column, annotation);
        }
        return column;
    }

    protected void scanPrimaryKey(FieldColumn column, Field field, Class<?> clazz) {
        String fieldName = field.getName();
        Id idAnnotation = field.getAnnotation(Id.class);
        if (idAnnotation != null || primaryKeyMatcher != null && primaryKeyMatcher.matches(fieldName)) {
            column.setPrimaryKey(true);
        }
    }

    /** 解析@Column注解中声明的信息 **/
    protected void parseColumnAnnotation(SimpleFieldColumn column, Column annotation) {
        if (annotation == null) {
            return;
        }
        // column.setColumnNullable(annotation.nullable());
        // column.setColumnDefinition(annotation.columnDefinition());
        // column.setColumnLength(annotation.length());
        // column.setColumnPrecision(annotation.precision());
        // column.setColumnScale(annotation.scale());
        if (VerifyTools.isNotBlank(annotation.columnDefinition())) {
            parseColumnDefinition(column, annotation.columnDefinition());
        }
    }

    /** 从列定义中解析列属性 **/
    // columnDefinition="Decimal(10,2) default 1.00"
    // columnDefinition="TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
    // columnDefinition="VARCHAR(20) DEFAULT 'N/A'" // 字段串要用单引号括起来
    protected void parseColumnDefinition(SimpleFieldColumn column, String columnDefinition) {
        // TODO 从列定义中解析列属性
    }
}
