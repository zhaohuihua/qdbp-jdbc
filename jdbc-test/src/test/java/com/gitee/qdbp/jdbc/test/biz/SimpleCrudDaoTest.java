package com.gitee.qdbp.jdbc.test.biz;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.able.jdbc.ordering.OrderPaging;
import com.gitee.qdbp.able.jdbc.ordering.Orderings;
import com.gitee.qdbp.able.jdbc.paging.PageList;
import com.gitee.qdbp.able.jdbc.paging.Paging;
import com.gitee.qdbp.jdbc.api.CrudDao;
import com.gitee.qdbp.jdbc.api.QdbcBoot;
import com.gitee.qdbp.jdbc.model.DbVersion;
import com.gitee.qdbp.jdbc.test.enums.UserState;
import com.gitee.qdbp.jdbc.test.enums.UserType;
import com.gitee.qdbp.jdbc.test.model.SysUserEntity;
import com.gitee.qdbp.jdbc.utils.ParseTools;
import com.gitee.qdbp.tools.utils.ConvertTools;
import com.gitee.qdbp.tools.utils.DateTools;
import com.gitee.qdbp.tools.utils.JsonTools;

@Test
@ContextConfiguration(locations = { "classpath:settings/spring/spring.xml" })
public class SimpleCrudDaoTest extends AbstractTestNGSpringContextTests {

    private Logger log = LoggerFactory.getLogger(SimpleCrudDaoTest.class);

    @Autowired
    private QdbcBoot qdbcBoot;

    @Test(priority = 2)
    public void testVersionQuery() {
        DbVersion version = qdbcBoot.getSqlBufferJdbcOperations().findDbVersion();
        log.debug("DbVersion: {}", version);
        Assert.assertNotNull(version);
    }

    @Test(priority = 3)
    public void testUserBeanQuery() {
        SysUserEntity bean = new SysUserEntity();
        bean.setUserCode("super");
        bean.setSuperman(true);
        bean.setUserType(UserType.ADMIN);
        bean.setUserState(UserState.NORMAL);
        DbWhere where = ParseTools.parseBeanToDbWhere(bean);
        CrudDao<SysUserEntity> dao = qdbcBoot.buildCrudDao(SysUserEntity.class);
        SysUserEntity user = dao.find(where);
        log.debug("UserBeanQueryResult: {}", JsonTools.toLogString(user));
        Assert.assertNotNull(user);
    }

    @Test(priority = 4)
    public void testUserWhereQuery() {
        DbWhere where = new DbWhere();
        where.on("userCode", "=", "super");
        where.on("superman", "=", true);
        where.on("userType", "=", UserType.ADMIN);
        where.on("userState", "in", UserState.NORMAL, UserState.LOCKED);
        where.on("createTime", ">=", DateTools.parse("2017-01-01"));
        CrudDao<SysUserEntity> dao = qdbcBoot.buildCrudDao(SysUserEntity.class);
        SysUserEntity user = dao.find(where);
        log.debug("UserWhereQueryResult: {}", JsonTools.toLogString(user));
        Assert.assertNotNull(user);
    }

    @Test(priority = 5)
    public void testUserOrQuery() {
        // @formatter:off
        DbWhere where = new DbWhere();
        where.on("userType", "=", UserType.ADMIN);
        where.on("userState", "in", UserState.NORMAL, UserState.LOCKED);
        where.on("createTime", ">=", DateTools.parse("2017-01-01"));
        where.sub("or")
            .on("userCode", "=", "kelly")
            .on("superman", "=", true);
        // @formatter:on
        CrudDao<SysUserEntity> dao = qdbcBoot.buildCrudDao(SysUserEntity.class);
        PageList<SysUserEntity> users = dao.list(where, OrderPaging.NONE);
        log.debug("UserOrQueryResult: {}", JsonTools.toLogString(users));
        Assert.assertTrue(users.size() > 0);
    }

    @Test(priority = 6)
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
