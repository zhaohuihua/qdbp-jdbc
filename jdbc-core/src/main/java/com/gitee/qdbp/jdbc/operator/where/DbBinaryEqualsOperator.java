package com.gitee.qdbp.jdbc.operator.where;

import com.gitee.qdbp.jdbc.operator.DbBinaryOperator;
import com.gitee.qdbp.jdbc.operator.base.DbAbstractOperator;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.SqlBuilder;

/**
 * 二元等号运算符
 *
 * @author zhaohuihua
 * @version 20200123
 */
public class DbBinaryEqualsOperator extends DbAbstractOperator implements DbBinaryOperator {

    /** 版本序列号 **/
    private static final long serialVersionUID = 1L;

    public DbBinaryEqualsOperator() {
        super("=", "Equals");
    }

    @Override
    public SqlBuffer buildSql(String columnName, Object columnValue, SqlDialect dialect) {
        SqlBuilder sql = new SqlBuilder();
        sql.ad(columnName);
        if (columnValue == null || "".equals(columnValue)) {
            sql.ad("IS NULL");
        } else {
            sql.ad('=').var(columnValue);
        }
        return sql.out();
    }

}
