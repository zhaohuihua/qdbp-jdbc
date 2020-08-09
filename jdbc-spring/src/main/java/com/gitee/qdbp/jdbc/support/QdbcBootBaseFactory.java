package com.gitee.qdbp.jdbc.support;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import com.gitee.qdbp.jdbc.api.QdbcBoot;
import com.gitee.qdbp.jdbc.api.SqlBufferJdbcOperations;
import com.gitee.qdbp.jdbc.biz.QdbcBootImpl;
import com.gitee.qdbp.jdbc.plugins.DbPluginContainer;

/**
 * 初始化QdbcBoot, 同时负责数据库插件初始化
 *
 * @author zhaohuihua
 * @version 20200128
 */
public class QdbcBootBaseFactory {

    private boolean singleton = true;
    private boolean initialized = false;
    private QdbcBootImpl singletonInstance;

    /** Spring的类型转换处理类 **/
    private ConversionService conversionService;
    /** 数据库插件容器类 **/
    private DbPluginContainer pluginContainer;
    /** SqlBuffer数据库操作类 **/
    private SqlBufferJdbcOperations sqlBufferJdbcOperations;

    public void afterPropertiesSet() {
        if (singletonInstance != null) {
            singletonInstance.setSqlBufferJdbcOperations(sqlBufferJdbcOperations);
        }
        if (conversionService == null) {
            conversionService = DefaultConversionService.getSharedInstance();
        }
        if (pluginContainer == null) {
            pluginContainer = new DbPluginContainer();
        }
        pluginContainer.setConversionService(conversionService);
        DbPluginContainer.init(pluginContainer);
    }

    /** 获取实例 **/
    public QdbcBoot getObject() {
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

    protected QdbcBootImpl createInstance() {
        QdbcBootImpl boot = new QdbcBootImpl();
        boot.setSqlBufferJdbcOperations(sqlBufferJdbcOperations);
        return boot;
    }

    /** 判断是否使用单例模式 **/
    public boolean isSingleton() {
        return singleton;
    }

    /** 设置是否使用单例模式 **/
    public void setSingleton(boolean singleton) {
        this.singleton = singleton;
    }

    /** Spring的类型转换处理类 **/
    public ConversionService getConversionService() {
        return conversionService;
    }

    /** Spring的类型转换处理类 **/
    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    /** SqlBuffer数据库操作类 **/
    public SqlBufferJdbcOperations getSqlBufferJdbcOperations() {
        return sqlBufferJdbcOperations;
    }

    /** SqlBuffer数据库操作类 **/
    public void setSqlBufferJdbcOperations(SqlBufferJdbcOperations sqlBufferJdbcOperations) {
        this.sqlBufferJdbcOperations = sqlBufferJdbcOperations;
    }

    /** 数据库插件容器类 **/
    public DbPluginContainer getPluginContainer() {
        return pluginContainer;
    }

    /** 数据库插件容器类 **/
    public void setPluginContainer(DbPluginContainer pluginContainer) {
        this.pluginContainer = pluginContainer;
    }
}
