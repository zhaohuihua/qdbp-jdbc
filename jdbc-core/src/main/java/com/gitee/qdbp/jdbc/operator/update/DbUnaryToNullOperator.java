package com.gitee.qdbp.jdbc.operator.update;

import com.gitee.qdbp.jdbc.operator.DbUnaryOperator;
import com.gitee.qdbp.jdbc.operator.base.DbAbstractOperator;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.SqlBuilder;

/**
 * 一元ToNull运算符(UPDATE SET columnName=NULL)
 *
 * @author zhaohuihua
 * @version 20200123
 */
public class DbUnaryToNullOperator extends DbAbstractOperator implements DbUnaryOperator {

    public DbUnaryToNullOperator() {
        super("To Null", "ToNull");
    }

    @Override
    public SqlBuffer buildSql(String columnName, SqlDialect dialect) {
        return new SqlBuilder().ad(columnName).ad('=').ad("NULL").out();
    }

}
