package com.gitee.qdbp.jdbc.operator.impl;

import com.gitee.qdbp.jdbc.operator.DbBinaryOperator;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;

/**
 * 二元Like运算符
 *
 * @author zhaohuihua
 * @version 200123
 */
public class DbBinaryLikeOperator extends DbAbstractOperator implements DbBinaryOperator {

    public DbBinaryLikeOperator() {
        super("Like");
    }

    @Override
    public SqlBuffer buildSql(String columnName, Object columnValue, SqlDialect dialect) {
        SqlBuffer buffer = new SqlBuffer();
        buffer.append(columnName, ' ');
        buffer.append(dialect.buildLikeSql(columnValue));
        return buffer;
    }

}
