package com.gitee.qdbp.jdbc.test.biz;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.able.jdbc.fields.Fields;
import com.gitee.qdbp.able.jdbc.ordering.Orderings;
import com.gitee.qdbp.jdbc.api.CrudDao;
import com.gitee.qdbp.jdbc.api.QdbcBoot;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.fragment.CrudFragmentHelper;
import com.gitee.qdbp.jdbc.sql.parse.SqlFragmentContainer;
import com.gitee.qdbp.jdbc.test.model.SysDeptEntity;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * SQL解析测试
 *
 * @author zhaohuihua
 * @version 20200911
 */
@Test
@ContextConfiguration(locations = { "classpath:settings/spring/spring.xml" })
public class SqlParserTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private QdbcBoot qdbcBoot;

    @Test
    public void testGetSqlBuffer() {

        SqlDialect dialect = qdbcBoot.getSqlDialect();
        CrudDao<SysDeptEntity> dao = qdbcBoot.buildCrudDao(SysDeptEntity.class);
        CrudFragmentHelper sqlHelper = dao.getSqlBuilder().helper();

        String codeField = "deptCode";
        String parentCode = "parentCode";
        List<String> startCodes = Arrays.asList("1001", "1002");
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
        SqlBuffer sql = SqlFragmentContainer.defaults().render(sqlId, params, dialect);

        System.out.println(sql.getLoggingSqlString(dialect));
    }
}
