package com.gitee.qdbp.jdbc.operator;

import java.util.Collection;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;

/**
 * 多元运算符, 如 IN / NOT IN
 *
 * @author zhaohuihua
 * @version 20200123
 */
public interface DbMultivariateOperator extends DbBaseOperator {

    /** 生成SQL片断 **/
    SqlBuffer buildSql(String columnName, Collection<?> columnValues, SqlDialect dialect);
}
