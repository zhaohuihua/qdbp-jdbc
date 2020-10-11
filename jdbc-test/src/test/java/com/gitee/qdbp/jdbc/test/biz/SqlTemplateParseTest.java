package com.gitee.qdbp.jdbc.test.biz;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.able.jdbc.condition.TableJoin;
import com.gitee.qdbp.able.jdbc.fields.Fields;
import com.gitee.qdbp.able.jdbc.ordering.Orderings;
import com.gitee.qdbp.jdbc.api.QdbcBoot;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.fragment.CrudFragmentHelper;
import com.gitee.qdbp.jdbc.sql.fragment.QueryFragmentHelper;
import com.gitee.qdbp.jdbc.sql.parse.SqlFragmentContainer;
import com.gitee.qdbp.jdbc.test.model.ActProcState;
import com.gitee.qdbp.jdbc.test.model.ActTask;
import com.gitee.qdbp.jdbc.test.model.SysDeptEntity;
import com.gitee.qdbp.jdbc.test.model.SysRoleEntity;
import com.gitee.qdbp.jdbc.test.model.SysUserRoleEntity;
import com.gitee.qdbp.tools.files.PathTools;
import com.gitee.qdbp.tools.utils.AssertTools;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * SQL解析测试
 *
 * @author zhaohuihua
 * @version 20200911
 */
@Test
@ContextConfiguration(locations = { "classpath:settings/spring/spring.xml" })
public class SqlTemplateParseTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private QdbcBoot qdbcBoot;

    @Test
    public void testGetUserRolesQuerySql11() throws IOException {
        String sqlId = "user.roles.query";
        String userIds = "10000001";
        testGetUserRolesQuerySql(sqlId, userIds, null, null, 11);
    }

    @Test
    public void testGetUserRolesQuerySql12() throws IOException {
        String sqlId = "user.roles.query";
        List<String> userIds = Arrays.asList("10000001", "10000002", "10000008");
        testGetUserRolesQuerySql(sqlId, userIds, null, null, 12);
    }

    @Test
    public void testGetUserRolesQuerySql21() throws IOException {
        String sqlId = "user.roles.query";
        int userIds = 10000001;
        Orderings orderings = Orderings.of("ur.userId, r.id");
        testGetUserRolesQuerySql(sqlId, userIds, null, orderings, 21);
    }

    @Test
    public void testGetUserRolesQuerySql22() throws IOException {
        String sqlId = "user.roles.query";
        List<Integer> userIds = Arrays.asList(10000001, 10000002, 10000008);
        Orderings orderings = Orderings.of("ur.userId, r.id");
        testGetUserRolesQuerySql(sqlId, userIds, null, orderings, 22);
    }

    @Test
    public void testGetUserRolesQuerySql31() throws IOException {
        String sqlId = "user.roles.query";
        int userIds = 10000001;
        DbWhere where = new DbWhere();
        where.on("r.tenantCode", "=", "userrole");
        Orderings orderings = Orderings.of("ur.userId, r.id");
        testGetUserRolesQuerySql(sqlId, userIds, where, orderings, 31);
    }

    @Test
    public void testGetUserRolesQuerySql32() throws IOException {
        String sqlId = "user.roles.query";
        int[] userIds = new int[] { 10000001, 10000002, 10000008 };
        DbWhere where = new DbWhere();
        where.on("r.tenantCode", "=", "userrole");
        Orderings orderings = Orderings.of("ur.userId, r.id");
        testGetUserRolesQuerySql(sqlId, userIds, where, orderings, 32);
    }

    private void testGetUserRolesQuerySql(String sqlId, Object userIds, DbWhere where, Orderings orderings, int index)
            throws IOException {
        SqlDialect dialect = qdbcBoot.getSqlDialect();
        TableJoin tables = TableJoin.of(SysUserRoleEntity.class, "ur", SysRoleEntity.class, "r");
        QueryFragmentHelper sqlHelper = qdbcBoot.buildSqlBuilder(tables).helper();

        Map<String, Object> params = new HashMap<>();
        params.put("userIds", userIds);
        if (VerifyTools.isNotBlank(where)) {
            params.put("whereCondition", sqlHelper.buildWhereSql(where, false));
        }
        if (VerifyTools.isNotBlank(orderings)) {
            params.put("orderByCondition", sqlHelper.buildOrderBySql(orderings, false));
        }

        SqlBuffer buffer = SqlFragmentContainer.defaults().render(sqlId, params, dialect);
        String sqlText = buffer.getLoggingSqlString(dialect);

        String fileName = "SqlTemplateParse." + sqlId + "." + index + ".sql";
        System.out.println("<<" + fileName + ">>" + '\n' + sqlText);
        URL resultFile = PathTools.findClassResource(SqlTemplateParseTest.class, fileName);
        String resultText = PathTools.downloadString(resultFile);
        AssertTools.assertTextLinesEquals(sqlText, resultText, sqlId);
    }

    @Test
    public void testGetRecursiveFindChildrenSql() throws IOException {
        SqlDialect dialect = qdbcBoot.getSqlDialect();
        CrudFragmentHelper sqlHelper = qdbcBoot.buildSqlBuilder(SysDeptEntity.class).helper();

        String codeField = "deptCode";
        String parentCode = "parentCode";
        List<String> startCodes = Arrays.asList("D0000001", "D0000002");
        DbWhere where = new DbWhere().on("dataState", "=", "1");
        Orderings orderings = Orderings.of("parentCode, sortIndex");

        Map<String, Object> params = new HashMap<>();
        params.put("keyword", "WITH RECURSIVE");
        params.put("codeField", sqlHelper.buildByFieldsSql(codeField));
        params.put("parentField", sqlHelper.buildByFieldsSql(parentCode));
        params.put("tableName", sqlHelper.getTableName());
        params.put("selectFields", sqlHelper.buildSelectFieldsSql(Fields.ALL));
        params.put("startCodeCondition", sqlHelper.buildInSql(codeField, startCodes, false));
        if (where != null && !where.isEmpty()) {
            params.put("whereCondition", sqlHelper.buildWhereSql(where, false));
        }
        if (VerifyTools.isNotBlank(orderings)) {
            params.put("orderByCondition", sqlHelper.buildOrderBySql(orderings, false));
        }
        String sqlId = "recursive.find.children";
        SqlBuffer buffer = SqlFragmentContainer.defaults().render(sqlId, params, dialect);
        String sqlText = buffer.getLoggingSqlString(dialect);

        String fileName = "SqlTemplateParse." + sqlId + ".sql";
        System.out.println("<<" + fileName + ">>" + '\n' + sqlText);
        URL resultFile = PathTools.findClassResource(SqlTemplateParseTest.class, fileName);
        String resultText = PathTools.downloadString(resultFile);
        AssertTools.assertTextLinesEquals(sqlText, resultText, sqlId);
    }

    @Test
    public void testGetBacklogTodoQuerySql() throws IOException {
        SqlDialect dialect = qdbcBoot.getSqlDialect();
        TableJoin tables = TableJoin.of(ActTask.class, "t", ActProcState.class, "s");
        QueryFragmentHelper sqlHelper = qdbcBoot.buildSqlBuilder(tables).helper();

        String userName = "U0000001";
        List<String> roleIds = Arrays.asList("R0000001", "R0000006");
        DbWhere where = new DbWhere().on("s.projectCode", "=", "P0000001");

        Map<String, Object> params = new HashMap<>();
        params.put("userName", userName);
        params.put("roleIds", roleIds);
        if (where != null && !where.isEmpty()) {
            params.put("whereCondition", sqlHelper.buildWhereSql(where, false));
        }

        String sqlId = "backlog.todo.query";
        SqlBuffer buffer = SqlFragmentContainer.defaults().render(sqlId, params, dialect);
        String sqlText = buffer.getLoggingSqlString(dialect);

        String fileName = "SqlTemplateParse." + sqlId + ".sql";
        System.out.println("<<" + fileName + ">>" + '\n' + sqlText);
        URL resultFile = PathTools.findClassResource(SqlTemplateParseTest.class, fileName);
        String resultText = PathTools.downloadString(resultFile);
        AssertTools.assertTextLinesEquals(sqlText, resultText, sqlId);
    }

    @Test
    public void testGetBacklogTodoCountSql() throws IOException {
        SqlDialect dialect = qdbcBoot.getSqlDialect();
        TableJoin tables = TableJoin.of(ActTask.class, "t", ActProcState.class, "s");
        QueryFragmentHelper sqlHelper = qdbcBoot.buildSqlBuilder(tables).helper();

        String userName = "U0000001";
        List<String> roleIds = Arrays.asList("R0000001", "R0000006");
        DbWhere where = new DbWhere().on("s.projectCode", "=", "P0000001");

        Map<String, Object> params = new HashMap<>();
        params.put("userName", userName);
        params.put("roleIds", roleIds);
        if (where != null && !where.isEmpty()) {
            params.put("whereCondition", sqlHelper.buildWhereSql(where, false));
        }

        String sqlId = "backlog.todo.count";
        SqlBuffer buffer = SqlFragmentContainer.defaults().render(sqlId, params, dialect);
        String sqlText = buffer.getLoggingSqlString(dialect);

        String fileName = "SqlTemplateParse." + sqlId + ".sql";
        System.out.println("<<" + fileName + ">>" + '\n' + sqlText);
        URL resultFile = PathTools.findClassResource(SqlTemplateParseTest.class, fileName);
        String resultText = PathTools.downloadString(resultFile);
        AssertTools.assertTextLinesEquals(sqlText, resultText, sqlId);
    }
}
