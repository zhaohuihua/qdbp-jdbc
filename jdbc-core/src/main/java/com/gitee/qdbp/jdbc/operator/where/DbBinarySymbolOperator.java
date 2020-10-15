package com.gitee.qdbp.jdbc.operator.where;

import com.gitee.qdbp.jdbc.operator.DbBinaryOperator;
import com.gitee.qdbp.jdbc.operator.base.DbAbstractOperator;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.SqlBuilder;

/**
 * 二元符号运算符, 如 = != &lt; &gt; &lt;= &gt;= 等
 *
 * @author zhaohuihua
 * @version 20200123
 */
public class DbBinarySymbolOperator extends DbAbstractOperator implements DbBinaryOperator {

    /** 版本序列号 **/
    private static final long serialVersionUID = 1L;

    public DbBinarySymbolOperator(String operator, String... aliases) {
        super(operator, aliases);
    }

    @Override
    public SqlBuffer buildSql(String columnName, Object columnValue, SqlDialect dialect) {
        return new SqlBuilder().ad(columnName).ad(getType()).var(columnValue).out();
    }

}
