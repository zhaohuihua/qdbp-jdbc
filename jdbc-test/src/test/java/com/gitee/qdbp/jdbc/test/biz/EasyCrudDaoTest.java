package com.gitee.qdbp.jdbc.test.biz;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;
import com.gitee.qdbp.jdbc.api.CoreJdbcBoot;
import com.gitee.qdbp.jdbc.model.DbVersion;

@Test
@ContextConfiguration(locations = { "classpath:settings/spring/spring.xml" })
public class EasyCrudDaoTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private CoreJdbcBoot coreJdbcBoot;

    @Test
    public void testVersionQuery() {
        DbVersion version = coreJdbcBoot.findDbVersion();
        System.out.println(version);
        Assert.assertNotNull(version);
    }
}
