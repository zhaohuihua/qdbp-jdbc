package com.gitee.qdbp.jdbc.operator;

import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;

/**
 * 二元运算符, 如 等于/不等于/大于/小于/LIKE/NOT LIKE等
 *
 * @author zhaohuihua
 * @version 20200123
 */
public interface DbBinaryOperator extends DbBaseOperator {

    /** 生成SQL片断 **/
    SqlBuffer buildSql(String columnName, Object columnValue, SqlDialect dialect);
}
