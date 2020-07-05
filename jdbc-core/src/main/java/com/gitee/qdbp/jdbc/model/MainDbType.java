package com.gitee.qdbp.jdbc.model;

/**
 * 常见的数据库类型
 *
 * @author zhaohuihua
 * @version 200705
 */
public enum MainDbType implements DbType {
    /** 未知 **/
    Unknown,
    /** Oracle **/
    Oracle,
    /** MySQL **/
    MySQL,
    /** DB2 **/
    DB2,
    /** H2 **/
    H2,
    /** PostgreSQL **/
    PostgreSQL,
    /** SqlServer **/
    SqlServer,
    /** Derby **/
    Derby,
    /** HyperSQL **/
    HyperSQL,
    /** Informix **/
    Informix,
    /** Sybase **/
    Sybase
}
