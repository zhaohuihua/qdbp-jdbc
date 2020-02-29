package com.gitee.qdbp.jdbc.operator.impl;

import com.gitee.qdbp.jdbc.operator.DbTernaryOperator;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;

/**
 * 三元NotBetween运算符
 *
 * @author zhaohuihua
 * @version 20200208
 */
public class DbTernaryNotBetweenOperator extends DbAbstractOperator implements DbTernaryOperator {

    public DbTernaryNotBetweenOperator() {
        super("NotBetween");
    }

    @Override
    public SqlBuffer buildSql(String columnName, Object columnValue1, Object columnValue2, SqlDialect dialect) {
        SqlBuffer buffer = new SqlBuffer();
        buffer.append(columnName);
        buffer.append(' ', "NOT").append(' ', "BETWEEN", ' ');
        buffer.addVariable(columnValue1);
        buffer.append(' ', "AND", ' ');
        buffer.addVariable(columnValue2);
        return buffer;
    }

}
