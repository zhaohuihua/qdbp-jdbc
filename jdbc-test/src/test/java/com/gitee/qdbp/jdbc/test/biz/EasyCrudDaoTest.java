package com.gitee.qdbp.jdbc.test.biz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.jdbc.api.QdbcBoot;
import com.gitee.qdbp.jdbc.api.CrudDao;
import com.gitee.qdbp.jdbc.model.DbVersion;
import com.gitee.qdbp.jdbc.test.enums.UserState;
import com.gitee.qdbp.jdbc.test.enums.UserType;
import com.gitee.qdbp.jdbc.test.model.SysUserEntity;
import com.gitee.qdbp.jdbc.utils.ParseTools;
import com.gitee.qdbp.tools.utils.DateTools;
import com.gitee.qdbp.tools.utils.JsonTools;

@Test
@ContextConfiguration(locations = { "classpath:settings/spring/spring.xml" })
public class EasyCrudDaoTest extends AbstractTestNGSpringContextTests {

    private Logger log = LoggerFactory.getLogger(EasyCrudDaoTest.class);

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
        DbWhere where = ParseTools.parseWhereFromEntity(bean);
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
}
