package com.gitee.qdbp.jdbc.operator;

import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;

/**
 * 三元运算符, 如 BETWEEN
 *
 * @author zhaohuihua
 * @version 200123
 */
public interface DbTernaryOperator extends DbBaseOperator {

    /** 生成SQL片断 **/
    SqlBuffer buildSql(String columnName, Object columnValue1, Object columnValue2, SqlDialect dialect);
}
