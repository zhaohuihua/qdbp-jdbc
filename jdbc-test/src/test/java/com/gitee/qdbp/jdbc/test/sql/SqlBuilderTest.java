package com.gitee.qdbp.jdbc.test.sql;

import com.gitee.qdbp.jdbc.model.MainDbType;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuilder;
import com.gitee.qdbp.jdbc.utils.DbTools;
import com.gitee.qdbp.tools.utils.DateTools;

public class SqlBuilderTest {

    public static void main(String[] args) {
        SqlDialect dialect = DbTools.buildSqlDialect(MainDbType.Oracle);

        SqlBuilder buffer = new SqlBuilder();
        buffer.ad("AND CREATE_USER").ad('=').var("zhh");
        buffer.newline();
        buffer.ad("AND CREATE_TIME", ">=").var(DateTools.parse("2019-01-01"));

        SqlBuilder appendBuffer = new SqlBuilder();
        appendBuffer.ad("AND USER_STATE").ad('=').var(1);
        appendBuffer.ad("AND EFTFLAG IN").ad('(').var('E').ad(',').var('N').ad(')');

        SqlBuilder prependBuffer = new SqlBuilder();
        prependBuffer.ad("WHERE DEPT_CODE").ad('=').var("10001");
        prependBuffer.newline().tab();
        prependBuffer.ad("AND PHONE").ad(dialect.buildLikeSql("139%"));
        buffer.newline().ad(appendBuffer);
        buffer.pd(prependBuffer);

        SqlBuilder selectBuffer = new SqlBuilder();
        selectBuffer.ad("SELECT * FROM SYS_USER").newline().tab();
        buffer.pd(selectBuffer);

        System.out.println(buffer.end().getPreparedSqlString(dialect));
        System.out.println(buffer.end().toString());
        // String sql = buffer.end().getExecutableSqlString(dialect);
        // System.out.println(DbTools.formatSql(sql, 0));
    }
}
