package com.gitee.qdbp.jdbc.test.trans;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.jdbc.test.model.SysSettingEntity;
import com.gitee.qdbp.jdbc.test.service.SysSettingService;
import com.gitee.qdbp.jdbc.test.service.SysSettingService.TestModel;

@Test
@ContextConfiguration(locations = { "classpath:settings/spring/spring.xml" })
public class TranscationalTest extends AbstractTestNGSpringContextTests {

    private static Logger log = LoggerFactory.getLogger(TranscationalTest.class);

    @Autowired
    private SysSettingService sysSettingService;

    public void test() {
        log.debug("------------------------------------------------------");
        { // 清除全部记录
            sysSettingService.clearAllRecord();
        }
        log.debug("------------------------------------------------------");
        { // 测试新增记录1
            SysSettingEntity entity = new SysSettingEntity();
            entity.setName("TranscationalTest-" + 1);
            entity.setValue("测试 TranscationalTest-" + 1);
            sysSettingService.createSetting(entity);
        }
        log.debug("------------------------------------------------------");
        { // 测试新增记录2
            SysSettingEntity entity = new SysSettingEntity();
            entity.setName("TranscationalTest-" + 2);
            entity.setValue("测试 TranscationalTest-" + 2);
            sysSettingService.createSetting(entity);
        }
        log.debug("------------------------------------------------------");
        try { // 测试新增记录3
            SysSettingEntity entity = new SysSettingEntity();
            entity.setName("TranscationalTest-" + 3);
            entity.setValue("测试 TranscationalTest-" + 3);
            sysSettingService.createSetting(entity, TestModel.mainErrorLogSuccess);
        } catch (Exception e) {
            log.warn("Failed to createSetting for 'TranscationalTest-2', {}", e.getMessage());
        }
        log.debug("------------------------------------------------------");
        { // 查询记录总数
            int count = sysSettingService.countSetting(DbWhere.NONE);
            log.warn("Total of setting record is [{}]", count);
            // 记录1/记录2成功, 记录3失败
            Assert.assertEquals(count, 2, "Total of setting record");
        }
        log.debug("------------------------------------------------------");
    }
}
