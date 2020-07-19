package com.gitee.qdbp.jdbc.test.sql;

import com.gitee.qdbp.jdbc.model.MainDbType;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.utils.DbTools;
import com.gitee.qdbp.tools.utils.DateTools;

public class SqlBufferTest {

    public static void main(String[] args) {
        SqlDialect dialect = DbTools.buildSqlDialect(MainDbType.Oracle);
        test1(dialect);
        test2(dialect);
    }

    private static void test1(SqlDialect dialect) {
        SqlBuffer buffer = new SqlBuffer();
        buffer.append("SELECT * FROM SYS_USER");
        buffer.append(" WHERE DEPT_CODE = ").addVariable("10001");
        buffer.append(" AND PHONE ").append(dialect.buildLikeSql("139%"));
        buffer.append(" AND CREATE_USER = ").addVariable("zhh");
        buffer.append(" AND CREATE_TIME >= ").addVariable(DateTools.parse("2019-01-01"));
        buffer.append(" AND USER_STATE IN (").addVariable(1).append(',').addVariable(2).append(')');
        buffer.append(" AND DATA_STATE = ").addVariable(0);
        printSqlString(buffer, dialect);
    }

    private static void test2(SqlDialect dialect) {
        SqlBuffer buffer = new SqlBuffer();
        buffer.append(" AND CREATE_USER = ").addVariable("zhh");
        buffer.append(" AND CREATE_TIME >= ").addVariable(DateTools.parse("2019-01-01"));

        SqlBuffer appendBuffer = new SqlBuffer();
        appendBuffer.append(" AND USER_STATE IN (").addVariable(1).append(',').addVariable(2).append(')');
        appendBuffer.append(" AND DATA_STATE = ").addVariable(0);

        SqlBuffer prependBuffer = new SqlBuffer();
        prependBuffer.append(" WHERE DEPT_CODE = ").addVariable("10001");
        prependBuffer.append(" AND PHONE ").append(dialect.buildLikeSql("139%"));
        buffer.append(appendBuffer);
        buffer.prepend(prependBuffer);

        buffer.prepend("SELECT * FROM SYS_USER");

        printSqlString(buffer, dialect);
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
