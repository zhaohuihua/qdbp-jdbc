package com.gitee.qdbp.jdbc.plugins.impl;

import java.lang.reflect.Field;
import javax.persistence.Table;
import com.gitee.qdbp.able.matches.EqualsStringMatcher;
import com.gitee.qdbp.able.matches.StringMatcher;
import com.gitee.qdbp.jdbc.model.PrimaryKeyFieldColumn;
import com.gitee.qdbp.jdbc.model.SimpleFieldColumn;
import com.gitee.qdbp.jdbc.plugins.NameConverter;
import com.gitee.qdbp.tools.utils.VerifyTools;

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
    }

    @Override
    public String scanTableName(Class<?> clazz) {
        Table annotation = clazz.getAnnotation(Table.class);
        if (annotation != null && VerifyTools.isNotBlank(annotation.name())) {
            return annotation.name();
        } else {
            return nameConverter.beanNameToTableName(clazz.getSimpleName());
        }
    }

    @Override
    protected SimpleFieldColumn scanColumn(Field field, Class<?> clazz) {
        String fieldName = field.getName();
        return new SimpleFieldColumn(fieldName, nameConverter.fieldNameToColumnName(fieldName));
    }

    @Override
    protected PrimaryKeyFieldColumn scanPrimaryKey(Field field, SimpleFieldColumn column, Class<?> clazz) {
        String fieldName = field.getName();
        if (primaryKeyMatcher.matches(fieldName)) {
            if (column != null) {
                return column.to(PrimaryKeyFieldColumn.class);
            } else {
                return new PrimaryKeyFieldColumn(fieldName, nameConverter.fieldNameToColumnName(fieldName));
            }
        }
        return null;
    }
}
