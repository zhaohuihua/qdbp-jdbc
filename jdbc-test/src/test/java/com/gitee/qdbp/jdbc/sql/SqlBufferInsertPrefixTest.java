package com.gitee.qdbp.jdbc.sql;

import org.testng.Assert;
import org.testng.annotations.Test;
import com.gitee.qdbp.jdbc.model.MainDbType;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.utils.DbTools;
import com.gitee.qdbp.tools.utils.StringTools;

@Test
public class SqlBufferInsertPrefixTest {

    private SqlDialect dialect = DbTools.buildSqlDialect(MainDbType.MySQL);

    @Test
    public void testInsertPrefix11() {
        testInsertPrefix11("", new SqlBuffer("AND ORG_STATE=").addVariable(1));
        testInsertPrefix11("", new SqlBuffer("ORG_STATE=").addVariable(1));
        testInsertPrefix11("\n", new SqlBuffer("\nAND ORG_STATE=").addVariable(1));
        testInsertPrefix11("\n", new SqlBuffer("\nORG_STATE=").addVariable(1));
        testInsertPrefix11("\n\t", new SqlBuffer("\n\tAND ORG_STATE=").addVariable(1));
        testInsertPrefix11("\n\t", new SqlBuffer("\n\tORG_STATE=").addVariable(1));
        testInsertPrefix11("    \n    ", new SqlBuffer("    \n    AND ORG_STATE=").addVariable(1));
        testInsertPrefix11("    \n    ", new SqlBuffer("    \n    ORG_STATE=").addVariable(1));
    }

    private void testInsertPrefix11(String space, SqlBuffer sql) {
        testInsertPrefix(sql.copy(), space + "WHERE ORG_STATE=1", "WHERE", "AND");
        testInsertPrefix(sql.copy(), space + "WHERE ORG_STATE=1", "WHERE", "and");
        testInsertPrefix(sql.copy(), space + "WHERE ORG_STATE=1", "WHERE", " AND ");
        testInsertPrefix(sql.copy(), space + "WHERE ORG_STATE=1", "WHERE", " and ");
        testInsertPrefix(sql.copy(), space + "WHERE ORG_STATE=1", "WHERE", "AND|OR");
        testInsertPrefix(sql.copy(), space + "WHERE ORG_STATE=1", "WHERE", " AND|OR ");
        testInsertPrefix(sql.copy(), space + "WHERE ORG_STATE=1", "WHERE", "and|or");
        testInsertPrefix(sql.copy(), space + "WHERE ORG_STATE=1", "WHERE", " and | or ");
    }

    @Test
    public void testInsertPrefix12() {
        testInsertPrefix12("", new SqlBuffer("OR ORG_STATE=").addVariable(1));
        testInsertPrefix12("", new SqlBuffer("ORG_STATE=").addVariable(1));
        testInsertPrefix12("\n", new SqlBuffer("\nOR ORG_STATE=").addVariable(1));
        testInsertPrefix12("\n", new SqlBuffer("\nORG_STATE=").addVariable(1));
        testInsertPrefix12("\n\t", new SqlBuffer("\n\tOR ORG_STATE=").addVariable(1));
        testInsertPrefix12("\n\t", new SqlBuffer("\n\tORG_STATE=").addVariable(1));
        testInsertPrefix12("    \n    ", new SqlBuffer("    \n    OR ORG_STATE=").addVariable(1));
        testInsertPrefix12("    \n    ", new SqlBuffer("    \n    ORG_STATE=").addVariable(1));
    }

    private void testInsertPrefix12(String space, SqlBuffer sql) {
        testInsertPrefix(sql.copy(), space + "WHERE ORG_STATE=1", "WHERE", "OR");
        testInsertPrefix(sql.copy(), space + "WHERE ORG_STATE=1", "WHERE", "or");
        testInsertPrefix(sql.copy(), space + "WHERE ORG_STATE=1", "WHERE", " OR ");
        testInsertPrefix(sql.copy(), space + "WHERE ORG_STATE=1", "WHERE", " or ");
        testInsertPrefix(sql.copy(), space + "WHERE ORG_STATE=1", "WHERE", "AND|OR");
        testInsertPrefix(sql.copy(), space + "WHERE ORG_STATE=1", "WHERE", " AND|OR ");
        testInsertPrefix(sql.copy(), space + "WHERE ORG_STATE=1", "WHERE", "and|or");
        testInsertPrefix(sql.copy(), space + "WHERE ORG_STATE=1", "WHERE", " and | or ");
    }

    @Test
    public void testInsertPrefix13() {
        testInsertPrefix13("", new SqlBuffer("OR ORG_STATE=").addVariable(1));
        testInsertPrefix13("", new SqlBuffer(" ORG_STATE=").addVariable(1));
        testInsertPrefix13("\n", new SqlBuffer("\nOR ORG_STATE=").addVariable(1));
        testInsertPrefix13("\n", new SqlBuffer("\n ORG_STATE=").addVariable(1));
        testInsertPrefix13("\n\t", new SqlBuffer("\n\tOR ORG_STATE=").addVariable(1));
        testInsertPrefix13("\n\t", new SqlBuffer("\n\t ORG_STATE=").addVariable(1));
        testInsertPrefix13("    \n    ", new SqlBuffer("    \n    OR ORG_STATE=").addVariable(1));
        testInsertPrefix13("    \n    ", new SqlBuffer("    \n     ORG_STATE=").addVariable(1));
    }

    private void testInsertPrefix13(String space, SqlBuffer sql) {
        testInsertPrefix(sql.copy(), space + " ORG_STATE=1", null, "OR");
        testInsertPrefix(sql.copy(), space + " ORG_STATE=1", null, "or");
        testInsertPrefix(sql.copy(), space + " ORG_STATE=1", null, " OR ");
        testInsertPrefix(sql.copy(), space + " ORG_STATE=1", null, " or ");
        testInsertPrefix(sql.copy(), space + " ORG_STATE=1", null, "AND|OR");
        testInsertPrefix(sql.copy(), space + " ORG_STATE=1", null, " AND|OR ");
        testInsertPrefix(sql.copy(), space + " ORG_STATE=1", null, "and|or");
        testInsertPrefix(sql.copy(), space + " ORG_STATE=1", null, " and | or ");
    }

    @Test
    public void testInsertPrefix21() {
        testInsertPrefix21("", new SqlBuffer("").addVariable(1));
        testInsertPrefix21("", new SqlBuffer("AND ").addVariable(1));
        testInsertPrefix21("\n", new SqlBuffer("\n").addVariable(1));
        testInsertPrefix21("\n", new SqlBuffer("\nAND ").addVariable(1));
        testInsertPrefix21("\n\t", new SqlBuffer("\n\t").addVariable(1));
        testInsertPrefix21("\n\t", new SqlBuffer("\n\tAND ").addVariable(1));
        testInsertPrefix21("    \n    ", new SqlBuffer("    \n    ").addVariable(1));
        testInsertPrefix21("    \n    ", new SqlBuffer("    \n    AND ").addVariable(1));
    }

    private void testInsertPrefix21(String space, SqlBuffer sql) {
        testInsertPrefix(sql.copy(), space + "LIKE 1", "LIKE", "AND");
        testInsertPrefix(sql.copy(), space + "LIKE 1", "LIKE", "and");
        testInsertPrefix(sql.copy(), space + "LIKE 1", "LIKE", " AND ");
        testInsertPrefix(sql.copy(), space + "LIKE 1", "LIKE", " and ");
        testInsertPrefix(sql.copy(), space + "LIKE 1", "LIKE", "AND|OR");
        testInsertPrefix(sql.copy(), space + "LIKE 1", "LIKE", " AND|OR ");
        testInsertPrefix(sql.copy(), space + "LIKE 1", "LIKE", "and|or");
        testInsertPrefix(sql.copy(), space + "LIKE 1", "LIKE", " and | or ");
    }

    @Test
    public void testInsertPrefix22() {
        testInsertPrefix23("", new SqlBuffer(" ").addVariable(1));
        testInsertPrefix23("", new SqlBuffer("AND ").addVariable(1));
        testInsertPrefix23("\n", new SqlBuffer("\n ").addVariable(1));
        testInsertPrefix23("\n", new SqlBuffer("\nAND ").addVariable(1));
        testInsertPrefix23("\n\t", new SqlBuffer("\n\t ").addVariable(1));
        testInsertPrefix23("\n\t", new SqlBuffer("\n\tAND ").addVariable(1));
        testInsertPrefix23("    \n    ", new SqlBuffer("    \n     ").addVariable(1));
        testInsertPrefix23("    \n    ", new SqlBuffer("    \n    AND ").addVariable(1));
    }

    private void testInsertPrefix23(String space, SqlBuffer sql) {
        testInsertPrefix(sql.copy(), space + " 1", null, "AND");
        testInsertPrefix(sql.copy(), space + " 1", null, "and");
        testInsertPrefix(sql.copy(), space + " 1", null, " AND ");
        testInsertPrefix(sql.copy(), space + " 1", null, " and ");
        testInsertPrefix(sql.copy(), space + " 1", null, "AND|OR");
        testInsertPrefix(sql.copy(), space + " 1", null, " AND|OR ");
        testInsertPrefix(sql.copy(), space + " 1", null, "and|or");
        testInsertPrefix(sql.copy(), space + " 1", null, " and | or ");
    }

    private void testInsertPrefix(SqlBuffer sql, String result, String prefix, String prefixOverrides) {
        sql.insertPrefix(prefix, prefixOverrides);
        String actual = sql.getExecutableSqlString(dialect);
        String expected = StringTools.replace(result, "\t", "    ");
        Assert.assertEquals(actual, expected, "testInsertPrefix");
    }

    @Test
    public void testNotChanged1() {
        testNotChanged1(new SqlBuffer("AND ORG_STATE=").addVariable(1));
        testNotChanged1(new SqlBuffer("ORG_STATE=").addVariable(1));
        testNotChanged1(new SqlBuffer("AND ").addVariable(1));
        testNotChanged1(new SqlBuffer().addVariable(1));
        testNotChanged1(new SqlBuffer());
    }

    private void testNotChanged1(SqlBuffer sql) {
        testNotChanged(sql.copy(), null, "OR");
        testNotChanged(sql.copy(), null, "or");
        testNotChanged(sql.copy(), null, " OR ");
        testNotChanged(sql.copy(), null, " or ");
    }

    private void testNotChanged(SqlBuffer sql, String prefix, String prefixOverrides) {
        boolean changed = sql.insertPrefix(prefix, prefixOverrides);
        Assert.assertEquals(changed, false, "testNotChanged");
    }
}
