package com.gitee.qdbp.jdbc.operator;

import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;

/**
 * 一元运算符, 如 IS NULL, IS NOT NULL
 *
 * @author zhaohuihua
 * @version 200123
 */
public interface DbUnaryOperator extends DbBaseOperator {

    /** 生成SQL片断 **/
    SqlBuffer buildSql(String columnName, SqlDialect dialect);
}
