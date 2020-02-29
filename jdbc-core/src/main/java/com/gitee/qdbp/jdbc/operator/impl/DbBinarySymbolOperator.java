package com.gitee.qdbp.jdbc.operator.impl;

import com.gitee.qdbp.jdbc.operator.DbBinaryOperator;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;

/**
 * 二元符号运算符, 如 = != &lt; &gt; &lt;= &gt;= 等
 *
 * @author zhaohuihua
 * @version 20200123
 */
public class DbBinarySymbolOperator extends DbAbstractOperator implements DbBinaryOperator {

    public DbBinarySymbolOperator(String operator, String... aliases) {
        super(operator, aliases);
    }

    @Override
    public SqlBuffer buildSql(String columnName, Object columnValue, SqlDialect dialect) {
        SqlBuffer buffer = new SqlBuffer();
        buffer.append(columnName);
        buffer.append(getType());
        buffer.addVariable(columnValue);
        return buffer;
    }

}
