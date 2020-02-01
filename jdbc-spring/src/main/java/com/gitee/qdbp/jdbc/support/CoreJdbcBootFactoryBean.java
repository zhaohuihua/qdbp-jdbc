package com.gitee.qdbp.jdbc.support;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import com.gitee.qdbp.jdbc.api.CoreJdbcBoot;
import com.gitee.qdbp.jdbc.api.SqlBufferJdbcOperations;
import com.gitee.qdbp.jdbc.biz.CoreJdbcBootImpl;
import com.gitee.qdbp.jdbc.plugins.DbPluginContainer;

/**
 * CoreJdbcBootFactoryBean<br>
 * 同时负责数据库插件初始化处理
 *
 * @author zhaohuihua
 * @version 200128
 */
public class CoreJdbcBootFactoryBean implements FactoryBean<CoreJdbcBoot>, InitializingBean, ApplicationContextAware {

    private boolean singleton = true;
    private boolean initialized = false;
    private CoreJdbcBootImpl singletonInstance;

    private ApplicationContext context;
    private SqlBufferJdbcOperations sqlBufferJdbcOperations;
    private DbPluginContainer pluginContainer;

    @Override
    public void afterPropertiesSet() {
        if (pluginContainer != null) {
            SqlBuilderScanTools.scanAndRegisterWhereSqlBuilder(pluginContainer, context);
            SqlBuilderScanTools.scanAndRegisterUpdateSqlBuilder(pluginContainer, context);
            SqlBuilderScanTools.scanAndRegisterOrderBySqlBuilder(pluginContainer, context);
            DbPluginContainer.init(pluginContainer);
        }
        if (singletonInstance != null) {
            singletonInstance.setSqlBufferJdbcOperations(sqlBufferJdbcOperations);
        }
    }

    @Override
    public CoreJdbcBoot getObject() throws Exception {
        if (isSingleton()) {
            if (!this.initialized) {
                this.singletonInstance = createInstance();
                this.initialized = true;
            }
            return this.singletonInstance;
        } else {
            return createInstance();
        }
    }

    private CoreJdbcBootImpl createInstance() {
        CoreJdbcBootImpl boot = new CoreJdbcBootImpl();
        boot.setSqlBufferJdbcOperations(sqlBufferJdbcOperations);
        return boot;
    }

    @Override
    public Class<?> getObjectType() {
        return CoreJdbcBoot.class;
    }

    public void setSingleton(boolean singleton) {
        this.singleton = singleton;
    }

    @Override
    public boolean isSingleton() {
        return singleton;
    }

    @Override
    public void setApplicationContext(ApplicationContext context) {
        this.context = context;
    }

    public ApplicationContext getApplicationContext() {
        return context;
    }

    public SqlBufferJdbcOperations getSqlBufferJdbcOperations() {
        return sqlBufferJdbcOperations;
    }

    public void setSqlBufferJdbcOperations(SqlBufferJdbcOperations sqlBufferJdbcOperations) {
        this.sqlBufferJdbcOperations = sqlBufferJdbcOperations;
    }

    public DbPluginContainer getPluginContainer() {
        return pluginContainer;
    }

    public void setPluginContainer(DbPluginContainer pluginContainer) {
        this.pluginContainer = pluginContainer;
    }

}
