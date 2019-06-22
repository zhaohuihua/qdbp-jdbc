package com.gitee.qdbp.jdbc.exception;

import com.gitee.qdbp.able.result.IResultMessage;

public enum DbErrorCode implements IResultMessage {

    /** 主键字段未找到 **/
    DB_PRIMARY_KEY_FIELD_NOT_FOUND("主键字段未找到"),
    /** 主键值不能为空 **/
    DB_PRIMARY_KEY_VALUE_IS_REQUIRED("主键值不能为空"),
    /** 受影响记录数为0 **/
    DB_AFFECTED_ROWS_IS_ZERO("受影响记录数为0"),
    /** 该数据不支持逻辑删除 **/
    UNSUPPORTED_LOGICAL_DELETE("该数据不支持逻辑删除"),;

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