package com.gitee.qdbp.jdbc.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import com.gitee.qdbp.jdbc.plugins.BatchInsertExecutor;
import com.gitee.qdbp.jdbc.plugins.BatchUpdateExecutor;
import com.gitee.qdbp.jdbc.plugins.DbPluginContainer;
import com.gitee.qdbp.jdbc.plugins.OrderBySqlBuilder;
import com.gitee.qdbp.jdbc.plugins.UpdateSqlBuilder;
import com.gitee.qdbp.jdbc.plugins.WhereSqlBuilder;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * DbPlugin扫描工具
 *
 * @author zhaohuihua
 * @version 20200129
 */
public class DbPluginScanTools {

    private static final Logger log = LoggerFactory.getLogger(DbPluginScanTools.class);

    /** 扫描并注册所有的WhereSqlBuilder子类 **/
    public static void scanAndRegisterWhereSqlBuilder(DbPluginContainer plugins, ApplicationContext context) {
        // 查找所有的WhereSqlBuilder的子类
        Map<String, ?> whereBuilderInstances = context.getBeansOfType(WhereSqlBuilder.class);
        if (VerifyTools.isNotBlank(whereBuilderInstances)) {
            List<String> registered = new ArrayList<>();
            for (Object item : whereBuilderInstances.values()) {
                WhereSqlBuilder<?> builder = (WhereSqlBuilder<?>) item;
                plugins.addWhereSqlBuilder(builder);
                registered.add(builder.supported().getSimpleName());
            }
            log.debug("DbPluginContainer.defaults().setWhereSqlBuilders({})", registered);
        }
    }

    /** 扫描并注册所有的UpdateSqlBuilder子类 **/
    public static void scanAndRegisterUpdateSqlBuilder(DbPluginContainer plugins, ApplicationContext context) {
        // 查找所有的UpdateSqlBuilder的子类
        Map<String, ?> updateBuilderInstances = context.getBeansOfType(UpdateSqlBuilder.class);
        if (VerifyTools.isNotBlank(updateBuilderInstances)) {
            List<String> registered = new ArrayList<>();
            for (Object item : updateBuilderInstances.values()) {
                UpdateSqlBuilder<?> builder = (UpdateSqlBuilder<?>) item;
                plugins.addUpdateSqlBuilder(builder);
                registered.add(builder.supported().getSimpleName());
            }
            log.debug("DbPluginContainer.defaults().setUpdateSqlBuilders({})", registered);
        }
    }

    /** 扫描并注册所有的OrderBySqlBuilder子类 **/
    public static void scanAndRegisterOrderBySqlBuilder(DbPluginContainer plugins, ApplicationContext context) {
        // 查找所有的OrderBySqlBuilder的子类
        Map<String, ?> orderByBuilderInstances = context.getBeansOfType(OrderBySqlBuilder.class);
        if (VerifyTools.isNotBlank(orderByBuilderInstances)) {
            List<String> registered = new ArrayList<>();
            for (Object item : orderByBuilderInstances.values()) {
                OrderBySqlBuilder<?> builder = (OrderBySqlBuilder<?>) item;
                plugins.addOrderBySqlBuilder(builder);
                registered.add(builder.supported().getSimpleName());
            }
            log.debug("DbPluginContainer.defaults().setOrderBySqlBuilders({})", registered);
        }
    }

    /** 扫描并注册所有的BatchInsertExecutor子类 **/
    public static void scanAndRegisterBatchInsertExecutor(DbPluginContainer plugins, ApplicationContext context) {
        // 查找所有的BatchInsertExecutor的子类
        Map<String, ?> batchInsertExecutorInstances = context.getBeansOfType(BatchInsertExecutor.class);
        if (VerifyTools.isNotBlank(batchInsertExecutorInstances)) {
            List<String> registered = new ArrayList<>();
            for (Object item : batchInsertExecutorInstances.values()) {
                BatchInsertExecutor executor = (BatchInsertExecutor) item;
                plugins.addBatchInsertExecutor(executor);
                registered.add(executor.getClass().getSimpleName());
            }
            log.debug("DbPluginContainer.defaults().setBatchInsertExecutors({})", registered);
        }
    }

    /** 扫描并注册所有的WhereSqlBuilder子类 **/
    public static void scanAndRegisterBatchUpdateExecutor(DbPluginContainer plugins, ApplicationContext context) {
        // 查找所有的BatchUpdateExecutor的子类
        Map<String, ?> batchUpdateExecutorInstances = context.getBeansOfType(BatchUpdateExecutor.class);
        if (VerifyTools.isNotBlank(batchUpdateExecutorInstances)) {
            List<String> registered = new ArrayList<>();
            for (Object item : batchUpdateExecutorInstances.values()) {
                BatchUpdateExecutor executor = (BatchUpdateExecutor) item;
                plugins.addBatchUpdateExecutor(executor);
                registered.add(executor.getClass().getSimpleName());
            }
            log.debug("DbPluginContainer.defaults().setBatchUpdateExecutors({})", registered);
        }
    }

}
