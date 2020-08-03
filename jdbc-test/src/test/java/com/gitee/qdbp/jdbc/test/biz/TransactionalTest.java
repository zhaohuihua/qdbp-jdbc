package com.gitee.qdbp.jdbc.test.biz;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;
import com.gitee.qdbp.able.exception.ServiceException;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.jdbc.api.CrudDao;
import com.gitee.qdbp.jdbc.api.QdbcBoot;
import com.gitee.qdbp.jdbc.test.model.SysLoggerEntity;
import com.gitee.qdbp.jdbc.test.model.SysSettingEntity;
import com.gitee.qdbp.jdbc.test.service.SysSettingService;
import com.gitee.qdbp.jdbc.test.service.SysSettingService.TestModel;

/**
 * 事务测试, Setting是主事务, 记录日志为非事务执行(TransactionDefinition.PROPAGATION_NOT_SUPPORTED)
 *
 * @author zhaohuihua
 * @version 20200212
 */
@Test
@ContextConfiguration(locations = { "classpath:settings/spring/spring.xml" })
public class TransactionalTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private QdbcBoot qdbcBoot;
    @Autowired
    private SysSettingService sysSettingService;

    @PostConstruct
    public void init() {
        {
            CrudDao<SysSettingEntity> dao = qdbcBoot.buildCrudDao(SysSettingEntity.class);
            DbWhere where = new DbWhere();
            where.on("name", "starts", "Transactional");
            dao.physicalDelete(where);
        }
        {
            CrudDao<SysLoggerEntity> dao = qdbcBoot.buildCrudDao(SysLoggerEntity.class);
            DbWhere where = new DbWhere();
            where.on("content", "like", "Transactional");
            dao.physicalDelete(where);
        }
    }

    @Test(priority = 20)
    public void testSuccessToInsert() {
        String key = "Transactional-20";
        SysSettingEntity entity = new SysSettingEntity();
        entity.setName(key);
        entity.setValue("测试 " + key);
        sysSettingService.createSetting(entity, TestModel.skipLogging);
        { // 主记录成功
            DbWhere where = new DbWhere();
            where.on("name", "=", key);
            int mainCount = sysSettingService.countSetting(where);
            Assert.assertEquals(mainCount, 1);
        }
        { // 不记录日志
            DbWhere where = new DbWhere();
            where.on("content", "like", key);
            int logCount = sysSettingService.countLogging(where);
            Assert.assertEquals(logCount, 0); // 期望日志数为0
        }
    }

    @Test(priority = 30)
    public void testFailedToInsert() {
        String key = "Transactional-30";
        SysSettingEntity entity = new SysSettingEntity();
        entity.setName(key);
        entity.setValue("测试 " + key);
        try {
            sysSettingService.createSetting(entity, TestModel.mainErrorLogSuccess);
        } catch (ServiceException e) {
            Assert.assertTrue(e.getCause() instanceof DataIntegrityViolationException);
        }
        { // 主记录失败
            DbWhere where = new DbWhere();
            where.on("name", "=", key);
            int mainCount = sysSettingService.countSetting(where);
            Assert.assertEquals(mainCount, 0);
        }
        { // 日志记录成功
            DbWhere where = new DbWhere();
            where.on("content", "like", key);
            int logCount = sysSettingService.countLogging(where);
            Assert.assertEquals(logCount, 2); // 新增的日志+更新的日志
        }
    }

    @Test(priority = 40)
    public void testFailedToLogging() {
        String key = "Transactional-40";
        SysSettingEntity entity = new SysSettingEntity();
        entity.setName(key);
        entity.setValue("测试 " + key);
        sysSettingService.createSetting(entity, TestModel.mainSuccessLogError);
        { // 主记录成功
            DbWhere where = new DbWhere();
            where.on("name", "=", key);
            int mainCount = sysSettingService.countSetting(where);
            Assert.assertEquals(mainCount, 1);
        }
        { // 日志记录失败
            DbWhere where = new DbWhere();
            where.on("content", "like", key);
            int logCount = sysSettingService.countLogging(where);
            Assert.assertEquals(logCount, 1); // 新增的日志保存成功, 更新的日志保存失败
        }
    }

}
