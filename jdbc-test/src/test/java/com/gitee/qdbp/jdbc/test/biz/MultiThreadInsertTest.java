package com.gitee.qdbp.jdbc.test.biz;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.PostConstruct;
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
import com.gitee.qdbp.jdbc.sql.SqlBuilder;
import com.gitee.qdbp.jdbc.test.model.SysSettingEntity;
import com.gitee.qdbp.jdbc.test.service.SysSettingService;

@ContextConfiguration(locations = { "classpath:settings/spring/spring.xml" })
public class MultiThreadInsertTest extends AbstractTestNGSpringContextTests {

    private Logger log = LoggerFactory.getLogger(MultiThreadInsertTest.class);

    @Autowired
    private QdbcBoot qdbcBoot;
    @Autowired
    private SysSettingService sysSettingService;

    @PostConstruct
    public void init() {
        SqlBuilder buffer = new SqlBuilder();
        buffer.ad("DELETE FROM TEST_SETTING");
        qdbcBoot.getSqlBufferJdbcOperations().update(buffer.out());
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
