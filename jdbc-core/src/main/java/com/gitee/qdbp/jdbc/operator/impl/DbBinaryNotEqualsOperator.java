package com.gitee.qdbp.jdbc.operator.impl;

import com.gitee.qdbp.jdbc.operator.DbBinaryOperator;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;

/**
 * 二元不等号运算符
 *
 * @author zhaohuihua
 * @version 20200123
 */
public class DbBinaryNotEqualsOperator extends DbAbstractOperator implements DbBinaryOperator {

    public DbBinaryNotEqualsOperator() {
        super("!=", "Not Equals", "NotEquals");
    }

    @Override
    public SqlBuffer buildSql(String columnName, Object columnValue, SqlDialect dialect) {
        SqlBuffer buffer = new SqlBuffer();
        buffer.append(columnName);
        if (columnValue == null || "".equals(columnValue)) {
            buffer.append(' ').append("IS NOT NULL");
        } else {
            buffer.append("!=").addVariable(columnValue);
        }
        return buffer;
    }

}
