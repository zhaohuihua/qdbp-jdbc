package com.gitee.qdbp.jdbc.operator.impl;

import com.gitee.qdbp.jdbc.operator.DbBinaryOperator;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;

/**
 * 二元EndsWith运算符
 *
 * @author zhaohuihua
 * @version 20200123
 */
public class DbBinaryEndsWithOperator extends DbAbstractOperator implements DbBinaryOperator {

    public DbBinaryEndsWithOperator() {
        super("Ends", "Ends With", "EndsWith");
    }

    @Override
    public SqlBuffer buildSql(String columnName, Object columnValue, SqlDialect dialect) {
        SqlBuffer buffer = new SqlBuffer();
        buffer.append(columnName, ' ');
        buffer.append(dialect.buildEndsWithSql(columnValue));
        return buffer;
    }

}
