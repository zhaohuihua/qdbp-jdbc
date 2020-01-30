package com.gitee.qdbp.jdbc.operator.impl;

import com.gitee.qdbp.jdbc.operator.DbBinaryOperator;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 二元UpdateSet运算符(UPDATE SET columnName=columnValue)
 *
 * @author zhaohuihua
 * @version 200123
 */
public class DbBinarySetOperator extends DbAbstractOperator implements DbBinaryOperator {

    public DbBinarySetOperator() {
        super("=", "Equals");
    }

    @Override
    public SqlBuffer buildSql(String columnName, Object columnValue, SqlDialect dialect) {
        SqlBuffer buffer = new SqlBuffer();
        buffer.append(columnName, '=');
        if (VerifyTools.isBlank(columnValue)) {
            buffer.append("NULL");
        } else {
            buffer.addVariable(columnValue);
        }
        return buffer;
    }

}
