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
        // ad=append, pd=prepend, var=addVariable
        SqlBuilder builder = new SqlBuilder();
        builder.ad("SELECT * FROM SYS_USER");
        builder.newline().tab(); // 缩进1次
        builder.ad("WHERE DEPT_CODE").ad('=').var("10001");
        builder.newline().tab(); // 在上次的基础上再缩进1次
        builder.ad("AND PHONE").ad(dialect.buildLikeSql("139%"));
        builder.ad("AND CREATE_USER").ad('=').var("zhh");
        builder.newline(); // 这里会继承前面的2次缩进
        builder.ad("AND CREATE_TIME", ">=").var(DateTools.parse("2019-01-01"));
        builder.newline(); // 继承2次缩进
        builder.ad("AND USER_STATE", "IN").ad('(').var(1).ad(',').var(2).ad(')');
        builder.ad("AND DATA_STATE").ad('=').var(0);
        builder.newline().tab(-2); // 回退2次缩进, 即回到顶格
        builder.ad("UNION");
        builder.newline(); // 继承0缩进
        builder.ad("SELECT * FROM SYS_USER");
        builder.newline().tab(); // 缩进1次
        builder.ad("WHERE DEPT_CODE").ad('=').var("10002");
        builder.ad("AND USER_STATE", "IN").ad('(').var(1).ad(',').var(2).ad(')');
        builder.ad("AND DATA_STATE").ad('=').var(0);

        printSqlString(builder.out(), dialect);
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
