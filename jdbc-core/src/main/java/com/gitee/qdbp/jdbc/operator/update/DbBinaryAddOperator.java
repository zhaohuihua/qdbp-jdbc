package com.gitee.qdbp.jdbc.operator.update;

import com.gitee.qdbp.jdbc.operator.DbBinaryOperator;
import com.gitee.qdbp.jdbc.operator.base.DbAbstractOperator;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.SqlBuilder;

/**
 * 二元Add运算符(UPDATE SET columnName=columnName+number)
 *
 * @author zhaohuihua
 * @version 20200123
 */
public class DbBinaryAddOperator extends DbAbstractOperator implements DbBinaryOperator {

    public DbBinaryAddOperator() {
        super("Add");
    }

    @Override
    public SqlBuffer buildSql(String columnName, Object columnValue, SqlDialect dialect) {
        return new SqlBuilder().ad(columnName).ad('=').ad(columnName).ad('+').var(columnValue).out();
    }

}
