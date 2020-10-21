package com.gitee.qdbp.jdbc.test.biz;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.able.jdbc.ordering.Orderings;
import com.gitee.qdbp.jdbc.api.CrudDao;
import com.gitee.qdbp.jdbc.api.QdbcBoot;
import com.gitee.qdbp.jdbc.test.model.SysDeptEntity;
import com.gitee.qdbp.tools.utils.StringTools;

@Test
@ContextConfiguration(locations = { "classpath:settings/spring/spring.xml" })
public class RecursiveFindChildrenTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private QdbcBoot qdbcBoot;

    @BeforeClass
    public void init() {
        CrudDao<SysDeptEntity> dao = qdbcBoot.buildCrudDao(SysDeptEntity.class);
        { // 清除记录
            DbWhere where = new DbWhere();
            where.on("tenantCode", "=", "depttest");
            dao.physicalDelete(where);
        }
        List<SysDeptEntity> container = new ArrayList<>();
        initDepts("D", 1, container);
        dao.inserts(container);
    }

    private void initDepts(String parent, int level, List<SysDeptEntity> container) {
        for (int i = 1; i <= level; i++) {
            String code = parent + StringTools.pad(i, 2);
            SysDeptEntity dept = new SysDeptEntity();
            dept.setTenantCode("depttest"); // 租户编号
            dept.setDeptCode(code); // 部门编号
            dept.setDeptName("部门-" + code); // 部门名称
            dept.setParentCode(parent); // 上级部门编号
            dept.setSortIndex(i); // 排序号(越小越靠前)
            container.add(dept);
            if (level < 5) {
                initDepts(code, level + 1, container);
            }
        }
    }

    @Test
    public void testListDeptChildrenBean() throws IOException {
        CrudDao<SysDeptEntity> dao = qdbcBoot.buildCrudDao(SysDeptEntity.class);
        String startCode = "D0102";
        DbWhere filter = new DbWhere().on("tenantCode", "=", "depttest");
        DbWhere search = new DbWhere().on("deptCode", "!=", startCode);
        Orderings orderings = Orderings.of("parentCode, sortIndex");
        List<SysDeptEntity> result = dao.listChildren(startCode, "deptCode", "parentCode", filter, search, orderings);
        // 75 = 3 + 3 * 4 + 3 * 4 * 5
        // D0102是第2级, 每个2级有3个下级, 每个3级有4个下级, 每个4级有5个下级
        Assert.assertEquals(result.size(), 75, "testListDeptChildren");
    }

    @Test
    public void testListDeptChildrenCode() throws IOException {
        CrudDao<SysDeptEntity> dao = qdbcBoot.buildCrudDao(SysDeptEntity.class);
        String startCode = "D010203";
        DbWhere filter = new DbWhere().on("tenantCode", "=", "depttest");
        DbWhere search = new DbWhere().on("deptCode", "!=", startCode);
        Orderings orderings = Orderings.of("parentCode, sortIndex");
        List<String> result = dao.listChildrenCodes(startCode, "deptCode", "parentCode", filter, search, orderings);
        // 24 = 4 + 4 * 5
        // D010203是第3级, 每个3级有4个下级, 每个4级有5个下级
        Assert.assertEquals(result.size(), 24, "testListDeptChildren");
    }
}
