package com.gitee.qdbp.jdbc.operator.impl;

import java.util.Collection;
import com.gitee.qdbp.jdbc.operator.DbMultivariateOperator;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.SqlTools;

/**
 * 多元IN运算符
 *
 * @author zhaohuihua
 * @version 20200123
 */
public class DbMultivariateInOperator extends DbAbstractOperator implements DbMultivariateOperator {

    public DbMultivariateInOperator() {
        super("In");
    }

    @Override
    public SqlBuffer buildSql(String columnName, Collection<?> columnValues, SqlDialect dialect) {
        SqlBuffer buffer = new SqlBuffer();
        buffer.append(columnName);
        buffer.append(' ');
        buffer.append(SqlTools.buildInSql(columnValues));
        return buffer;
    }

}
