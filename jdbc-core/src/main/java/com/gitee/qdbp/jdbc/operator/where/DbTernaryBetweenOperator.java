package com.gitee.qdbp.jdbc.operator.where;

import com.gitee.qdbp.jdbc.operator.DbTernaryOperator;
import com.gitee.qdbp.jdbc.operator.base.DbAbstractOperator;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;

/**
 * 三元Between运算符
 *
 * @author zhaohuihua
 * @version 20200123
 */
public class DbTernaryBetweenOperator extends DbAbstractOperator implements DbTernaryOperator {

    public DbTernaryBetweenOperator() {
        super("Between");
    }

    @Override
    public SqlBuffer buildSql(String columnName, Object columnValue1, Object columnValue2, SqlDialect dialect) {
        SqlBuffer buffer = new SqlBuffer();
        buffer.append(columnName);
        buffer.append(' ', "BETWEEN", ' ');
        buffer.addVariable(columnValue1);
        buffer.append(' ', "AND", ' ');
        buffer.addVariable(columnValue2);
        return buffer;
    }

}
