package com.gitee.qdbp.jdbc.operator.where;

import com.gitee.qdbp.jdbc.operator.DbUnaryOperator;
import com.gitee.qdbp.jdbc.operator.base.DbAbstractOperator;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.SqlBuilder;

/**
 * 一元IsNull运算符
 *
 * @author zhaohuihua
 * @version 20200123
 */
public class DbUnaryIsNullOperator extends DbAbstractOperator implements DbUnaryOperator {

    /** 版本序列号 **/
    private static final long serialVersionUID = 1L;

    public DbUnaryIsNullOperator() {
        super("Is Null", "IsNull");
    }

    @Override
    public SqlBuffer buildSql(String columnName, SqlDialect dialect) {
        return new SqlBuilder().ad(columnName).ad("IS NULL").out();
    }

}
