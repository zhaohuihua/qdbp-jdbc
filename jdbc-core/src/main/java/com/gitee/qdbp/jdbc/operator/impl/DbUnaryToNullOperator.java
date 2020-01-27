package com.gitee.qdbp.jdbc.operator.impl;

import com.gitee.qdbp.jdbc.operator.DbUnaryOperator;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;

/**
 * 一元ToNull运算符(UPDATE SET columnName=NULL)
 *
 * @author zhaohuihua
 * @version 200123
 */
public class DbUnaryToNullOperator extends DbAbstractOperator implements DbUnaryOperator {

    public DbUnaryToNullOperator() {
        super("To Null", "ToNull");
    }

    @Override
    public SqlBuffer buildSql(String columnName, SqlDialect dialect) {
        SqlBuffer buffer = new SqlBuffer();
        buffer.append(columnName).append('=').append("NULL");
        return buffer;
    }

}
