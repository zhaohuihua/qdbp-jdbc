package com.gitee.qdbp.jdbc.operator.impl;

import com.gitee.qdbp.jdbc.operator.DbBinaryOperator;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;

/**
 * 二元Add运算符(UPDATE SET columnName=columnName+number)
 *
 * @author zhaohuihua
 * @version 200123
 */
public class DbBinaryAddOperator extends DbAbstractOperator implements DbBinaryOperator {

    public DbBinaryAddOperator() {
        super("Add");
    }

    @Override
    public SqlBuffer buildSql(String columnName, Object columnValue, SqlDialect dialect) {
        SqlBuffer buffer = new SqlBuffer();
        if (columnValue instanceof Number && ((Number) columnValue).doubleValue() < 0) {
            buffer.append(columnName).append('=');
            buffer.append(columnName).append('-');
            buffer.addVariable(columnValue);
        } else {
            buffer.append(columnName).append('=');
            buffer.append(columnName).append('+');
            buffer.addVariable(columnValue);
        }
        return buffer;
    }

}
