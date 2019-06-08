package com.gitee.qdbp.jdbc.model;

/**
 * 主键信息
 *
 * @author zhaohuihua
 * @version 190601
 */
public class PrimaryKey extends FieldColumn {

    /** SerialVersionUID **/
    private static final long serialVersionUID = 1L;

    /** 默认构造函数 **/
    public PrimaryKey() {
        super();
    }

    /** 构造函数 **/
    public PrimaryKey(String fieldName, String columnName) {
        super(fieldName, columnName);
    }

}
