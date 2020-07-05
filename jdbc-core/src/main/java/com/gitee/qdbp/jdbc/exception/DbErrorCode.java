package com.gitee.qdbp.jdbc.exception;

import com.gitee.qdbp.able.result.IResultMessage;

/**
 * 数据库错误码
 *
 * @author zhaohuihua
 * @version 20200130
 */
public enum DbErrorCode implements IResultMessage {

    /** 主键字段未找到 **/
    DB_PRIMARY_KEY_FIELD_NOT_FOUND("主键字段未找到"),
    /** 主键值不能为空 **/
    DB_PRIMARY_KEY_VALUE_IS_REQUIRED("主键值不能为空"),
    /** 实体类内容不能为空 **/
    DB_ENTITY_MUST_NOT_BE_EMPTY("实体类内容不能为空"),
    /** WHERE条件内容不能为空 **/
    DB_WHERE_MUST_NOT_BE_EMPTY("WHERE条件内容不能为空"),
    /** 受影响记录数为0 **/
    DB_AFFECTED_ROWS_IS_ZERO("受影响记录数为0"),
    /** 参数有误, 未指定字段列表 **/
    DB_INCLUDE_FIELDS_IS_EMPTY("参数有误, 未指定字段列表"),
    /** 该数据不支持逻辑删除 **/
    DB_UNSUPPORTED_LOGICAL_DELETE("该数据不支持逻辑删除");

    /** 错误描述 **/
    private final String message;

    /**
     * 构造函数
     *
     * @param message 错误描述
     */
    private DbErrorCode(String message) {
        this.message = message;
    }

    /** {@inheritDoc} **/
    @Override
    public String getCode() {
        return this.name();
    }

    /** {@inheritDoc} **/
    @Override
    public String getMessage() {
        return message;
    }

}
