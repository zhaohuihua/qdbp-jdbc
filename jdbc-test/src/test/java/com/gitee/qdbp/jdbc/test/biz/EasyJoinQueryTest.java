package com.gitee.qdbp.jdbc.test.biz;

import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import com.gitee.qdbp.able.jdbc.condition.DbFieldName;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.able.jdbc.condition.TableJoin;
import com.gitee.qdbp.able.jdbc.ordering.OrderPaging;
import com.gitee.qdbp.able.jdbc.paging.PageList;
import com.gitee.qdbp.jdbc.api.CoreJdbcBoot;
import com.gitee.qdbp.jdbc.api.EasyJoinQuery;
import com.gitee.qdbp.jdbc.plugins.DbPluginContainer;
import com.gitee.qdbp.jdbc.plugins.impl.SimpleTableInfoScans;
import com.gitee.qdbp.jdbc.plugins.impl.StaticFieldTableNameScans;
import com.gitee.qdbp.jdbc.test.enums.DataState;
import com.gitee.qdbp.jdbc.test.model.SysRoleEntity;
import com.gitee.qdbp.jdbc.test.model.SysUserEntity;
import com.gitee.qdbp.jdbc.test.model.SysUserRoleEntity;
import com.gitee.qdbp.jdbc.test.model.UserRole;
import com.gitee.qdbp.tools.utils.JsonTools;

@Test
@ContextConfiguration(locations = { "classpath:settings/spring/spring.xml" })
public class EasyJoinQueryTest extends AbstractTestNGSpringContextTests {

    private Logger log = LoggerFactory.getLogger(EasyJoinQueryTest.class);

    @Autowired
    private CoreJdbcBoot coreJdbcBoot;

    @BeforeTest
    public void init() {
        SimpleTableInfoScans tableInfoScans = new SimpleTableInfoScans();
        tableInfoScans.setTableNameScans(new StaticFieldTableNameScans("TABLE"));
        DbPluginContainer.global.registerTableInfoScans(tableInfoScans);
    }

    /**
     * 查询多个用户的角色信息<br>
     * sys_user关联sys_user_role再关联sys_role<br>
     * 结果放在UserRole对象中<br>
     * 不需要中间表sys_user_role的信息<br>
     */
    @Test(priority = 2)
    public void testUserBeanQuery() {
        List<String> userCodes = Arrays.asList("evan", "kelly", "coral");
        // @formatter:off
        TableJoin tables = new TableJoin(SysUserEntity.class, "u", "user")
            .innerJoin(SysUserRoleEntity.class, "ur")
            .on("u.id", "=", new DbFieldName("ur.userId"))
            .and("ur.dataState", "=", DataState.NORMAL)
            .innerJoin(SysRoleEntity.class, "r", "role")
            .on("ur.roleId", "=", new DbFieldName("r.id"))
            .and("r.dataState", "=", DataState.NORMAL)
            .end();
        // @formatter:on
        DbWhere where = new DbWhere();
        where.on("u.userCode", "in", userCodes);
        EasyJoinQuery<UserRole> query = coreJdbcBoot.buildJoinQuery(tables, UserRole.class);
        // UserRole = { SysUser user; SysRole role; }
        PageList<UserRole> userRoles = query.list(where, OrderPaging.NONE);
        log.debug("UserRolesQueryResult: {}", JsonTools.toLogString(userRoles));
        Assert.assertNotNull(userRoles);
    }

    /**
     * 查询某用户所配置的角色<br>
     * sys_user关联sys_user_role再关联sys_role<br>
     * 只需要sys_role表的信息<br>
     */
    @Test(priority = 3)
    public void testRolesQueryByUser() {
        String userCode = "kelly";
        // @formatter:off
        TableJoin tables = new TableJoin(SysUserEntity.class, "u")
            .innerJoin(SysUserRoleEntity.class, "ur")
            .on("u.id", "=", new DbFieldName("ur.userId"))
            .and("ur.dataState", "=", DataState.NORMAL)
             // this表示结果字段放在主对象中
            .innerJoin(SysRoleEntity.class, "r", "this")
            .on("ur.roleId", "=", new DbFieldName("r.id"))
            .and("r.dataState", "=", DataState.NORMAL)
            .end();
        // @formatter:on
        DbWhere where = new DbWhere();
        where.on("u.userCode", "=", userCode);
        EasyJoinQuery<SysRoleEntity> query = coreJdbcBoot.buildJoinQuery(tables, SysRoleEntity.class);
        PageList<SysRoleEntity> roles = query.list(where, OrderPaging.NONE);
        log.debug("RolesQueryByUserResult: {}", JsonTools.toLogString(roles));
        Assert.assertNotNull(roles);
    }
}
