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
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.able.jdbc.ordering.OrderPaging;
import com.gitee.qdbp.jdbc.api.QdbcBoot;
import com.gitee.qdbp.jdbc.sql.SqlBuilder;
import com.gitee.qdbp.jdbc.test.enums.SettingState;
import com.gitee.qdbp.jdbc.test.service.SysSettingService;
import com.gitee.qdbp.tools.utils.ConvertTools;

@ContextConfiguration(locations = { "classpath:settings/spring/spring.xml" })
public class MultiThreadQueryTest extends AbstractTestNGSpringContextTests {

    private Logger log = LoggerFactory.getLogger(MultiThreadQueryTest.class);

    @Autowired
    private QdbcBoot qdbcBoot;
    @Autowired
    private SysSettingService sysSettingService;

    @PostConstruct
    public void init() {
        {
            SqlBuilder buffer = new SqlBuilder();
            buffer.ad("CREATE TABLE IF NOT EXISTS TEST_SETTING (");
            buffer.ad("ID VARCHAR(50) NOT NULL COMMENT '主键',");
            buffer.ad("NAME VARCHAR(20) NOT NULL COMMENT '名称',");
            buffer.ad("VALUE VARCHAR(50) NOT NULL COMMENT '文本',");
            buffer.ad("VERSION INT(8) NOT NULL DEFAULT 1 COMMENT '版本号',");
            buffer.ad("REMARK VARCHAR(200) COMMENT '备注',");
            buffer.ad("STATE TINYINT(1) NOT NULL COMMENT '状态',");
            buffer.ad("CREATE_TIME DATETIME NOT NULL COMMENT '创建时间',");
            buffer.ad("UPDATE_TIME DATETIME COMMENT '修改时间',");
            buffer.ad("DATA_STATE INT(10) NOT NULL COMMENT '数据状态:0为正常|其他为删除',");
            buffer.ad("PRIMARY KEY (ID),");
            buffer.ad("UNIQUE KEY TEST_SETTING_NAME(NAME, DATA_STATE)");
            buffer.ad(")");
            qdbcBoot.getSqlBufferJdbcOperations().update(buffer.out());
        }
        {
            SqlBuilder buffer = new SqlBuilder();
            buffer.ad("DELETE FROM TEST_SETTING");
            qdbcBoot.getSqlBufferJdbcOperations().update(buffer.out());
        }
    }

    @Test(priority = 2)
    public void testQuery() {
        DbWhere where = new DbWhere();
        where.on("state", "=", SettingState.ENABLED);
        sysSettingService.listSetting(where, OrderPaging.NONE, 5000L);
    }

    @Test(priority = 3)
    public void testMultiThreadInsert() {
        int size = 25;
        int count = size * 2;
        CountDownLatch latch = new CountDownLatch(count);
        AtomicInteger counter = new AtomicInteger(0);
        for (int i = 1; i <= size; i++) {
            int times = 1;
            DbWhere where = new DbWhere();
            where.on("name", "=", "setting-" + i);
            where.on("state", "=", SettingState.ENABLED);
            QueryThread thread = new QueryThread(where, latch, counter, 2000L);
            thread.setName("setting-" + i + " group-" + times);
            thread.start();
        }
        try {
            Thread.sleep(50L);
        } catch (InterruptedException e) {
        }
        for (int i = 1; i <= size; i++) {
            int times = 2;
            DbWhere where = new DbWhere();
            where.on("name", "=", "setting-" + i);
            where.on("state", "=", SettingState.ENABLED);
            QueryThread thread = new QueryThread(where, latch, counter, 0L);
            thread.setName("setting-" + i + " group-" + times);
            thread.start();
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
        }
        log.debug("MultiThreadQuerySuccess={}/{}", counter.get(), count);
        Assert.assertTrue(counter.get() == count);
    }

    private class QueryThread extends Thread {

        private DbWhere where;
        private CountDownLatch latch;
        private AtomicInteger counter;
        private long sleepMills;
        private long startTime;

        public QueryThread(DbWhere where, CountDownLatch latch, AtomicInteger counter, long sleepMills) {
            this.where = where;
            this.latch = latch;
            this.counter = counter;
            this.sleepMills = sleepMills;
            this.startTime = System.currentTimeMillis();
        }

        public void run() {
            try {
                sysSettingService.listSetting(where, OrderPaging.NONE, sleepMills);
                counter.incrementAndGet();
                log.debug("time consumed {}", ConvertTools.toDuration(System.currentTimeMillis() - startTime, true));
            } catch (DataAccessException | ServiceException e) {
                log.error("Failed to query setting, {}", e.toString());
            } catch (Exception e) {
                log.error("Failed to query setting", e);
            } finally {
                latch.countDown();
            }
        }
    }
}
