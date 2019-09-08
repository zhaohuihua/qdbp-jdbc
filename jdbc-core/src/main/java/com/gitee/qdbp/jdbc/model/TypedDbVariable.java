package com.gitee.qdbp.jdbc.model;

/**
 * 指定SqlType的变量值: 用于where条件/insert值/update值
 *
 * @author zhaohuihua
 * @version 190824
 */
public class TypedDbVariable {

    /** SQL数据类型({@code java.sql.Types}) **/
    private Integer sqlType;
    /** 变量值 **/
    private Object value;

    /**
     * 构造函数
     * 
     * @param sqlType SQL数据类型({@code java.sql.Types})
     * @param value 变量值
     */
    public TypedDbVariable(int sqlType, Object value) {
        this.sqlType = sqlType;
        this.value = value;
    }

    /** SQL数据类型({@code java.sql.Types}) **/
    public Integer getSqlType() {
        return sqlType;
    }

    /** SQL数据类型({@code java.sql.Types}) **/
    public void setSqlType(Integer sqlType) {
        this.sqlType = sqlType;
    }

    /** 变量值 **/
    public Object getValue() {
        return value;
    }

    /** 变量值 **/
    public void setValue(Object value) {
        this.value = value;
    }

}
