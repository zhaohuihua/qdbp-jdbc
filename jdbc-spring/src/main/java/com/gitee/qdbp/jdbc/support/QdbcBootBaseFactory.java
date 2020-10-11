package com.gitee.qdbp.jdbc.support;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import com.gitee.qdbp.jdbc.api.QdbcBoot;
import com.gitee.qdbp.jdbc.api.SqlBufferJdbcOperations;
import com.gitee.qdbp.jdbc.biz.QdbcBootImpl;
import com.gitee.qdbp.jdbc.plugins.DbPluginContainer;
import com.gitee.qdbp.jdbc.sql.parse.SqlFragmentContainer;

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
    /**
     * 是否在系统启动时扫描SQL文件<br>
     * 如果启动时不扫描, 则首次获取SQL模板时会扫描(很慢)<br>
     * 所以, 一般情况下都应该配置为true, 除非系统中未使用SQL模板
     */
    private boolean sqlTemplateScanOnStartup = true;

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

        if (sqlTemplateScanOnStartup) {
            SqlFragmentContainer.defaults().scanSqlFiles();
        }
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

    /** 是否在系统启动时扫描SQL文件 **/
    public boolean isSqlTemplateScanOnStartup() {
        return sqlTemplateScanOnStartup;
    }

    /**
     * 设置是否在系统启动时扫描SQL文件<br>
     * 如果启动时不扫描, 则首次获取SQL模板时会扫描(很慢)<br>
     * 所以, 一般情况下都应该配置为true, 除非系统中未使用SQL模板
     * 
     * @param sqlTemplateScanOnStartup 是否在系统启动时扫描SQL文件
     */
    public void setSqlTemplateScanOnStartup(boolean sqlTemplateScanOnStartup) {
        this.sqlTemplateScanOnStartup = sqlTemplateScanOnStartup;
    }

}
