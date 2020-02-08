package com.gitee.qdbp.jdbc.operator.impl;

import com.gitee.qdbp.jdbc.operator.DbBinaryOperator;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;

/**
 * 二元等号运算符
 *
 * @author zhaohuihua
 * @version 200123
 */
public class DbBinaryEqualsOperator extends DbAbstractOperator implements DbBinaryOperator {

    public DbBinaryEqualsOperator() {
        super("=", "Equals");
    }

    @Override
    public SqlBuffer buildSql(String columnName, Object columnValue, SqlDialect dialect) {
        SqlBuffer buffer = new SqlBuffer();
        buffer.append(columnName);
        if (columnValue == null || "".equals(columnValue)) {
            buffer.append(' ').append("IS NULL");
        } else {
            buffer.append('=').addVariable(columnValue);
        }
        return buffer;
    }

}
