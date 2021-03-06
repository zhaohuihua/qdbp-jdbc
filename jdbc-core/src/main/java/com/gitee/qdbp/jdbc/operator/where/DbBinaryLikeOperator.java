package com.gitee.qdbp.jdbc.operator.where;

import com.gitee.qdbp.jdbc.operator.DbBinaryOperator;
import com.gitee.qdbp.jdbc.operator.base.DbAbstractOperator;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.SqlBuilder;

/**
 * 二元Like运算符
 *
 * @author zhaohuihua
 * @version 20200123
 */
public class DbBinaryLikeOperator extends DbAbstractOperator implements DbBinaryOperator {

    /** 版本序列号 **/
    private static final long serialVersionUID = 1L;

    public DbBinaryLikeOperator() {
        super("Like");
    }

    @Override
    public SqlBuffer buildSql(String columnName, Object columnValue, SqlDialect dialect) {
        return new SqlBuilder().ad(columnName).ad(dialect.buildLikeSql(columnValue)).out();
    }

}
