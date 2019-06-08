package com.gitee.qdbp.jdbc.model;

import java.io.Serializable;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 字段及列信息
 *
 * @author zhaohuihua
 * @version 180601
 */
public class FieldColumn implements Serializable {

    /** SerialVersionUID **/
    private static final long serialVersionUID = 1L;

    /** 表别名 **/
    private String tableAlias;
    /** 字段名 **/
    private String fieldName;
    /** 数据表列名 **/
    private String columnName;
    /** 列别名(用于重名字段) **/
    private String columnAlias;
    /** 描述文本 **/
    private String columnText;

    /** 默认构造函数 **/
    public FieldColumn() {
    }

    /** 构造函数, fieldName或columnName带有表别名将会拆分并保存到tableAlias字段 **/
    public FieldColumn(String fieldName, String columnName) {
        this.setFieldName(fieldName);
        this.setColumnName(columnName);
    }

    /** 表别名 **/
    public String getTableAlias() {
        return tableAlias;
    }

    /** 表别名 **/
    public void setTableAlias(String tableAlias) {
        this.tableAlias = tableAlias;
    }

    /** 字段名 **/
    public String getFieldName() {
        return fieldName;
    }

    /** 字段名(如果带有表别名, 会拆分并记录到tableAlias字段) **/
    public void setFieldName(String fieldName) {
        VerifyTools.requireNotBlank(fieldName, "fieldName");
        int dotIndex = fieldName.indexOf('.');
        if (dotIndex < 0) {
            this.fieldName = fieldName;
        } else if (dotIndex > 0) {
            this.tableAlias = fieldName.substring(0, dotIndex);
            this.fieldName = fieldName.substring(dotIndex + 1);
        } else {
            throw new IllegalArgumentException("FieldName can't be start with '.': " + fieldName);
        }
    }

    /** 数据表列名 **/
    public String getColumnName() {
        return columnName;
    }

    /** 数据表列名(如果带有表别名, 会拆分并记录到tableAlias字段) **/
    public void setColumnName(String columnName) {
        VerifyTools.requireNotBlank(columnName, "columnName");
        int dotIndex = columnName.indexOf('.');
        if (dotIndex < 0) {
            this.columnName = columnName;
        } else if (dotIndex > 0) {
            this.tableAlias = columnName.substring(0, dotIndex);
            this.columnName = columnName.substring(dotIndex + 1);
        } else {
            throw new IllegalArgumentException("ColumnName can't be start with '.': " + columnName);
        }
    }

    /** 列别名 **/
    public String getColumnAlias() {
        return columnAlias;
    }

    /** 列别名 **/
    public void setColumnAlias(String columnAlias) {
        this.columnAlias = columnAlias;
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
     * 与指定的字段是否匹配<br>
     * 如果未指定表别名或该FieldColumn没有表别名, 只要字段名匹配即为匹配<br>
     * 如果指定了表别名且该FieldColumn有表别名, 则需要表别名和字段名同时匹配
     * 
     * @param fieldName 指定的字段名, 可带表别名, 如: u.userName
     * @return 是否匹配
     */
    public boolean matchesWithField(String fieldName) {
        String tableAlias = null;
        String realName = fieldName;
        int dotIndex = fieldName.indexOf('.');
        if (dotIndex > 0) {
            tableAlias = fieldName.substring(0, dotIndex);
            realName = fieldName.substring(dotIndex + 1);
        } else if (dotIndex == 0) {
            realName = fieldName.substring(dotIndex + 1);
        }
        return matchesWithField(realName, tableAlias);
    }

    /**
     * 与指定的字段是否匹配<br>
     * 如果未指定表别名或该FieldColumn没有表别名, 只要字段名匹配即为匹配<br>
     * 如果指定了表别名且该FieldColumn有表别名, 则需要表别名和字段名同时匹配
     * 
     * @param fieldName 指定的字段名, required
     * @param tableAlias 指定的表别名, optional
     * @return 是否匹配
     */
    public boolean matchesWithField(String fieldName, String tableAlias) {
        if (VerifyTools.isBlank(tableAlias) || VerifyTools.isBlank(this.tableAlias)) {
            return fieldName.equals(this.fieldName);
        } else {
            return tableAlias.equals(this.tableAlias) && fieldName.equals(this.fieldName);
        }
    }

    /**
     * 将当前对象转换为子类对象
     *
     * @param clazz 目标类型
     * @return 目标对象
     */
    public <T extends FieldColumn> T to(Class<T> clazz) {
        T instance;
        try {
            instance = clazz.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to create " + clazz.getSimpleName() + " instance.", e);
        }

        instance.setTableAlias(this.getTableAlias());
        instance.setFieldName(this.getFieldName());
        instance.setColumnName(this.getColumnName());
        instance.setColumnAlias(this.getColumnAlias());
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
