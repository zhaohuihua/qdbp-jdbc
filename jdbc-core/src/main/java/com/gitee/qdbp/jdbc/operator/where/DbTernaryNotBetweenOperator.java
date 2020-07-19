package com.gitee.qdbp.jdbc.operator.where;

import com.gitee.qdbp.jdbc.operator.DbTernaryOperator;
import com.gitee.qdbp.jdbc.operator.base.DbAbstractOperator;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.SqlBuilder;

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
        return new SqlBuilder().ad(columnName).ad("NOT BETWEEN").var(columnValue1).ad("AND").var(columnValue2).out();
    }

}
