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

    /** 是否只读(作为全局缓存时设置为只读) **/
    protected boolean readonly;
    /** 字段名 **/
    private String fieldName;
    /** 数据表列名 **/
    private String columnName;
    /** 描述文本 **/
    private String columnText;
    /** Java字段类型 **/
    private Class<?> javaType;
    /** SQL数据类型({@code java.sql.Types}) **/
    private Integer sqlType;
    /** 是不是主键 **/
    private boolean primaryKey;
    /** 新增时是否使用此字段 **/
    private boolean columnInsertable = true;
    /** 修改时是否使用此字段 **/
    private boolean columnUpdatable = true;

    // /** Whether the column is a unique key. **/
    // private boolean columnUnique;
    // /** Whether the database column is nullable. **/
    // private boolean columnNullable = true;
    // /** The SQL fragment that is used when generating the DDL for the column. **/
    // private String columnDefinition;
    // /** The column length. (Applies only if a string-valued column is used.) **/
    // private int columnLength;
    // /** The precision for a decimal (exact numeric)  column. (Applies only if a decimal column is used.) **/
    // private int columnPrecision;
    // /** (Optional) The scale for a decimal (exact numeric) column. (Applies only if a decimal column is used.) **/
    // private int columnScale;
    /** 默认值 **/
    private Object columnDefault;

    /** 默认构造函数 **/
    public SimpleFieldColumn() {
    }

    /** 构造函数 **/
    public SimpleFieldColumn(String fieldName, String columnName) {
        this.setFieldName(fieldName);
        this.setColumnName(columnName);
    }

    protected void checkReadonly() {
        if (readonly) {
            throw new UnsupportedOperationException("read only");
        }
    }

    protected void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    /** 字段名 **/
    public String getFieldName() {
        return fieldName;
    }

    /** 字段名 **/
    public void setFieldName(String fieldName) {
        checkReadonly();
        this.fieldName = fieldName;
    }

    /** 数据表列名 **/
    public String getColumnName() {
        return columnName;
    }

    /** 数据表列名 **/
    public void setColumnName(String columnName) {
        checkReadonly();
        this.columnName = columnName;
    }

    /** 描述文本 **/
    public String getColumnText() {
        return columnText;
    }

    /** 描述文本 **/
    public void setColumnText(String columnText) {
        checkReadonly();
        this.columnText = columnText;
    }

    /** Java字段类型 **/
    public Class<?> getJavaType() {
        return javaType;
    }

    /** Java字段类型 **/
    public void setJavaType(Class<?> javaType) {
        checkReadonly();
        this.javaType = javaType;
    }

    /** SQL数据类型({@code java.sql.Types}) **/
    public Integer getSqlType() {
        return sqlType;
    }

    /** SQL数据类型({@code java.sql.Types}) **/
    public void setSqlType(Integer sqlType) {
        checkReadonly();
        this.sqlType = sqlType;
    }

    /** 是不是主键 **/
    public boolean isPrimaryKey() {
        return primaryKey;
    }

    /** 是不是主键 **/
    public void setPrimaryKey(boolean primaryKey) {
        checkReadonly();
        this.primaryKey = primaryKey;
    }

    /** 默认值 **/
    public Object getColumnDefault() {
        return columnDefault;
    }

    /** 默认值 **/
    public void setColumnDefault(Object columnDefault) {
        checkReadonly();
        this.columnDefault = columnDefault;
    }

    /** 新增时是否使用此字段 **/
    public boolean isColumnInsertable() {
        return columnInsertable;
    }

    /** 新增时是否使用此字段 **/
    public void setColumnInsertable(boolean columnInsertable) {
        checkReadonly();
        this.columnInsertable = columnInsertable;
    }

    /** 修改时是否使用此字段 **/
    public boolean isColumnUpdatable() {
        return columnUpdatable;
    }

    /** 修改时是否使用此字段 **/
    public void setColumnUpdatable(boolean columnUpdatable) {
        checkReadonly();
        this.columnUpdatable = columnUpdatable;
    }

    /**
     * 与目标字段是否匹配(区分大小写)
     * 
     * @param fieldName 指定的字段名
     * @return 是否匹配
     */
    public boolean matchesByFieldName(String fieldName) {
        return VerifyTools.equals(this.fieldName, fieldName);
    }

    /**
     * 与目标列名是否匹配(不区分大小写)
     * 
     * @param columnName 指定的列名
     * @return 是否匹配
     */
    public boolean matchesByColumnName(String columnName) {
        return VerifyTools.equals(this.columnName, columnName);
    }

    /**
     * 与目标列别名是否匹配(单表增删改查没有别名, 因此只与列名比较, 不区分大小写)
     * 
     * @param columnAlias 指定的列别名
     * @return 是否匹配
     */
    public boolean matchesByColumnAlias(String columnAlias) {
        return VerifyTools.equals(this.columnName, columnAlias);
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
        instance.setJavaType(this.getJavaType());
        instance.setSqlType(this.getSqlType());
        instance.setColumnInsertable(this.isColumnInsertable());
        instance.setColumnUpdatable(this.isColumnUpdatable());
        instance.setColumnDefault(this.getColumnDefault());
        // 副本不设置只读
        // instance.setReadonly(this.readonly);
        return instance;
    }

    /** {@inheritDoc} **/
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append('{').append(fieldName).append(':').append(columnName).append('}');
        return buffer.toString();
    }
}
