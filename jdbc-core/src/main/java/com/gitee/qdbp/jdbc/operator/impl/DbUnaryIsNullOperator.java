package com.gitee.qdbp.jdbc.operator.impl;

import com.gitee.qdbp.jdbc.operator.DbUnaryOperator;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;

/**
 * 一元IsNull运算符
 *
 * @author zhaohuihua
 * @version 200123
 */
public class DbUnaryIsNullOperator extends DbAbstractOperator implements DbUnaryOperator {

    public DbUnaryIsNullOperator() {
        super("Is Null", "IsNull");
    }

    @Override
    public SqlBuffer buildSql(String columnName, SqlDialect dialect) {
        SqlBuffer buffer = new SqlBuffer();
        buffer.append(columnName).append(' ').append("IS NULL");
        return buffer;
    }

}
