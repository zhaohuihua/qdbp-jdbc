package com.gitee.qdbp.jdbc.operator.where;

import com.gitee.qdbp.jdbc.operator.DbUnaryOperator;
import com.gitee.qdbp.jdbc.operator.base.DbAbstractOperator;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;

/**
 * 一元IsNotNull运算符
 *
 * @author zhaohuihua
 * @version 20200123
 */
public class DbUnaryIsNotNullOperator extends DbAbstractOperator implements DbUnaryOperator {

    public DbUnaryIsNotNullOperator() {
        super("Is Not Null", "IsNotNull");
    }

    @Override
    public SqlBuffer buildSql(String columnName, SqlDialect dialect) {
        SqlBuffer buffer = new SqlBuffer();
        buffer.append(columnName).append(' ').append("IS NOT NULL");
        return buffer;
    }

}