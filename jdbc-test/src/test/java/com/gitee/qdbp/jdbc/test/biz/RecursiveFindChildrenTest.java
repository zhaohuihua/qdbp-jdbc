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
        initDepts(1001, 1, container);
        dao.inserts(container);
    }

    private void initDepts(int parent, int level, List<SysDeptEntity> container) {
        for (int i = 1; i <= level; i++) {
            int code = parent * 100 + i;
            SysDeptEntity dept = new SysDeptEntity();
            dept.setTenantCode("depttest"); // 租户编号
            dept.setDeptCode(String.valueOf(code)); // 部门编号
            dept.setDeptName("部门-" + code); // 部门名称
            dept.setParentCode(String.valueOf(parent)); // 上级部门编号
            dept.setSortIndex(i); // 排序号(越小越靠前)
            container.add(dept);
            if (level <= 5) {
                initDepts(code, level + 1, container);
            }
        }
    }

    @Test
    public void testListDeptChildren() throws IOException {
        CrudDao<SysDeptEntity> dao = qdbcBoot.buildCrudDao(SysDeptEntity.class);
        DbWhere where = DbWhere.NONE;
        Orderings orderings = Orderings.of("parentCode, sortIndex");
        List<SysDeptEntity> result = dao.listChildren("100101", "deptCode", "parentCode", where, orderings);
        Assert.assertTrue(result.size() == 20, "testListDeptChildren");
    }
}
