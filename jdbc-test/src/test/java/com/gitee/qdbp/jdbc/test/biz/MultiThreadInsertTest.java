package com.gitee.qdbp.jdbc.test.biz;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;
import com.gitee.qdbp.able.exception.ServiceException;
import com.gitee.qdbp.jdbc.api.QdbcBoot;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.test.model.SysSettingEntity;
import com.gitee.qdbp.jdbc.test.service.SysSettingService;

@Test
@ContextConfiguration(locations = { "classpath:settings/spring/spring.xml" })
public class MultiThreadInsertTest extends AbstractTestNGSpringContextTests {

    private Logger log = LoggerFactory.getLogger(MultiThreadInsertTest.class);

    @Autowired
    private QdbcBoot qdbcBoot;
    @Autowired
    private SysSettingService sysSettingService;

    @Test(priority = 0)
    public void testCreateTable() {
        {
            SqlBuffer buffer = new SqlBuffer();
            buffer.append("CREATE TABLE IF NOT EXISTS TEST_SETTING (");
            buffer.append("ID VARCHAR(50) NOT NULL COMMENT '主键',");
            buffer.append("NAME VARCHAR(20) NOT NULL COMMENT '名称',");
            buffer.append("VALUE VARCHAR(50) NOT NULL COMMENT '文本',");
            buffer.append("VERSION INT(8) NOT NULL DEFAULT 1 COMMENT '版本号',");
            buffer.append("REMARK VARCHAR(200) COMMENT '备注',");
            buffer.append("STATE TINYINT(1) NOT NULL COMMENT '状态',");
            buffer.append("CREATE_TIME DATETIME NOT NULL COMMENT '创建时间',");
            buffer.append("UPDATE_TIME DATETIME COMMENT '修改时间',");
            buffer.append("DATA_STATE INT(10) NOT NULL COMMENT '数据状态:0为正常|其他为删除',");
            buffer.append("PRIMARY KEY (ID),");
            buffer.append("UNIQUE KEY TEST_SETTING_NAME(NAME, DATA_STATE)");
            buffer.append(")");
            qdbcBoot.getSqlBufferJdbcOperations().update(buffer);
        }
        {
            SqlBuffer buffer = new SqlBuffer();
            buffer.append("DELETE FROM TEST_SETTING");
            qdbcBoot.getSqlBufferJdbcOperations().update(buffer);
        }
    }

    @Test(priority = 2)
    public void testInsert() {
        SysSettingEntity entity = new SysSettingEntity();
        entity.setName("setting-" + 0);
        entity.setValue("测试 setting-" + 0);
        sysSettingService.createSetting(entity, 5000L);
    }

    @Test(priority = 3)
    public void testMultiThreadInsert() {
        int size = 3;
        int times = 3;
        CountDownLatch latch = new CountDownLatch(size * times);
        AtomicInteger counter = new AtomicInteger(0);
        for (int i = 1; i <= size; i++) {
            for (int j = 1; j <= times; j++) {
                SysSettingEntity entity = new SysSettingEntity();
                entity.setName("setting-" + i);
                entity.setValue("测试 setting-" + i);
                InsertThread thread = new InsertThread(entity, latch, counter);
                thread.setName("setting-" + i + " " + j);
                thread.start();
            }
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
        }
        log.debug("MultiThreadInsertSuccess={}", counter.get());
        Assert.assertTrue(counter.get() == size);
    }

    private class InsertThread extends Thread {

        private SysSettingEntity entity;
        private CountDownLatch latch;
        private AtomicInteger counter;

        public InsertThread(SysSettingEntity entity, CountDownLatch latch, AtomicInteger counter) {
            this.entity = entity;
            this.latch = latch;
            this.counter = counter;
        }

        public void run() {
            try {
                sysSettingService.createSetting(entity, 5000L);
                counter.incrementAndGet();
            } catch (DataAccessException | ServiceException e) {
                log.error("Failed to create setting, {}, {}", entity.getName(), e.toString());
            } catch (Exception e) {
                log.error("Failed to create setting, {}", entity.getName(), e);
            } finally {
                latch.countDown();
            }
        }
    }
}
