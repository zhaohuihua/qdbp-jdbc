package com.gitee.qdbp.jdbc.test.sql;

import com.gitee.qdbp.jdbc.model.MainDbType;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.SqlBuilder;
import com.gitee.qdbp.jdbc.utils.DbTools;
import com.gitee.qdbp.tools.utils.DateTools;

public class SqlBuilderTest {

    public static void main(String[] args) {
        SqlDialect dialect = DbTools.buildSqlDialect(MainDbType.Oracle);
        test1(dialect);
        test2(dialect);
    }

    private static void test1(SqlDialect dialect) {
        SqlBuilder buffer = new SqlBuilder();
        buffer.ad("SELECT * FROM SYS_USER");
        buffer.newline().tab();
        buffer.ad("WHERE DEPT_CODE").ad('=').var("10001");
        buffer.newline().tab();
        buffer.ad("AND PHONE").ad(dialect.buildLikeSql("139%"));
        buffer.ad("AND CREATE_USER").ad('=').var("zhh");
        buffer.newline();
        buffer.ad("AND CREATE_TIME", ">=").var(DateTools.parse("2019-01-01"));
        buffer.newline();
        buffer.ad("AND USER_STATE", "IN").ad('(').var(1).ad(',').var(2).ad(')');
        buffer.ad("AND DATA_STATE").ad('=').var(0);
        buffer.newline().tab(-2);
        buffer.ad("UNION");
        buffer.newline();
        buffer.ad("SELECT * FROM SYS_USER");
        buffer.newline().tab();
        buffer.ad("WHERE DEPT_CODE").ad('=').var("10001");
        buffer.ad("AND USER_STATE").ad('=').var(2);
        buffer.ad("AND EFTFLAG IN").ad('(').var('E').ad(',').var('N').ad(')');

        printSqlString(buffer.out(), dialect);
    }

    private static void test2(SqlDialect dialect) {
        SqlBuilder buffer = new SqlBuilder();
        buffer.ad("AND CREATE_USER").ad('=').var("zhh");
        buffer.newline();
        buffer.ad("AND CREATE_TIME", ">=").var(DateTools.parse("2019-01-01"));

        SqlBuilder appendBuffer = new SqlBuilder();
        appendBuffer.ad("AND USER_STATE IN").ad('(').var(1).ad(',').var(2).ad(')');
        appendBuffer.ad("AND EFTFLAG").ad('=').var(0);

        SqlBuilder prependBuffer = new SqlBuilder();
        prependBuffer.ad("WHERE DEPT_CODE").ad('=').var("10001");
        prependBuffer.newline().tab();
        prependBuffer.ad("AND PHONE").ad(dialect.buildLikeSql("139%"));
        buffer.newline().ad(appendBuffer);
        buffer.pd(prependBuffer);

        SqlBuilder selectBuffer = new SqlBuilder();
        selectBuffer.ad("SELECT * FROM SYS_USER").newline().tab();
        buffer.pd(selectBuffer);

        printSqlString(buffer.out(), dialect);
    }

    private static void printSqlString(SqlBuffer buffer, SqlDialect dialect) {
        System.out.println("/******************************************************\\");
        System.out.println("-- PreparedSqlString");
        System.out.println(buffer.getPreparedSqlString(dialect));
        System.out.println("--------------------------------------------------------");
        System.out.println("-- ExecutableSqlString");
        System.out.println(buffer.getExecutableSqlString(dialect));
        System.out.println("--------------------------------------------------------");
        System.out.println("-- LoggingSqlString");
        System.out.println(buffer.getLoggingSqlString(dialect));
        System.out.println("\\******************************************************/");
        System.out.println();
    }
}
