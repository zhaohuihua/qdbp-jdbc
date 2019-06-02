package com.gitee.qdbp.jdbc.model;

import java.io.Serializable;

/**
 * 字段及列信息
 *
 * @author zhaohuihua
 * @version 180601
 */
public class FieldColumn implements Serializable {

    /** SerialVersionUID **/
    private static final long serialVersionUID = 1L;

    /** 字段名 **/
    private String fieldName;
    /** 数据表列名 **/
    private String columnName;
    /** 描述文本 **/
    private String columnText;

    /** 默认构造函数 **/
    public FieldColumn() {
    }

    /** 构造函数 **/
    public FieldColumn(String fieldName, String columnName) {
        this.fieldName = fieldName;
        this.columnName = columnName;
    }

    /** 构造函数 **/
    public FieldColumn(String fieldName, String columnName, String columnText) {
        this.fieldName = fieldName;
        this.columnName = columnName;
        this.columnText = columnText;
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

    /** {@inheritDoc} **/
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append('{').append(fieldName).append(':').append(columnName).append('}');
        return buffer.toString();
    }
}
