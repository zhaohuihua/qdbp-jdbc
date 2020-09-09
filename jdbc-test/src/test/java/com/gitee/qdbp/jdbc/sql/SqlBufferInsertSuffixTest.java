package com.gitee.qdbp.jdbc.sql;

import org.testng.Assert;
import org.testng.annotations.Test;
import com.gitee.qdbp.jdbc.model.MainDbType;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.utils.DbTools;
import com.gitee.qdbp.tools.utils.StringTools;

@Test
public class SqlBufferInsertSuffixTest {

    private SqlDialect dialect = DbTools.buildSqlDialect(MainDbType.MySQL);

    @Test
    public void testInsertSuffix11() {
        // testInsertSuffix11("", new SqlBuffer("STATE=").addVariable(1).append(" CREATOR AND"));
        // testInsertSuffix11("", new SqlBuffer("STATE=").addVariable(1).append(" CREATOR"));
        testInsertSuffix11("\n", new SqlBuffer("STATE=").addVariable(1).append(" CREATOR AND\n"));
        testInsertSuffix11("\n", new SqlBuffer("STATE=").addVariable(1).append(" CREATOR\n"));
        testInsertSuffix11("\n\t", new SqlBuffer("STATE=").addVariable(1).append(" CREATOR AND\n\t"));
        testInsertSuffix11("\n\t", new SqlBuffer("STATE=").addVariable(1).append(" CREATOR\n\t"));
        testInsertSuffix11("    \n    ", new SqlBuffer("STATE=").addVariable(1).append(" CREATOR AND    \n    "));
        testInsertSuffix11("    \n    ", new SqlBuffer("STATE=").addVariable(1).append(" CREATOR    \n    "));
    }

    private void testInsertSuffix11(String space, SqlBuffer sql) {
        testInsertSuffix(sql.copy(), "STATE=1 CREATOR TEST" + space, "TEST", "AND");
        testInsertSuffix(sql.copy(), "STATE=1 CREATOR TEST" + space, "TEST", "and");
        testInsertSuffix(sql.copy(), "STATE=1 CREATOR TEST" + space, "TEST", " AND ");
        testInsertSuffix(sql.copy(), "STATE=1 CREATOR TEST" + space, "TEST", " and ");
        testInsertSuffix(sql.copy(), "STATE=1 CREATOR TEST" + space, "TEST", "AND|OR");
        testInsertSuffix(sql.copy(), "STATE=1 CREATOR TEST" + space, "TEST", " AND|OR ");
        testInsertSuffix(sql.copy(), "STATE=1 CREATOR TEST" + space, "TEST", "and|or");
        testInsertSuffix(sql.copy(), "STATE=1 CREATOR TEST" + space, "TEST", " and | or ");
    }

    @Test
    public void testInsertSuffix12() {
        testInsertSuffix12("", new SqlBuffer("STATE=").addVariable(1).append(" CREATOR OR"));
        testInsertSuffix12("", new SqlBuffer("STATE=").addVariable(1).append(" CREATOR"));
        testInsertSuffix12("\n", new SqlBuffer("STATE=").addVariable(1).append(" CREATOR OR\n"));
        testInsertSuffix12("\n", new SqlBuffer("STATE=").addVariable(1).append(" CREATOR\n"));
        testInsertSuffix12("\n\t", new SqlBuffer("STATE=").addVariable(1).append(" CREATOR OR\n\t"));
        testInsertSuffix12("\n\t", new SqlBuffer("STATE=").addVariable(1).append(" CREATOR\n\t"));
        testInsertSuffix12("    \n    ", new SqlBuffer("STATE=").addVariable(1).append(" CREATOR OR    \n    "));
        testInsertSuffix12("    \n    ", new SqlBuffer("STATE=").addVariable(1).append(" CREATOR    \n    "));
    }

    private void testInsertSuffix12(String space, SqlBuffer sql) {
        testInsertSuffix(sql.copy(), "STATE=1 CREATOR TEST" + space, "TEST", "OR");
        testInsertSuffix(sql.copy(), "STATE=1 CREATOR TEST" + space, "TEST", "or");
        testInsertSuffix(sql.copy(), "STATE=1 CREATOR TEST" + space, "TEST", " OR ");
        testInsertSuffix(sql.copy(), "STATE=1 CREATOR TEST" + space, "TEST", " or ");
        testInsertSuffix(sql.copy(), "STATE=1 CREATOR TEST" + space, "TEST", "AND|OR");
        testInsertSuffix(sql.copy(), "STATE=1 CREATOR TEST" + space, "TEST", " AND|OR ");
        testInsertSuffix(sql.copy(), "STATE=1 CREATOR TEST" + space, "TEST", "and|or");
        testInsertSuffix(sql.copy(), "STATE=1 CREATOR TEST" + space, "TEST", " and | or ");
    }

    @Test
    public void testInsertSuffix13() {
        testInsertSuffix13("", new SqlBuffer("STATE=").addVariable(1).append(" CREATOR OR"));
        testInsertSuffix13("", new SqlBuffer("STATE=").addVariable(1).append(" CREATOR"));
        testInsertSuffix13("\n", new SqlBuffer("STATE=").addVariable(1).append(" CREATOR OR\n"));
        testInsertSuffix13("\n", new SqlBuffer("STATE=").addVariable(1).append(" CREATOR\n"));
        testInsertSuffix13("\n\t", new SqlBuffer("STATE=").addVariable(1).append(" CREATOR OR\n\t"));
        testInsertSuffix13("\n\t", new SqlBuffer("STATE=").addVariable(1).append(" CREATOR\n\t"));
        testInsertSuffix13("    \n    ", new SqlBuffer("STATE=").addVariable(1).append(" CREATOR OR    \n    "));
        testInsertSuffix13("    \n    ", new SqlBuffer("STATE=").addVariable(1).append(" CREATOR    \n    "));
    }

    private void testInsertSuffix13(String space, SqlBuffer sql) {
        testInsertSuffix(sql.copy(), "STATE=1 CREATOR " + space, null, "OR");
        testInsertSuffix(sql.copy(), "STATE=1 CREATOR " + space, null, "or");
        testInsertSuffix(sql.copy(), "STATE=1 CREATOR " + space, null, " OR ");
        testInsertSuffix(sql.copy(), "STATE=1 CREATOR " + space, null, " or ");
        testInsertSuffix(sql.copy(), "STATE=1 CREATOR " + space, null, "AND|OR");
        testInsertSuffix(sql.copy(), "STATE=1 CREATOR " + space, null, " AND|OR ");
        testInsertSuffix(sql.copy(), "STATE=1 CREATOR " + space, null, "and|or");
        testInsertSuffix(sql.copy(), "STATE=1 CREATOR " + space, null, " and | or ");
    }

    @Test
    public void testInsertSuffix14() {
        testInsertSuffix14("", new SqlBuffer("STATE=").addVariable(1).append(","));
        testInsertSuffix14("", new SqlBuffer("STATE=").addVariable(1).append(""));
        testInsertSuffix14("\n", new SqlBuffer("STATE=").addVariable(1).append(",\n"));
        testInsertSuffix14("\n", new SqlBuffer("STATE=").addVariable(1).append("\n"));
        testInsertSuffix14("\n\t", new SqlBuffer("STATE=").addVariable(1).append(",\n\t"));
        testInsertSuffix14("\n\t", new SqlBuffer("STATE=").addVariable(1).append("\n\t"));
        testInsertSuffix14("    \n    ", new SqlBuffer("STATE=").addVariable(1).append(",    \n    "));
        testInsertSuffix14("    \n    ", new SqlBuffer("STATE=").addVariable(1).append("    \n    "));
    }

    private void testInsertSuffix14(String space, SqlBuffer sql) {
        testInsertSuffix(sql.copy(), "STATE=1; " + space, ";", ",");
        testInsertSuffix(sql.copy(), "STATE=1; " + space, ";", ",");
        testInsertSuffix(sql.copy(), "STATE=1; " + space, ";", " , ");
        testInsertSuffix(sql.copy(), "STATE=1; " + space, ";", " , ");
        testInsertSuffix(sql.copy(), "STATE=1; " + space, ";", "!|,");
        testInsertSuffix(sql.copy(), "STATE=1; " + space, ";", " !|, ");
        testInsertSuffix(sql.copy(), "STATE=1; " + space, ";", "!|,");
        testInsertSuffix(sql.copy(), "STATE=1; " + space, ";", " ! | , ");
    }

    @Test
    public void testInsertSuffix15() {
        testInsertSuffix15("", new SqlBuffer().append("AND"));
        testInsertSuffix15("\n", new SqlBuffer().append("AND\n"));
        testInsertSuffix15("\n\t", new SqlBuffer().append("AND\n\t"));
        testInsertSuffix15("    \n    ", new SqlBuffer().append("AND    \n    "));
    }

    private void testInsertSuffix15(String space, SqlBuffer sql) {
        testInsertSuffix(sql.copy(), "TEST" + space, "TEST", "AND");
        testInsertSuffix(sql.copy(), "TEST" + space, "TEST", "and");
        testInsertSuffix(sql.copy(), "TEST" + space, "TEST", " AND ");
        testInsertSuffix(sql.copy(), "TEST" + space, "TEST", " and ");
        testInsertSuffix(sql.copy(), "TEST" + space, "TEST", "AND|OR");
        testInsertSuffix(sql.copy(), "TEST" + space, "TEST", " AND|OR ");
        testInsertSuffix(sql.copy(), "TEST" + space, "TEST", "and|or");
        testInsertSuffix(sql.copy(), "TEST" + space, "TEST", " and | or ");
    }

    @Test
    public void testInsertSuffix21() {
        testInsertSuffix21("", new SqlBuffer().addVariable(1));
        testInsertSuffix21("", new SqlBuffer().addVariable(1).append(" AND"));
        testInsertSuffix21("\n", new SqlBuffer().addVariable(1).append("\n"));
        testInsertSuffix21("\n", new SqlBuffer().addVariable(1).append(" AND\n"));
        testInsertSuffix21("\n\t", new SqlBuffer().addVariable(1).append("\n\t"));
        testInsertSuffix21("\n\t", new SqlBuffer().addVariable(1).append(" AND\n\t"));
        testInsertSuffix21("    \n    ", new SqlBuffer().addVariable(1).append("    \n    "));
        testInsertSuffix21("    \n    ", new SqlBuffer().addVariable(1).append(" AND    \n    "));
    }

    private void testInsertSuffix21(String space, SqlBuffer sql) {
        testInsertSuffix(sql.copy(), "1 TEST" + space, "TEST", "AND");
        testInsertSuffix(sql.copy(), "1 TEST" + space, "TEST", "and");
        testInsertSuffix(sql.copy(), "1 TEST" + space, "TEST", " AND ");
        testInsertSuffix(sql.copy(), "1 TEST" + space, "TEST", " and ");
        testInsertSuffix(sql.copy(), "1 TEST" + space, "TEST", "AND|OR");
        testInsertSuffix(sql.copy(), "1 TEST" + space, "TEST", " AND|OR ");
        testInsertSuffix(sql.copy(), "1 TEST" + space, "TEST", "and|or");
        testInsertSuffix(sql.copy(), "1 TEST" + space, "TEST", " and | or ");
    }

    @Test
    public void testInsertSuffix23() {
        testInsertSuffix23("", new SqlBuffer().addVariable(1));
        testInsertSuffix23("", new SqlBuffer().addVariable(1).append(" AND"));
        testInsertSuffix23("\n", new SqlBuffer().addVariable(1).append("\n"));
        testInsertSuffix23("\n", new SqlBuffer().addVariable(1).append(" AND\n"));
        testInsertSuffix23("\n\t", new SqlBuffer().addVariable(1).append("\n\t"));
        testInsertSuffix23("\n\t", new SqlBuffer().addVariable(1).append(" AND\n\t"));
        testInsertSuffix23("    \n    ", new SqlBuffer().addVariable(1).append("    \n    "));
        testInsertSuffix23("    \n    ", new SqlBuffer().addVariable(1).append(" AND    \n    "));
    }

    private void testInsertSuffix23(String space, SqlBuffer sql) {
        testInsertSuffix(sql.copy(), "1 " + space, null, "AND");
        testInsertSuffix(sql.copy(), "1 " + space, null, "and");
        testInsertSuffix(sql.copy(), "1 " + space, null, " AND ");
        testInsertSuffix(sql.copy(), "1 " + space, null, " and ");
        testInsertSuffix(sql.copy(), "1 " + space, null, "AND|OR");
        testInsertSuffix(sql.copy(), "1 " + space, null, " AND|OR ");
        testInsertSuffix(sql.copy(), "1 " + space, null, "and|or");
        testInsertSuffix(sql.copy(), "1 " + space, null, " and | or ");
    }

    private void testInsertSuffix(SqlBuffer sql, String result, String prefix, String prefixOverrides) {
        sql.insertSuffix(prefix, prefixOverrides);
        // 加上{EOF}, 防止自动去除最后的空白字符
        String actual = sql.append("{EOF}").getExecutableSqlString(dialect);
        String expected = StringTools.replace(result, "\t", "    ");
        Assert.assertEquals(actual, expected + "{EOF}", "testInsertSuffix");
    }

    @Test
    public void testNotChanged1() {
        testNotChanged1(new SqlBuffer().addVariable(1).append(" CREATOR AND"));
        testNotChanged1(new SqlBuffer().addVariable(1).append(" CREATOR"));
        testNotChanged1(new SqlBuffer().addVariable(1).append(" AND"));
        testNotChanged1(new SqlBuffer().addVariable(1));
        testNotChanged1(new SqlBuffer("\n"));
        testNotChanged1(new SqlBuffer("\n\t"));
        testNotChanged1(new SqlBuffer("    \n   "));
        testNotChanged1(new SqlBuffer());
    }

    private void testNotChanged1(SqlBuffer sql) {
        testNotChanged(sql.copy(), null, "OR");
        testNotChanged(sql.copy(), null, "or");
        testNotChanged(sql.copy(), null, " OR ");
        testNotChanged(sql.copy(), null, " or ");
    }

    private void testNotChanged(SqlBuffer sql, String prefix, String prefixOverrides) {
        boolean changed = sql.insertSuffix(prefix, prefixOverrides);
        Assert.assertEquals(changed, false, "testNotChanged");
    }
}
