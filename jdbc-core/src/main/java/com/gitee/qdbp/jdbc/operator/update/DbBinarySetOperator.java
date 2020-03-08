package com.gitee.qdbp.jdbc.operator.update;

import com.gitee.qdbp.jdbc.operator.DbBinaryOperator;
import com.gitee.qdbp.jdbc.operator.base.DbAbstractOperator;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;

/**
 * 二元UpdateSet运算符(UPDATE SET columnName=columnValue)
 *
 * @author zhaohuihua
 * @version 20200123
 */
public class DbBinarySetOperator extends DbAbstractOperator implements DbBinaryOperator {

    public DbBinarySetOperator() {
        super("Set", "=", "Equals");
    }

    @Override
    public SqlBuffer buildSql(String columnName, Object columnValue, SqlDialect dialect) {
        SqlBuffer buffer = new SqlBuffer();
        buffer.append(columnName, '=');
        if (columnValue == null || "".equals(columnValue)) {
            buffer.append("NULL");
        } else {
            buffer.addVariable(columnValue);
        }
        return buffer;
    }

}
