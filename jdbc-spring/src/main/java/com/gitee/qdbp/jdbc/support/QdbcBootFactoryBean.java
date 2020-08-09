package com.gitee.qdbp.jdbc.support;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import com.gitee.qdbp.jdbc.api.QdbcBoot;
import com.gitee.qdbp.jdbc.plugins.DbPluginContainer;

/**
 * QdbcBootFactoryBean<br>
 * 同时负责数据库插件初始化处理
 *
 * @author zhaohuihua
 * @version 20200128
 */
public class QdbcBootFactoryBean extends QdbcBootBaseFactory
        implements FactoryBean<QdbcBoot>, InitializingBean, ApplicationContextAware {

    private ApplicationContext context;

    @Override
    public void afterPropertiesSet() {
        this.scanAndRegisterPlugin();
        super.afterPropertiesSet();
    }

    protected void scanAndRegisterPlugin() {
        DbPluginContainer pluginContainer = getPluginContainer();
        if (pluginContainer != null && context != null) {
            PluginInstanceScanTools.scanAndRegisterWhereSqlBuilder(pluginContainer, context);
            PluginInstanceScanTools.scanAndRegisterUpdateSqlBuilder(pluginContainer, context);
            PluginInstanceScanTools.scanAndRegisterOrderBySqlBuilder(pluginContainer, context);
            PluginInstanceScanTools.scanAndRegisterBatchInsertExecutor(pluginContainer, context);
            PluginInstanceScanTools.scanAndRegisterBatchUpdateExecutor(pluginContainer, context);
        }
    }

    @Override
    public Class<?> getObjectType() {
        return QdbcBoot.class;
    }

    @Override
    public void setApplicationContext(ApplicationContext context) {
        this.context = context;
    }

    public ApplicationContext getApplicationContext() {
        return context;
    }

}
