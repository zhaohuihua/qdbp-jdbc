package com.gitee.qdbp.jdbc.test.biz;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.jdbc.api.QdbcBoot;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
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
        {
            SqlBuffer buffer = new SqlBuffer();
            buffer.append("CREATE TABLE IF NOT EXISTS TEST_LOGGER (");
            buffer.append("ID VARCHAR(50) NOT NULL COMMENT '主键',");
            buffer.append("NAME VARCHAR(20) COMMENT '名称',");
            buffer.append("CONTENT TEXT NOT NULL COMMENT '内容',");
            buffer.append("CREATE_TIME DATETIME NOT NULL COMMENT '创建时间',");
            buffer.append("DATA_STATE INT(10) NOT NULL COMMENT '数据状态:0为正常|其他为删除',");
            buffer.append("PRIMARY KEY (ID)");
            buffer.append(")");
            qdbcBoot.getSqlBufferJdbcOperations().update(buffer);
        }
        {
            SqlBuffer buffer = new SqlBuffer();
            buffer.append("DELETE FROM TEST_LOGGER");
            qdbcBoot.getSqlBufferJdbcOperations().update(buffer);
        }
    }

    @Test(priority = 20)
    public void testSuccessToInsert() {
        String key = "setting-20";
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
            Assert.assertEquals(logCount, 0);
        }
    }

    @Test(priority = 30)
    public void testFailedToInsert() {
        String key = "setting-30";
        SysSettingEntity entity = new SysSettingEntity();
        entity.setName(key);
        entity.setValue("测试 " + key);
        try {
            sysSettingService.createSetting(entity, TestModel.mainErrorLogSuccess);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof DataIntegrityViolationException);
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
        String key = "setting-40";
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
