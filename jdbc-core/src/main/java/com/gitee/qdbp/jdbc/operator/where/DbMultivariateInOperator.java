package com.gitee.qdbp.jdbc.operator.where;

import java.util.Collection;
import com.gitee.qdbp.jdbc.operator.DbMultivariateOperator;
import com.gitee.qdbp.jdbc.operator.base.DbAbstractOperator;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.SqlTools;

/**
 * 多元IN运算符
 *
 * @author zhaohuihua
 * @version 20200123
 */
public class DbMultivariateInOperator extends DbAbstractOperator implements DbMultivariateOperator {

    /** 版本序列号 **/
    private static final long serialVersionUID = 1L;

    public DbMultivariateInOperator() {
        super("In");
    }

    @Override
    public SqlBuffer buildSql(String columnName, Collection<?> columnValues, SqlDialect dialect) {
        return SqlTools.buildInSql(columnName, columnValues, dialect);
    }

}
