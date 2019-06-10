package com.gitee.qdbp.jdbc.model;

import java.io.Serializable;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 字段及列信息
 *
 * @author zhaohuihua
 * @version 180601
 */
public class SimpleFieldColumn implements Serializable {

    /** SerialVersionUID **/
    private static final long serialVersionUID = 1L;

    /** 字段名 **/
    private String fieldName;
    /** 数据表列名 **/
    private String columnName;
    /** 描述文本 **/
    private String columnText;

    /** 默认构造函数 **/
    public SimpleFieldColumn() {
    }

    /** 构造函数, fieldName或columnName带有表别名将会拆分并保存到tableAlias字段 **/
    public SimpleFieldColumn(String fieldName, String columnName) {
        this.setFieldName(fieldName);
        this.setColumnName(columnName);
    }

    /** 字段名 **/
    public String getFieldName() {
        return fieldName;
    }

    /** 字段名 **/
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    /** 数据表列名 **/
    public String getColumnName() {
        return columnName;
    }

    /** 数据表列名 **/
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    /** 描述文本 **/
    public String getColumnText() {
        return columnText;
    }

    /** 描述文本 **/
    public void setColumnText(String columnText) {
        this.columnText = columnText;
    }

    /**
     * 与目标字段是否匹配
     * 
     * @param fieldName 指定的字段名
     * @return 是否匹配
     */
    public boolean matchesByFieldName(String fieldName) {
        return VerifyTools.equals(this.fieldName, fieldName);
    }

    /** 返回表字段名 **/
    public String toTableFieldName() {
        return this.fieldName;
    }

    /** 返回表列名 **/
    public String toTableColumnName() {
        return this.columnName;
    }

    /** 返回完整列名 **/
    public String toFullColumnName() {
        return this.columnName;
    }

    /**
     * 将当前对象转换为子类对象
     *
     * @param clazz 目标类型
     * @return 目标对象
     */
    public <T extends SimpleFieldColumn> T to(Class<T> clazz) {
        T instance;
        try {
            instance = clazz.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to create " + clazz.getSimpleName() + " instance.", e);
        }

        instance.setFieldName(this.getFieldName());
        instance.setColumnName(this.getColumnName());
        instance.setColumnText(this.getColumnText());
        return instance;
    }

    /** {@inheritDoc} **/
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append('{').append(fieldName).append(':').append(columnName).append('}');
        return buffer.toString();
    }
}
