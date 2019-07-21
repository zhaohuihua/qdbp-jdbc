package com.gitee.qdbp.jdbc.test.sql;

import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.utils.DbTools;
import com.gitee.qdbp.tools.utils.DateTools;

public class SqlBufferTest {

    public static void main(String[] args) {
        SqlDialect dialect = DbTools.getSqlDialect();

        SqlBuffer buffer = new SqlBuffer();
        buffer.append(" AND CREATE_USER = ").addVariable("zhh");
        buffer.append(" AND CREATE_TIME >= ").addVariable(DateTools.parse("2019-01-01"));

        SqlBuffer appendBuffer = new SqlBuffer();
        appendBuffer.append(" AND USER_STATE = ").addVariable(1);
        appendBuffer.append(" AND EFTFLAG IN (").addVariable('E').append(',').addVariable('N').append(')');

        SqlBuffer prependBuffer = new SqlBuffer();
        prependBuffer.append(" WHERE DEPT_CODE = ").addVariable("10001");
        prependBuffer.append(" AND PHONE ").append(dialect.buildLikeSql("139%"));
        buffer.append(appendBuffer);
        buffer.prepend(prependBuffer);

        buffer.prepend("SELECT * FROM SYS_USER ");

        System.out.println(buffer.getPreparedSqlString());
        System.out.println(buffer.toString());
        System.out.println(DbTools.formatSql(buffer, 0));
    }
}
