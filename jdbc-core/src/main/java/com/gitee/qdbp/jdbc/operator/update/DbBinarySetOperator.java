package com.gitee.qdbp.jdbc.operator.update;

import com.gitee.qdbp.jdbc.operator.DbBinaryOperator;
import com.gitee.qdbp.jdbc.operator.base.DbAbstractOperator;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.SqlBuilder;

/**
 * 二元UpdateSet运算符(UPDATE SET columnName=columnValue)
 *
 * @author zhaohuihua
 * @version 20200123
 */
public class DbBinarySetOperator extends DbAbstractOperator implements DbBinaryOperator {

    /** 版本序列号 **/
    private static final long serialVersionUID = 1L;

    public DbBinarySetOperator() {
        super("Set", "=", "Equals");
    }

    @Override
    public SqlBuffer buildSql(String columnName, Object columnValue, SqlDialect dialect) {
        SqlBuilder sql = new SqlBuilder();
        sql.ad(columnName).ad('=');
        if (columnValue == null || "".equals(columnValue)) {
            sql.var(null);
        } else {
            sql.var(columnValue);
        }
        return sql.out();
    }

}
