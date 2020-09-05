package com.gitee.qdbp.jdbc.plugins.impl;

import java.lang.reflect.Field;
import com.gitee.qdbp.able.matches.EqualsStringMatcher;
import com.gitee.qdbp.able.matches.StringMatcher;
import com.gitee.qdbp.jdbc.model.SimpleFieldColumn;
import com.gitee.qdbp.jdbc.plugins.NameConverter;
import com.gitee.qdbp.jdbc.plugins.TableNameScans;

/**
 * 提取全部字段, 而不是扫描注解
 *
 * @author zhaohuihua
 * @version 190601
 */
public class SimpleTableInfoScans extends BaseTableInfoScans {

    /** 查找ID的处理器 **/
    private StringMatcher primaryKeyMatcher = new EqualsStringMatcher("id");
    /** 名称转换处理器 **/
    private NameConverter nameConverter = new SimpleNameConverter();

    /** 默认构造函数 **/
    public SimpleTableInfoScans() {
        this.setTableNameScans(new SimpleTableNameScans());
        this.setNameConverter(new SimpleNameConverter());
    }

    /** 查找主键字段的处理器 **/
    public StringMatcher getPrimaryKeyMatcher() {
        return primaryKeyMatcher;
    }

    /** 查找主键字段的处理器 **/
    public void setPrimaryKeyMatcher(StringMatcher primaryKeyMatcher) {
        this.primaryKeyMatcher = primaryKeyMatcher;
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
        // 获取字段名
        String fieldName = field.getName();
        // 获取列名
        String columnName = nameConverter.fieldNameToColumnName(fieldName);
        // 生成列信息对象
        SimpleFieldColumn column = new SimpleFieldColumn(fieldName, columnName);
        // 判断是不是主键
        scanPrimaryKey(column, field, clazz);
        // 扫描@ColumnDefault注解声明的默认值
        scanColumnDefault(field, column);
        return column;
    }

    protected void scanPrimaryKey(SimpleFieldColumn column, Field field, Class<?> clazz) {
        String fieldName = field.getName();
        if (primaryKeyMatcher.matches(fieldName)) {
            column.setPrimaryKey(true);
        }
    }
}
