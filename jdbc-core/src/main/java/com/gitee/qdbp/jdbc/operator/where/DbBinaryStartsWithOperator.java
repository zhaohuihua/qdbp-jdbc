package com.gitee.qdbp.jdbc.operator.where;

import com.gitee.qdbp.jdbc.operator.DbBinaryOperator;
import com.gitee.qdbp.jdbc.operator.base.DbAbstractOperator;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.SqlBuilder;

/**
 * 二元StartsWith运算符
 *
 * @author zhaohuihua
 * @version 20200123
 */
public class DbBinaryStartsWithOperator extends DbAbstractOperator implements DbBinaryOperator {

    public DbBinaryStartsWithOperator() {
        super("Starts", "Starts With", "StartsWith");
    }

    @Override
    public SqlBuffer buildSql(String columnName, Object columnValue, SqlDialect dialect) {
        return new SqlBuilder().ad(columnName).ad(dialect.buildStartsWithSql(columnValue)).out();
    }

}
