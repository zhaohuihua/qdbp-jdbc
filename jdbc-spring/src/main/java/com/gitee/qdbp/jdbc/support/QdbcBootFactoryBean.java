package com.gitee.qdbp.jdbc.support;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.core.convert.converter.GenericConverter;
import com.gitee.qdbp.jdbc.api.QdbcBoot;
import com.gitee.qdbp.jdbc.api.SqlBufferJdbcOperations;
import com.gitee.qdbp.jdbc.biz.QdbcBootImpl;
import com.gitee.qdbp.jdbc.plugins.VariableToDbValueConverter;
import com.gitee.qdbp.jdbc.plugins.DbPluginContainer;
import com.gitee.qdbp.jdbc.plugins.MapToBeanConverter;

/**
 * QdbcBootFactoryBean<br>
 * 同时负责数据库插件初始化处理
 *
 * @author zhaohuihua
 * @version 20200128
 */
public class QdbcBootFactoryBean implements FactoryBean<QdbcBoot>, InitializingBean, ApplicationContextAware {

    private boolean singleton = true;
    private boolean initialized = false;
    private QdbcBootImpl singletonInstance;

    private ApplicationContext context;
    /** Spring的类型转换处理类 **/
    private ConversionService conversionService;
    /** SqlBuffer数据库操作类 **/
    private SqlBufferJdbcOperations sqlBufferJdbcOperations;
    /** 数据库插件容器类 **/
    private DbPluginContainer pluginContainer;

    @Override
    public void afterPropertiesSet() {
        if (singletonInstance != null) {
            singletonInstance.setSqlBufferJdbcOperations(sqlBufferJdbcOperations);
        }
        if (pluginContainer != null) {
            PluginInstanceScanTools.scanAndRegisterWhereSqlBuilder(pluginContainer, context);
            PluginInstanceScanTools.scanAndRegisterUpdateSqlBuilder(pluginContainer, context);
            PluginInstanceScanTools.scanAndRegisterOrderBySqlBuilder(pluginContainer, context);
            PluginInstanceScanTools.scanAndRegisterBatchInsertExecutor(pluginContainer, context);
            PluginInstanceScanTools.scanAndRegisterBatchUpdateExecutor(pluginContainer, context);
            DbPluginContainer.init(pluginContainer);
            if (conversionService != null) {
                // 设置插件的ConversionService
                fillConversionService(pluginContainer, conversionService);
                // 如果插件是Converter, 将其注册到ConverterRegistry
                registerConverter(pluginContainer, conversionService);
            }
        }
    }

    /** 设置插件的ConversionService **/
    protected void fillConversionService(DbPluginContainer plugins, ConversionService conversionService) {
        MapToBeanConverter mapToBeanConverter = plugins.getMapToBeanConverter();
        if (mapToBeanConverter instanceof ConversionServiceAware) {
            ((ConversionServiceAware) mapToBeanConverter).setConversionService(conversionService);
        }
        VariableToDbValueConverter toDbValueConverter = plugins.getToDbValueConverter();
        if (toDbValueConverter instanceof ConversionServiceAware) {
            ((ConversionServiceAware) toDbValueConverter).setConversionService(conversionService);
        }
    }

    /** 如果插件是Converter, 将其注册到ConverterRegistry **/
    protected void registerConverter(DbPluginContainer plugins, ConversionService conversionService) {
        if (conversionService instanceof ConverterRegistry) {
            MapToBeanConverter mapToBeanConverter = plugins.getMapToBeanConverter();
            ConverterRegistry registry = (ConverterRegistry) conversionService;
            if (mapToBeanConverter instanceof GenericConverter) {
                registry.addConverter((GenericConverter) mapToBeanConverter);
            } else if (mapToBeanConverter instanceof Converter<?, ?>) {
                registry.addConverter((Converter<?, ?>) mapToBeanConverter);
            } else if (mapToBeanConverter instanceof ConverterFactory<?, ?>) {
                registry.addConverterFactory((ConverterFactory<?, ?>) mapToBeanConverter);
            }
        }
    }

    @Override
    public QdbcBoot getObject() throws Exception {
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

    private QdbcBootImpl createInstance() {
        QdbcBootImpl boot = new QdbcBootImpl();
        boot.setSqlBufferJdbcOperations(sqlBufferJdbcOperations);
        return boot;
    }

    @Override
    public Class<?> getObjectType() {
        return QdbcBoot.class;
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
