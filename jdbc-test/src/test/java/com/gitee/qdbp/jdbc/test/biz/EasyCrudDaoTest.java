package com.gitee.qdbp.jdbc.test.biz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.jdbc.api.CoreJdbcBoot;
import com.gitee.qdbp.jdbc.api.EasyCrudDao;
import com.gitee.qdbp.jdbc.model.DbVersion;
import com.gitee.qdbp.jdbc.plugins.DbPluginContainer;
import com.gitee.qdbp.jdbc.plugins.impl.SimpleTableInfoScans;
import com.gitee.qdbp.jdbc.plugins.impl.StaticFieldTableNameScans;
import com.gitee.qdbp.jdbc.test.enums.AccountType;
import com.gitee.qdbp.jdbc.test.enums.UserState;
import com.gitee.qdbp.jdbc.test.model.UserCoreBean;
import com.gitee.qdbp.jdbc.utils.ParseTools;
import com.gitee.qdbp.tools.utils.DateTools;
import com.gitee.qdbp.tools.utils.JsonTools;

@Test
@ContextConfiguration(locations = { "classpath:settings/spring/spring.xml" })
public class EasyCrudDaoTest extends AbstractTestNGSpringContextTests {
    
    private Logger log = LoggerFactory.getLogger(EasyCrudDaoTest.class);

    @Autowired
    private CoreJdbcBoot coreJdbcBoot;

    @BeforeTest
    public void init() {
        SimpleTableInfoScans tableInfoScans = new SimpleTableInfoScans();
        tableInfoScans.setTableNameScans(new StaticFieldTableNameScans("TABLE"));
        DbPluginContainer.global.registerTableInfoScans(tableInfoScans);
    }

    @Test
    public void testVersionQuery() {
        DbVersion version = coreJdbcBoot.getSqlBufferJdbcOperations().findDbVersion();
        log.debug("DbVersion: {}", version);
        Assert.assertNotNull(version);
    }

    @Test
    public void testUserBeanQuery() {
        UserCoreBean bean = new UserCoreBean();
        bean.setUserCode("super");
        bean.setSuperman(true);
        bean.setUserType(AccountType.ADMIN);
        bean.setUserState(UserState.NORMAL);
        DbWhere where = ParseTools.parseWhereFromEntity(bean);
        EasyCrudDao<UserCoreBean> dao = coreJdbcBoot.buildCrudDao(UserCoreBean.class);
        UserCoreBean user = dao.find(where);
        log.debug("UserBeanQuery: {}", JsonTools.toLogString(user));
        Assert.assertNotNull(user);
    }

    @Test
    public void testUserWhereQuery() {
        DbWhere where = new DbWhere();
        where.on("userCode", "=", "super");
        where.on("userType", "=", AccountType.ADMIN);
        where.on("superman", "=", true);
        where.on("userState", "=", UserState.NORMAL);
        where.on("createTime", ">=", DateTools.parse("2017-01-01"));
        EasyCrudDao<UserCoreBean> dao = coreJdbcBoot.buildCrudDao(UserCoreBean.class);
        UserCoreBean user = dao.find(where);
        log.debug("UserWhereQuery: {}", JsonTools.toLogString(user));
        Assert.assertNotNull(user);
    }
}
