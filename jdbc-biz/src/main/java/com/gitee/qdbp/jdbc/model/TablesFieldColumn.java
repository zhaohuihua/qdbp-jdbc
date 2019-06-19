package com.gitee.qdbp.jdbc.model;

import com.gitee.qdbp.able.jdbc.utils.FieldTools;
import com.gitee.qdbp.able.jdbc.utils.FieldTools.FieldItem;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 多个表的字段及列信息
 *
 * @author zhaohuihua
 * @version 180601
 */
public class TablesFieldColumn extends SimpleFieldColumn {

    /** SerialVersionUID **/
    private static final long serialVersionUID = 1L;

    /** 表别名 **/
    private String tableAlias;
    /** 列别名(用于重名字段) **/
    private String columnAlias;
    /** 数据保存至结果类的哪个字段(子对象) **/
    private String resultField;

    /** 默认构造函数 **/
    public TablesFieldColumn() {
    }

    /** 构造函数, fieldName或columnName带有表别名将会拆分并保存到tableAlias字段 **/
    public TablesFieldColumn(String fieldName, String columnName) {
        this.setFieldName(fieldName);
        this.setColumnName(columnName);
    }

    /** 构造函数, fieldName或columnName带有表别名将会拆分并保存到tableAlias字段 **/
    public TablesFieldColumn(String fieldName, String columnName, String resultField) {
        this.setFieldName(fieldName);
        this.setColumnName(columnName);
        this.setResultField(resultField);
    }

    /** 表别名 **/
    public String getTableAlias() {
        return tableAlias;
    }

    /** 表别名 **/
    public void setTableAlias(String tableAlias) {
        this.tableAlias = tableAlias;
    }

    /** 字段名(如果带有表别名, 会拆分并记录到tableAlias字段) **/
    public void setFieldName(String fieldName) {
        VerifyTools.requireNotBlank(fieldName, "fieldName");
        int dotIndex = fieldName.indexOf('.');
        if (dotIndex < 0) {
            super.setFieldName(fieldName);
        } else if (dotIndex > 0) {
            this.tableAlias = fieldName.substring(0, dotIndex);
            super.setFieldName(fieldName.substring(dotIndex + 1));
        } else {
            throw new IllegalArgumentException("FieldName can't be start with '.': " + fieldName);
        }
    }

    /** 数据表列名(如果带有表别名, 会拆分并记录到tableAlias字段) **/
    public void setColumnName(String columnName) {
        VerifyTools.requireNotBlank(columnName, "columnName");
        int dotIndex = columnName.indexOf('.');
        if (dotIndex < 0) {
            super.setColumnName(columnName);
        } else if (dotIndex > 0) {
            this.tableAlias = columnName.substring(0, dotIndex);
            super.setColumnName(columnName.substring(dotIndex + 1));
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

    /** 数据保存至结果类的哪个字段(子对象) **/
    public String getResultField() {
        return resultField;
    }

    /** 数据保存至结果类的哪个字段(子对象) **/
    public void setResultField(String resultField) {
        this.resultField = resultField;
    }

    /**
     * 与目标字段名是否匹配(字段名区分大小写, 别表名不区分大小写)<br>
     * 如果当前字段名或目标字段名没有表别名, 只要字段名匹配即为匹配<br>
     * 如果当前字段名和目标字段名都有表别名, 则需要表别名和字段名同时匹配
     * 
     * @param fieldName 目标字段名, 可带表别名, 如: u.userName
     * @return 是否匹配
     */
    public boolean matchesByFieldName(String fieldName) {
        return new FieldItem(this.getFieldName(), this.tableAlias).matches(FieldItem.parse(fieldName));
    }

    /**
     * 与目标列名是否匹配(不区分大小写)<br>
     * 如果当前列名或目标列名没有表别名, 只要列名匹配即为匹配<br>
     * 如果当前列名和目标列名都有表别名, 则需要表别名和列名同时匹配
     * 
     * @param columnName 目标列名, 可带表别名, 如: u.USER_NAME
     * @return 是否匹配
     */
    public boolean matchesByColumnName(String columnName) {
        // 列名比较不区分大小写
        String thisColumnName = this.getColumnName().toUpperCase();
        String thatColumnName = columnName.toUpperCase();
        return new FieldItem(thisColumnName, this.tableAlias).matches(FieldItem.parse(thatColumnName));
    }

    /**
     * 与目标列别名是否匹配(不区分大小写, 列别名不能带表别名)<br>
     * 优先与列别名比较, 如果没有列别名则与列名比较
     * 
     * @param columnAlias 目标列别名, 不能带表别名, 如: U_USER_NAME
     * @return 是否匹配
     */
    public boolean matchesByColumnAlias(String columnAlias) {
        if (VerifyTools.isBlank(this.columnAlias)) {
            return this.columnAlias.equalsIgnoreCase(columnAlias);
        } else {
            return this.getColumnName().equalsIgnoreCase(columnAlias);
        }
    }

    /** 返回带表别名的字段名 **/
    public String toTableFieldName() {
        return FieldTools.toTableFieldName(this.getFieldName(), tableAlias);
    }

    /** 返回带表别名的列名 **/
    public String toTableColumnName() {
        StringBuilder buffer = new StringBuilder();
        // 表别名
        if (VerifyTools.isNotBlank(tableAlias)) {
            buffer.append(tableAlias.toUpperCase()).append('.');
        }
        // 列名
        buffer.append(this.getColumnName());
        return buffer.toString();
    }

    /** 返回带表别名和列别名的完整列名 **/
    public String toFullColumnName() {
        StringBuilder buffer = new StringBuilder();
        // 表别名
        if (VerifyTools.isNotBlank(tableAlias)) {
            buffer.append(tableAlias.toUpperCase()).append('.');
        }
        // 列名
        buffer.append(this.getColumnName());
        // 列别名
        if (VerifyTools.isNotBlank(columnAlias)) {
            buffer.append(' ').append("AS").append(' ').append(columnAlias);
        }
        return buffer.toString();
    }

    /**
     * 将当前对象转换为子类对象
     *
     * @param clazz 目标类型
     * @return 目标对象
     */
    public <T extends SimpleFieldColumn> T to(Class<T> clazz) {
        T instance = super.to(clazz);
        if (instance instanceof TablesFieldColumn) {
            TablesFieldColumn real = (TablesFieldColumn) instance;
            real.setTableAlias(this.getTableAlias());
            real.setColumnAlias(this.getColumnAlias());
        }
        return instance;
    }

}
