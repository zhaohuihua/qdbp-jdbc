package com.gitee.qdbp.jdbc.test.service;

import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import com.gitee.qdbp.able.jdbc.condition.DbUpdate;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.able.jdbc.ordering.OrderPaging;
import com.gitee.qdbp.able.jdbc.paging.PageList;
import com.gitee.qdbp.jdbc.api.CrudDao;
import com.gitee.qdbp.jdbc.api.QdbcBoot;
import com.gitee.qdbp.jdbc.test.enums.SettingState;
import com.gitee.qdbp.jdbc.test.model.SysLoggerEntity;
import com.gitee.qdbp.jdbc.test.model.SysSettingEntity;

@Service
public class SysSettingService {

    private static Logger log = LoggerFactory.getLogger(SysSettingService.class);

    /** 测试模式 **/
    public static enum TestModel {
        /** 不记日志 **/
        skipLogging,
        /** 主操作成功+日志失败 **/
        mainSuccessLogError,
        /** 主操作失败+日志成功 **/
        mainErrorLogSuccess
    }

    @Autowired
    private QdbcBoot qdbcBoot;
    @Autowired
    private DataSourceTransactionManager transactionManager;

    @Transactional(readOnly = true)
    public int countLogging(DbWhere where) {
        CrudDao<SysLoggerEntity> dao = qdbcBoot.buildCrudDao(SysLoggerEntity.class);
        return dao.count(where);
    }

    @Transactional(readOnly = true)
    public int countSetting(DbWhere where) {
        CrudDao<SysSettingEntity> dao = qdbcBoot.buildCrudDao(SysSettingEntity.class);
        return dao.count(where);
    }

    @Transactional(readOnly = true)
    public PageList<SysSettingEntity> listSetting(DbWhere where, OrderPaging odpg) {
        CrudDao<SysSettingEntity> dao = qdbcBoot.buildCrudDao(SysSettingEntity.class);
        return dao.list(where, odpg);
    }

    @Transactional(readOnly = true)
    public PageList<SysSettingEntity> listSetting(DbWhere where, OrderPaging odpg, long sleepMills) {
        log.debug("Before query setting");
        CrudDao<SysSettingEntity> dao = qdbcBoot.buildCrudDao(SysSettingEntity.class);
        PageList<SysSettingEntity> list = dao.list(where, odpg);
        if (sleepMills > 0) {
            try {
                // 业务处理
                log.debug("Start to business handle");
                Thread.sleep(sleepMills);
                log.debug("Success to business handle");
            } catch (InterruptedException e) {
            }
        }
        log.debug("Finished to query setting, rows={}", list.getTotal());
        return list;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public String createSetting(SysSettingEntity entity, long sleepMills) {
        return createSetting(entity, sleepMills, TestModel.skipLogging);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public String createSetting(SysSettingEntity entity, TestModel testModel) {
        return createSetting(entity, 0, TestModel.skipLogging);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public String createSetting(SysSettingEntity entity, long sleepMills, TestModel testModel) {
        // 新增数据
        log.debug("Before insert setting");
        entity.setState(SettingState.DISABLED);
        CrudDao<SysSettingEntity> dao = qdbcBoot.buildCrudDao(SysSettingEntity.class);
        String id = dao.insert(entity, true);
        log.debug("Success to insert setting, id={}", id);
        if (testModel != TestModel.skipLogging) {
            recordLogger("SysSetting", "Success to create " + entity.getName(), true);
        }

        if (sleepMills > 0) {
            try {
                // 业务处理
                log.debug("Start to business handle");
                Thread.sleep(sleepMills);
                log.debug("Success to business handle");
            } catch (InterruptedException e) {
            }
        }

        // 更新数据
        boolean logSuccess = testModel != TestModel.mainSuccessLogError;
        DbUpdate ud = new DbUpdate();
        ud.set("state", SettingState.ENABLED);
        ud.set("updateTime", new Date());
        if (testModel == TestModel.mainErrorLogSuccess) {
            // 利用必填字段制造异常
            ud.toNull("name");
        }
        DbWhere where = new DbWhere();
        where.on("id", "=", id);
        try {
            dao.update(ud, where, true, true);
            if (testModel != TestModel.skipLogging) {
                log.debug("Success to update setting, id={}", id);
                recordLogger("SysSetting", "Success to update setting for " + entity.getName(), logSuccess);
            }
            return id;
        } catch (Throwable e) {
            log.debug("Failed to update setting, id={}", id);
            if (testModel != TestModel.skipLogging) {
                recordLogger("SysSetting", "Failed to update setting for " + entity.getName(), logSuccess);
            }
            throw e;
        }
    }

    private void recordLogger(String name, String content, boolean success) {
        // 非事务执行, 如果已在事务中则挂起存在的事务
        int transLevel = TransactionDefinition.PROPAGATION_NOT_SUPPORTED;
        TransactionStatus ts = transactionManager.getTransaction(new DefaultTransactionDefinition(transLevel));
        CrudDao<SysLoggerEntity> dao = qdbcBoot.buildCrudDao(SysLoggerEntity.class);
        SysLoggerEntity entity = new SysLoggerEntity();
        entity.setName(name);
        if (success) {
            // 如果模拟成功就设置必填字段, 否则利用必填字段为空制造异常
            entity.setContent(content);
        }
        try {
            String id = dao.insert(entity, true);
            log.debug("Success to save logger, content={}, id={}", content, id);
            transactionManager.commit(ts);
        } catch (Exception e) {
            log.warn("Failed to save logger, content={}, {}", content, e.toString());
            transactionManager.rollback(ts);
        }
    }
}
