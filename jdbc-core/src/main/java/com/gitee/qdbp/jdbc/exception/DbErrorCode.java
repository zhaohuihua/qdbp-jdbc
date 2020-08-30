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
    DB_PRIMARY_KEY_FIELD_IS_UNRESOLVED("主键字段未找到"),
    /** 主键值不能为空 **/
    DB_PRIMARY_KEY_VALUE_IS_REQUIRED("主键值不能为空"),
    /** 实体类内容不能为空 **/
    DB_ENTITY_MUST_NOT_BE_EMPTY("实体类内容不能为空"),
    /** 不支持的实体类型 **/
    DB_UNSUPPORTED_ENTITY_TYPE("不支持的实体类型"),
    /** 不支持的WHERE类型 **/
    DB_UNSUPPORTED_WHERE_TYPE("不支持的WHERE类型"),
    /** WHERE条件内容不能为空 **/
    DB_WHERE_MUST_NOT_BE_EMPTY("WHERE条件内容不能为空"),
    /** 受影响记录数为0 **/
    DB_AFFECTED_ROWS_IS_ZERO("受影响记录数为0"),
    /** 参数有误, 未指定字段列表 **/
    DB_INCLUDE_FIELDS_IS_EMPTY("参数有误, 未指定字段列表"),
    /** 该数据不支持逻辑删除 **/
    DB_UNSUPPORTED_LOGICAL_DELETE("该数据不支持逻辑删除"),
    /** 数据源初始化失败 **/
    DB_DATA_SOURCE_INIT_ERROR("数据源初始化失败"),
    /** SQL片断不存在 **/
    DB_SQL_FRAGMENT_NOT_FOUND("SQL片断不存在"),
    /** SQL片断解析失败 **/
    DB_SQL_FRAGMENT_PARSE_ERROR("SQL片断解析失败"),
    /** SQL片断输出失败 **/
    DB_SQL_FRAGMENT_RENDER_ERROR("SQL片断输出失败");

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
