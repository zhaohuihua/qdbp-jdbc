package com.gitee.qdbp.jdbc.test.biz;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.able.jdbc.fields.ExcludeFields;
import com.gitee.qdbp.able.jdbc.fields.Fields;
import com.gitee.qdbp.able.jdbc.fields.IncludeFields;
import com.gitee.qdbp.able.jdbc.ordering.OrderPaging;
import com.gitee.qdbp.able.jdbc.ordering.Orderings;
import com.gitee.qdbp.able.jdbc.paging.PageList;
import com.gitee.qdbp.able.jdbc.paging.Paging;
import com.gitee.qdbp.jdbc.api.CrudDao;
import com.gitee.qdbp.jdbc.api.QdbcBoot;
import com.gitee.qdbp.jdbc.model.DbVersion;
import com.gitee.qdbp.jdbc.test.enums.DataState;
import com.gitee.qdbp.jdbc.test.enums.Gender;
import com.gitee.qdbp.jdbc.test.enums.UserSource;
import com.gitee.qdbp.jdbc.test.enums.UserState;
import com.gitee.qdbp.jdbc.test.enums.UserType;
import com.gitee.qdbp.jdbc.test.model.SysUserEntity;
import com.gitee.qdbp.jdbc.utils.ParseTools;
import com.gitee.qdbp.tools.utils.ConvertTools;
import com.gitee.qdbp.tools.utils.DateTools;
import com.gitee.qdbp.tools.utils.JsonTools;
import com.gitee.qdbp.tools.utils.StringTools;

@Test
@ContextConfiguration(locations = { "classpath:settings/spring/spring.xml" })
public class SimpleCrudDaoTest extends AbstractTestNGSpringContextTests {

    private Logger log = LoggerFactory.getLogger(SimpleCrudDaoTest.class);

    @Autowired
    private QdbcBoot qdbcBoot;

    @BeforeClass
    public void testVersionQuery() {
        DbVersion version = qdbcBoot.getSqlBufferJdbcOperations().getDbVersion();
        log.debug("DbVersion: {}", version);
        Assert.assertNotNull(version);
    }

    @Test(priority = 1001)
    public void testUserEntityDelete() {
        DbWhere where = new DbWhere();
        where.on("tenantCode", "=", "test");
        CrudDao<SysUserEntity> dao = qdbcBoot.buildCrudDao(SysUserEntity.class);
        dao.physicalDelete(where);
    }

    @Test(priority = 1002)
    public void testUserEntityCreate() {
        CrudDao<SysUserEntity> dao = qdbcBoot.buildCrudDao(SysUserEntity.class);
        { // 超级管理员
            SysUserEntity entity = new SysUserEntity();
            entity.setTenantCode("test");
            entity.setUserCode("super");
            entity.setSuperman(true);
            entity.setDeptCode("0");
            entity.setGender(Gender.UNKNOWN);
            entity.setUserState(UserState.NORMAL);
            entity.setUserSource(UserSource.INPUT);
            entity.setCreateTime(DateTools.parse("2018-05-15 20:30:40"));

            entity.setId("Y1000001");
            entity.setUserType(UserType.ADMIN);
            dao.insert(entity);

            entity.setId("U1000001");
            entity.setUserType(UserType.USER);
            dao.insert(entity);
        }
        { // 添加普通用户
            SysUserEntity entity = new SysUserEntity();
            entity.setTenantCode("test");
            entity.setSuperman(false);
            entity.setDeptCode("0");
            entity.setGender(Gender.FEMALE);
            entity.setUserType(UserType.USER);
            entity.setUserState(UserState.NORMAL);
            entity.setUserSource(UserSource.INPUT);

            entity.setId(null);
            entity.setUserCode("kelly");
            dao.insert(entity);

            entity.setId(null);
            entity.setUserCode("evan");
            dao.insert(entity);

            entity.setId(null);
            entity.setUserCode("coral");
            dao.insert(entity);
        }
        { // 添加分页查询数据
            SysUserEntity entity = new SysUserEntity();
            entity.setTenantCode("test");
            entity.setSuperman(false);
            entity.setDeptCode("0");
            entity.setGender(Gender.FEMALE);
            entity.setUserType(UserType.USER);
            entity.setUserState(UserState.NORMAL);
            entity.setUserSource(UserSource.INPUT);
            List<SysUserEntity> entities = new ArrayList<>();
            for (int i = 1; i <= 50; i++) {
                entity.setUserCode("test-" + StringTools.pad(i, 3));
                entities.add(entity.to(SysUserEntity.class));
            }
            // 批量新增
            dao.inserts(entities);
        }
    }

    @Test(priority = 2011)
    public void testUserEntityQuery() {
        SysUserEntity entity = new SysUserEntity();
        entity.setTenantCode("test");
        entity.setUserCode("super");
        entity.setSuperman(true);
        entity.setUserType(UserType.ADMIN);
        entity.setUserState(UserState.NORMAL);
        DbWhere where = ParseTools.parseBeanToDbWhere(entity);
        CrudDao<SysUserEntity> dao = qdbcBoot.buildCrudDao(SysUserEntity.class);
        SysUserEntity user = dao.find(where);
        log.debug("UserEntityQueryResult: {}", JsonTools.toLogString(user));
        String desc = "UserEntity[super]";
        Assert.assertNotNull(user, desc + "QueryResult");
        Assert.assertEquals(user.getSuperman(), Boolean.TRUE, desc + ":BooleanField[superman]");
        Assert.assertEquals(user.getUserType(), UserType.ADMIN, desc + ":InterfaceField[userType]");
        Assert.assertEquals(user.getUserState(), UserState.NORMAL, desc + ":EnumField[userState]");
        Assert.assertEquals(user.getDataState(), DataState.NORMAL, desc + ":DataStateField[dataState]");
    }

    @Test(priority = 2012)
    public void testUserWhereQuery() {
        DbWhere where = new DbWhere();
        where.on("tenantCode", "=", "test");
        where.on("userCode", "=", "super");
        where.on("superman", "=", true);
        where.on("userType", "=", UserType.ADMIN);
        where.on("userState", "in", UserState.NORMAL, UserState.LOCKED);
        where.on("createTime", ">=", DateTools.parse("2017-01-01"));
        CrudDao<SysUserEntity> dao = qdbcBoot.buildCrudDao(SysUserEntity.class);
        SysUserEntity user = dao.find(where);
        log.debug("UserWhereQueryResult: {}", JsonTools.toLogString(user));
        Assert.assertNotNull(user, "UserWhere[super]QueryResult");
    }

    @Test(priority = 2013)
    public void testUserOrQuery() {
        // @formatter:off
        DbWhere where = new DbWhere();
        where.on("tenantCode", "=", "test");
        where.on("userType", "=", UserType.USER);
        where.on("userState", "in", UserState.NORMAL, UserState.LOCKED);
        where.on("createTime", ">=", DateTools.parse("2017-01-01"));
        where.sub("or")
            .on("userCode", "=", "kelly")
            .on("superman", "=", true);
        // @formatter:on
        CrudDao<SysUserEntity> dao = qdbcBoot.buildCrudDao(SysUserEntity.class);
        PageList<SysUserEntity> users = dao.list(where, OrderPaging.NONE);
        log.debug("UserOrQueryResult: {}", JsonTools.toLogString(users));
        Assert.assertEquals(users.size(), 2, "UserOrQueryResult");
    }

    @Test(priority = 2020)
    public void testFindIncludeFields() {
        Fields fields = new IncludeFields("id,userCode,userName,nickName,realName,phone,email");
        DbWhere where = new DbWhere();
        where.on("tenantCode", "=", "test");
        where.on("userType", "=", UserType.USER);
        where.on("userCode", "=", "kelly");
        CrudDao<SysUserEntity> dao = qdbcBoot.buildCrudDao(SysUserEntity.class);
        SysUserEntity entity = dao.find(fields, where);
        log.debug("FindIncludeFieldsQueryResult: {}", JsonTools.toLogString(entity));
        Assert.assertNotNull(entity, "FindIncludeFieldsQueryResult");
        Assert.assertNotNull(entity.getId(), "UserEntity.id");
        Assert.assertNull(entity.getTenantCode(), "UserEntity.tenantCode");
        Assert.assertNull(entity.getDeptCode(), "UserEntity.deptCode");
        Assert.assertNull(entity.getUserType(), "UserEntity.userType");
        Assert.assertNull(entity.getUserState(), "UserEntity.userState");
        Assert.assertNull(entity.getCreateTime(), "UserEntity.createTime");
        Assert.assertEquals(entity.getUserCode(), "kelly", "UserEntity.userCode");
    }

    @Test(priority = 2021)
    public void testFindExcludeFields() {
        Fields fields = new ExcludeFields("tenantCode,userState,createTime");
        DbWhere where = new DbWhere();
        where.on("tenantCode", "=", "test");
        where.on("userType", "=", UserType.USER);
        where.on("userCode", "=", "kelly");
        CrudDao<SysUserEntity> dao = qdbcBoot.buildCrudDao(SysUserEntity.class);
        SysUserEntity entity = dao.find(fields, where);
        log.debug("FindExcludeFieldsQueryResult: {}", JsonTools.toLogString(entity));
        Assert.assertNotNull(entity, "FindExcludeFieldsQueryResult");
        Assert.assertNotNull(entity.getId(), "UserEntity.id");
        Assert.assertNull(entity.getTenantCode(), "UserEntity.tenantCode");
        Assert.assertNotNull(entity.getDeptCode(), "UserEntity.deptCode");
        Assert.assertNotNull(entity.getUserType(), "UserEntity.userType");
        Assert.assertNull(entity.getUserState(), "UserEntity.userState");
        Assert.assertNull(entity.getCreateTime(), "UserEntity.createTime");
        Assert.assertEquals(entity.getUserCode(), "kelly", "UserEntity.userCode");
    }

    @Test(priority = 2022)
    public void testListIncludeFields() {
        IncludeFields fields = new IncludeFields("id,userCode,userName,nickName,realName,phone,email");
        DbWhere where = new DbWhere();
        where.on("tenantCode", "=", "test");
        where.on("userType", "=", UserType.USER);
        where.on("userCode", "in", "kelly", "evan", "coral");
        CrudDao<SysUserEntity> dao = qdbcBoot.buildCrudDao(SysUserEntity.class);
        PageList<SysUserEntity> entities = dao.list(fields, where, OrderPaging.of("createTime"));
        Assert.assertEquals(entities.size(), 3, "ListIncludeFieldsQueryResult");
        for (SysUserEntity entity : entities) {
            Assert.assertNotNull(entity.getId(), "UserEntity.id");
            Assert.assertNull(entity.getTenantCode(), "UserEntity.tenantCode");
            Assert.assertNull(entity.getDeptCode(), "UserEntity.deptCode");
            Assert.assertNull(entity.getUserType(), "UserEntity.userType");
            Assert.assertNull(entity.getUserState(), "UserEntity.userState");
            Assert.assertNull(entity.getCreateTime(), "UserEntity.createTime");
            Assert.assertNotNull(entity.getUserCode(), "UserEntity.userCode");
        }
    }

    @Test(priority = 2023)
    public void testListExcludeFields() {
        Fields fields = new ExcludeFields("tenantCode,userState,createTime");
        DbWhere where = new DbWhere();
        where.on("tenantCode", "=", "test");
        where.on("userType", "=", UserType.USER);
        where.on("userCode", "in", "kelly", "evan", "coral");
        CrudDao<SysUserEntity> dao = qdbcBoot.buildCrudDao(SysUserEntity.class);
        PageList<SysUserEntity> entities = dao.list(fields, where, OrderPaging.of("createTime"));
        Assert.assertEquals(entities.size(), 3, "ListExcludeFieldsQueryResult");
        for (SysUserEntity entity : entities) {
            Assert.assertNotNull(entity.getId(), "UserEntity.id");
            Assert.assertNull(entity.getTenantCode(), "UserEntity.tenantCode");
            Assert.assertNotNull(entity.getDeptCode(), "UserEntity.deptCode");
            Assert.assertNotNull(entity.getUserType(), "UserEntity.userType");
            Assert.assertNull(entity.getUserState(), "UserEntity.userState");
            Assert.assertNull(entity.getCreateTime(), "UserEntity.createTime");
            Assert.assertNotNull(entity.getUserCode(), "UserEntity.userCode");
        }
    }

    @Test(priority = 3010)
    public void testUserPaging() {
        CrudDao<SysUserEntity> dao = qdbcBoot.buildCrudDao(SysUserEntity.class);
        DbWhere where = DbWhere.NONE;
        int total = dao.count(where);
        log.debug("UserRecordCount: {}", total);
        Orderings orderings = Orderings.of("createTime desc");
        int pageSize = 10;
        int pageCount = Math.min(5, (total + pageSize - 1) / pageSize);
        for (int i = 1; i <= pageCount; i++) {
            Paging paging = new Paging(i, pageSize, false);
            PageList<SysUserEntity> users = dao.list(where, OrderPaging.of(paging, orderings));
            log.debug("UserNames: {}", ConvertTools.joinToString(getUserNames(users)));
            Assert.assertTrue(users.size() > 0);
        }
    }

    private List<String> getUserNames(PageList<SysUserEntity> users) {
        List<String> userNames = new ArrayList<>();
        for (SysUserEntity user : users) {
            userNames.add(user.toDisplayName());
        }
        return userNames;
    }
}
