package com.gitee.qdbp.jdbc.test.biz;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.able.jdbc.ordering.OrderPaging;
import com.gitee.qdbp.able.jdbc.ordering.Orderings;
import com.gitee.qdbp.able.jdbc.paging.PageList;
import com.gitee.qdbp.able.jdbc.paging.Paging;
import com.gitee.qdbp.jdbc.api.CrudDao;
import com.gitee.qdbp.jdbc.api.QdbcBoot;
import com.gitee.qdbp.jdbc.test.enums.Gender;
import com.gitee.qdbp.jdbc.test.enums.UserSource;
import com.gitee.qdbp.jdbc.test.enums.UserState;
import com.gitee.qdbp.jdbc.test.enums.UserType;
import com.gitee.qdbp.jdbc.test.model.RoleIdUserResult;
import com.gitee.qdbp.jdbc.test.model.SysRoleEntity;
import com.gitee.qdbp.jdbc.test.model.SysUserEntity;
import com.gitee.qdbp.jdbc.test.model.SysUserRoleEntity;
import com.gitee.qdbp.jdbc.test.model.UserIdRoleResult;
import com.gitee.qdbp.jdbc.test.service.SysUserService;
import com.gitee.qdbp.tools.utils.StringTools;

@Test
@ContextConfiguration(locations = { "classpath:settings/spring/spring.xml" })
public class SqlTemplateUserRolesQueryTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private QdbcBoot qdbcBoot;
    @Autowired
    private SysUserService userService;

    @BeforeClass
    public void init() {
        List<String> userIds = initUsers();
        List<String> roleIds = initRoles();
        initUserRoles(userIds, roleIds);
    }

    private List<String> initUsers() {
        CrudDao<SysUserEntity> dao = qdbcBoot.buildCrudDao(SysUserEntity.class);
        { // 清理用户数据
            DbWhere where = new DbWhere();
            where.on("tenantCode", "=", "userrole");
            dao.physicalDelete(where);
        }
        { // 新增用户数据
            SysUserEntity entity = new SysUserEntity();
            entity.setTenantCode("userrole");
            entity.setSuperman(false);
            entity.setDeptCode("0");
            entity.setGender(Gender.FEMALE);
            entity.setUserType(UserType.USER);
            entity.setUserState(UserState.NORMAL);
            entity.setUserSource(UserSource.INPUT);
            List<SysUserEntity> entities = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                entity.setId("U0000" + StringTools.pad(i, 3));
                entity.setUserCode("U" + StringTools.pad(i, 3));
                entities.add(entity.to(SysUserEntity.class));
            }
            // 批量新增用户
            return dao.inserts(entities);
        }
    }

    private List<String> initRoles() {
        CrudDao<SysRoleEntity> dao = qdbcBoot.buildCrudDao(SysRoleEntity.class);
        { // 清理角色数据
            DbWhere where = new DbWhere();
            where.on("tenantCode", "=", "userrole");
            dao.physicalDelete(where);
        }
        { // 新增角色数据
            SysRoleEntity entity = new SysRoleEntity();
            entity.setTenantCode("userrole"); // 租户编号
            entity.setUserType(UserType.USER); // 用户类型
            entity.setDefaults(false);
            List<SysRoleEntity> entities = new ArrayList<>();
            for (int i = 1; i <= 20; i++) {
                entity.setSortIndex(i);
                entity.setId("R0000" + StringTools.pad(i, 3));
                entity.setRoleName("R" + StringTools.pad(i, 3));
                entities.add(entity.to(SysRoleEntity.class));
            }
            // 批量新增角色
            return dao.inserts(entities);
        }
    }

    private List<String> initUserRoles(List<String> userIds, List<String> roleIds) {
        CrudDao<SysUserRoleEntity> dao = qdbcBoot.buildCrudDao(SysUserRoleEntity.class);
        dao.physicalDelete(DbWhere.NONE);
        List<SysUserRoleEntity> entities = new ArrayList<>();
        for (int i = 0; i < userIds.size(); i++) {
            String userId = userIds.get(i);
            for (int j = 0; j < (i + 1) * 2 && j < roleIds.size(); j++) {
                String roleId = roleIds.get(j);
                SysUserRoleEntity entity = new SysUserRoleEntity();
                entity.setUserId(userId);
                entity.setRoleId(roleId);
                entities.add(entity);
            }
        }
        // 批量新增用户角色关系
        return dao.inserts(entities);
    }

    ///////////////////////////////////////////////
    // 查询用户所分配的角色
    ///////////////////////////////////////////////

    @Test
    public void testGetUserRolesQuerySql11() throws IOException {
        String userId = "U0000001";
        List<SysRoleEntity> list = userService.getUserRoles(userId, DbWhere.NONE, Orderings.NONE);

        // 用户1有2个角色, 2有4个, 3=6, 4=8, ...
        int size = 2;
        Assert.assertTrue(list.size() == size, "SysUserService.getUserRoles");
    }

    @Test
    public void testGetUserRolesQuerySql12() throws IOException {
        List<String> userIds = Arrays.asList("U0000001", "U0000002", "U0000008");
        List<UserIdRoleResult> list = userService.getUserRoles(userIds, DbWhere.NONE, Orderings.NONE);

        // 用户1有2个角色, 2有4个, 3=6, 4=8, ...
        int size = 22;
        Assert.assertTrue(list.size() == size, "SysUserService.getUserRoles");
    }

    @Test
    public void testGetUserRolesQuerySql21() throws IOException {
        String userId = "U0000001";
        Orderings orderings = Orderings.of("ur.userId, r.id");
        List<SysRoleEntity> list = userService.getUserRoles(userId, DbWhere.NONE, orderings);

        // 用户1有2个角色, 2有4个, 3=6, 4=8, ...
        int size = 2;
        Assert.assertTrue(list.size() == size, "SysUserService.getUserRoles");
    }

    @Test
    public void testGetUserRolesQuerySql22() throws IOException {
        List<String> userIds = Arrays.asList("U0000001", "U0000002", "U0000008");
        Orderings orderings = Orderings.of("ur.userId, r.id");
        List<UserIdRoleResult> list = userService.getUserRoles(userIds, DbWhere.NONE, orderings);

        // 用户1有2个角色, 2有4个, 3=6, 4=8, ...
        int size = 22;
        Assert.assertTrue(list.size() == size, "SysUserService.getUserRoles");
    }

    @Test
    public void testGetUserRolesQuerySql31() throws IOException {
        String userId = "U0000001";
        DbWhere where = new DbWhere();
        where.on("r.tenantCode", "=", "userrole");
        Orderings orderings = Orderings.of("ur.userId, r.id");
        List<SysRoleEntity> list = userService.getUserRoles(userId, where, orderings);

        // 用户1有2个角色, 2有4个, 3=6, 4=8, ...
        int size = 2;
        Assert.assertTrue(list.size() == size, "SysUserService.getUserRoles");
    }

    @Test
    public void testGetUserRolesQuerySql32() throws IOException {
        List<String> userIds = Arrays.asList("U0000001", "U0000002", "U0000008");
        DbWhere where = new DbWhere();
        where.on("r.tenantCode", "=", "userrole");
        Orderings orderings = Orderings.of("ur.userId, r.id");
        List<UserIdRoleResult> list = userService.getUserRoles(userIds, where, orderings);

        // 用户1有2个角色, 2有4个, 3=6, 4=8, ...
        int size = 22;
        Assert.assertTrue(list.size() == size, "SysUserService.getUserRoles");
    }

    ///////////////////////////////////////////////
    // 查询角色下的用户, 分页查询
    ///////////////////////////////////////////////

    @Test
    public void testGetRoleUsersQuerySql11() throws IOException {
        String roleId = "R0000001";
        OrderPaging odpg = OrderPaging.of(new Paging(1, 5));
        PageList<SysUserEntity> list = userService.getRoleUsers(roleId, DbWhere.NONE, odpg);

        // 角色1/2分别有10个用户, 3/4=9, 5/6=8, 7/8=7, ..., 19/20=1
        int size = 5;
        int total = 10;
        Assert.assertTrue(list.size() == size, "SysUserService.getRoleUsers[result.size]");
        Assert.assertTrue(list.getTotal() == total, "SysUserService.getRoleUsers[result.total]");
    }

    @Test
    public void testGetRoleUsersQuerySql12() throws IOException {
        List<String> roleIds = Arrays.asList("R0000001", "R0000002", "R0000008");
        OrderPaging odpg = OrderPaging.of(new Paging(3, 10));
        PageList<RoleIdUserResult> list = userService.getRoleUsers(roleIds, DbWhere.NONE, odpg);

        // 角色1/2分别有10个用户, 3/4=9, 5/6=8, 7/8=7, ..., 19/20=1
        int size = 7;
        int total = 27;
        Assert.assertTrue(list.size() == size, "SysUserService.getRoleUsers[result.size]");
        Assert.assertTrue(list.getTotal() == total, "SysUserService.getRoleUsers[result.total]");
    }

    @Test
    public void testGetRoleUsersQuerySql21() throws IOException {
        String roleId = "R0000001";
        OrderPaging odpg = OrderPaging.of(new Paging(1, 5), "ur.roleId, u.id");
        PageList<SysUserEntity> list = userService.getRoleUsers(roleId, DbWhere.NONE, odpg);

        // 角色1/2分别有10个用户, 3/4=9, 5/6=8, 7/8=7, ..., 19/20=1
        int size = 5;
        int total = 10;
        Assert.assertTrue(list.size() == size, "SysUserService.getRoleUsers[result.size]");
        Assert.assertTrue(list.getTotal() == total, "SysUserService.getRoleUsers[result.total]");
    }

    @Test
    public void testGetRoleUsersQuerySql22() throws IOException {
        List<String> roleIds = Arrays.asList("R0000001", "R0000002", "R0000008");
        OrderPaging odpg = OrderPaging.of(new Paging(3, 10), "ur.roleId, u.id");
        PageList<RoleIdUserResult> list = userService.getRoleUsers(roleIds, DbWhere.NONE, odpg);

        // 1/2分别有10个用户, 3/4=9, 5/6=8, 7/8=7, ..., 19/20=1
        int size = 7;
        int total = 27;
        Assert.assertTrue(list.size() == size, "SysUserService.getRoleUsers[result.size]");
        Assert.assertTrue(list.getTotal() == total, "SysUserService.getRoleUsers[result.total]");
    }

    @Test
    public void testGetRoleUsersQuerySql31() throws IOException {
        String roleId = "R0000001";
        DbWhere where = new DbWhere();
        where.on("u.tenantCode", "=", "userrole");
        OrderPaging odpg = OrderPaging.of(new Paging(1, 5), "ur.roleId, u.id");
        PageList<SysUserEntity> list = userService.getRoleUsers(roleId, where, odpg);

        // 1/2分别有10个用户, 3/4=9, 5/6=8, 7/8=7, ..., 19/20=1
        int size = 5;
        int total = 10;
        Assert.assertTrue(list.size() == size, "SysUserService.getRoleUsers[result.size]");
        Assert.assertTrue(list.getTotal() == total, "SysUserService.getRoleUsers[result.total]");
    }

    @Test
    public void testGetRoleUsersQuerySql32() throws IOException {
        List<String> roleIds = Arrays.asList("R0000001", "R0000002", "R0000008");
        DbWhere where = new DbWhere();
        where.on("u.tenantCode", "=", "userrole");
        OrderPaging odpg = OrderPaging.of(new Paging(3, 10), "ur.roleId, u.id");
        PageList<RoleIdUserResult> list = userService.getRoleUsers(roleIds, where, odpg);

        // 1/2分别有10个用户, 3/4=9, 5/6=8, 7/8=7, ..., 19/20=1
        int size = 7;
        int total = 27;
        Assert.assertTrue(list.size() == size, "SysUserService.getRoleUsers[result.size]");
        Assert.assertTrue(list.getTotal() == total, "SysUserService.getRoleUsers[result.total]");
    }
}
