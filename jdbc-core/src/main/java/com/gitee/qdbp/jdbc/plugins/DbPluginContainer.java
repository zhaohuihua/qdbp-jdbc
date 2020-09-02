package com.gitee.qdbp.jdbc.plugins;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.core.convert.support.DefaultConversionService;
import com.gitee.qdbp.able.jdbc.base.OrderByCondition;
import com.gitee.qdbp.able.jdbc.base.UpdateCondition;
import com.gitee.qdbp.able.jdbc.base.WhereCondition;
import com.gitee.qdbp.jdbc.model.DbType;
import com.gitee.qdbp.jdbc.model.MainDbType;
import com.gitee.qdbp.jdbc.plugins.impl.BatchInsertByMultiRowsExecutor;
import com.gitee.qdbp.jdbc.plugins.impl.BatchInsertByUnionAllFromDualExecutor;
import com.gitee.qdbp.jdbc.plugins.impl.BatchOperateByForEachExecutor;
import com.gitee.qdbp.jdbc.plugins.impl.BatchUpdateByCaseWhenExecutor;
import com.gitee.qdbp.jdbc.plugins.impl.BatchUpdateByJoinUsingExecutor;
import com.gitee.qdbp.jdbc.plugins.impl.DataSourceDbVersionFinder;
import com.gitee.qdbp.jdbc.plugins.impl.FastJsonBeanToMapConverter;
import com.gitee.qdbp.jdbc.plugins.impl.FastJsonDbConditionConverter;
import com.gitee.qdbp.jdbc.plugins.impl.NoneEntityDataStateFillStrategy;
import com.gitee.qdbp.jdbc.plugins.impl.PersistenceAnnotationTableScans;
import com.gitee.qdbp.jdbc.plugins.impl.SimpleDbOperatorContainer;
import com.gitee.qdbp.jdbc.plugins.impl.SimpleEntityFieldFillStrategy;
import com.gitee.qdbp.jdbc.plugins.impl.SimpleRawValueConverter;
import com.gitee.qdbp.jdbc.plugins.impl.SimpleSqlFileScanner;
import com.gitee.qdbp.jdbc.plugins.impl.SimpleSqlFormatter;
import com.gitee.qdbp.jdbc.plugins.impl.SimpleVarToDbValueConverter;
import com.gitee.qdbp.jdbc.plugins.impl.SpringMapToBeanConverter;
import com.gitee.qdbp.jdbc.support.ConversionServiceAware;
import com.gitee.qdbp.jdbc.support.convert.NumberToBooleanConverter;
import com.gitee.qdbp.jdbc.support.convert.StringToDateConverter;
import com.gitee.qdbp.jdbc.support.enums.AllEnumConverterRegister;
import com.gitee.qdbp.staticize.tags.base.Taglib;
import com.gitee.qdbp.tools.utils.Config;
import com.gitee.qdbp.tools.utils.ConvertTools;

/**
 * 自定义插件容器
 *
 * @author zhaohuihua
 * @version 190601
 */
public class DbPluginContainer {

    private static Logger log = LoggerFactory.getLogger(DbPluginContainer.class);

    /** 全局实例 **/
    private static DbPluginContainer DEFAULTS;

    /** 初始化全局实例 **/
    public static void init(DbPluginContainer container) {
        DEFAULTS = container;
        checkAndSetDefaultPorperty(DEFAULTS);
    }

    /** 获取全局实例 **/
    public static DbPluginContainer defaults() {
        if (DEFAULTS == null) {
            DEFAULTS = InnerInstance.INSTANCE;
        }
        return DEFAULTS;
    }

    /**
     * 静态内部类单例模式, 同时解决延迟加载和并发问题(缺点是无法传参)<br>
     * 加载外部类时, 不会加载内部类, 也就不会创建实例对象;<br>
     * 只有DEFAULTS==null调用InnerInstance.INSTANCE时才会加载静态内部类;<br>
     * 加载类是线程安全的, 虚拟机保证只会装载一次内部类, 不会出现并发问题<br>
     *
     * @author zhaohuihua
     * @version 20200129
     */
    public static class InnerInstance {

        public static final DbPluginContainer INSTANCE = new DbPluginContainer();
        static {
            checkAndSetDefaultPorperty(INSTANCE);
        }
    }

    /**
     * 检查并设置默认属性<br>
     * <br>
     * 灵活就意味着可配置的细节多, 也就难以上手<br>
     * 提供一个默认版本, 用于满足基本需求, 基于约定,常用,无定制<br>
     * <br>
     * 未提供的特性包括:<br>
     * TableInfoScans.commonFieldResolver: 不会将公共字段放在查询列表的最后<br>
     * EntityFieldFillStrategy.entityFillBizResolver: 不会自动填充创建人/创建时间等业务参数<br>
     * EntityDataStateFillStrategy: 不会自动填充数据状态, 不支持逻辑删除<br>
     * 等等...
     * 
     * @param plugins 插件容器
     */
    protected static void checkAndSetDefaultPorperty(DbPluginContainer plugins) {
        if (plugins.getConversionService() == null) {
            plugins.setConversionService(DefaultConversionService.getSharedInstance());
        }
        // 初始化默认的类型转换处理器
        initDefaultConverter(plugins);

        if (plugins.getAvailableDbTypes() == null) {
            plugins.addAvailableDbType(MainDbType.class);
        }
        if (plugins.getSqlTaglib() == null) {
            plugins.setSqlTaglib(new Taglib("classpath:settings/dbtags/taglib.txt"));
        }
        if (plugins.getTableInfoScans() == null) {
            plugins.setTableInfoScans(new PersistenceAnnotationTableScans());
        }
        if (plugins.getEntityFieldFillStrategy() == null) {
            plugins.setEntityFieldFillStrategy(new SimpleEntityFieldFillStrategy());
        }
        if (plugins.getEntityDataStateFillStrategy() == null) {
            plugins.setEntityDataStateFillStrategy(new NoneEntityDataStateFillStrategy());
        }
        if (plugins.getRawValueConverter() == null) {
            plugins.setRawValueConverter(new SimpleRawValueConverter());
        }
        if (plugins.getToDbValueConverter() == null) {
            plugins.setToDbValueConverter(new SimpleVarToDbValueConverter());
        }
        if (plugins.getMapToBeanConverter() == null) {
            // 由于fastjson的TypeUtils.castToEnum()逻辑存在硬伤, 无法做到数字枚举值的自定义转换
            // container.setMapToBeanConverter(new FastJsonMapToBeanConverter());
            // 改为SpringMapToBeanConverter
            SpringMapToBeanConverter converter = new SpringMapToBeanConverter();
            converter.setConversionService(plugins.getConversionService());
            plugins.setMapToBeanConverter(converter);
        }
        if (plugins.getBeanToMapConverter() == null) {
            plugins.setBeanToMapConverter(new FastJsonBeanToMapConverter());
        }
        if (plugins.getDbConditionConverter() == null) {
            plugins.setDbConditionConverter(new FastJsonDbConditionConverter());
        }
        if (plugins.getOperatorContainer() == null) {
            plugins.setOperatorContainer(new SimpleDbOperatorContainer());
        }
        if (plugins.getSqlFormatter() == null) {
            plugins.setSqlFormatter(new SimpleSqlFormatter());
        }
        if (plugins.getDbVersionFinder() == null) {
            plugins.setDbVersionFinder(new DataSourceDbVersionFinder());
        }
        if (plugins.getSqlFileScanner() == null) {
            plugins.setSqlFileScanner(new SimpleSqlFileScanner("settings/sqls/", "*.sql"));
        }
        if (plugins.getDefaultBatchInsertExecutor() == null) {
            plugins.setDefaultBatchInsertExecutor(new BatchOperateByForEachExecutor());
        }
        if (plugins.getDefaultBatchUpdateExecutor() == null) {
            plugins.setDefaultBatchUpdateExecutor(new BatchOperateByForEachExecutor());
        }
        // 初始化公共的批量操作处理器(专用的放前面,通用的放后面)
        if (plugins.getBatchInsertExecutors().isEmpty()) {
            plugins.addBatchInsertExecutor(new BatchInsertByUnionAllFromDualExecutor());
            plugins.addBatchInsertExecutor(new BatchInsertByMultiRowsExecutor());
        }
        if (plugins.getBatchUpdateExecutors().isEmpty()) {
            plugins.addBatchUpdateExecutor(new BatchUpdateByJoinUsingExecutor());
            plugins.addBatchUpdateExecutor(new BatchUpdateByCaseWhenExecutor());
        }

        // 设置插件的ConversionService
        fillConversionService(plugins);
        // 如果插件是Converter, 将其注册到ConverterRegistry
        registerConverter(plugins);
    }

    protected static void initDefaultConverter(DbPluginContainer plugins) {
        ConversionService conversionService = plugins.getConversionService();
        if (conversionService instanceof ConverterRegistry) {
            ConverterRegistry registry = (ConverterRegistry) conversionService;
            if (!conversionService.canConvert(String.class, Date.class)) {
                registry.addConverter(new StringToDateConverter());
            }
            // oracle, 如果数字字段定义的类型是SMALLINT, 将会返回BigDecimal
            if (!conversionService.canConvert(BigDecimal.class, Boolean.class)) {
                registry.addConverter(new NumberToBooleanConverter());
            }
            // 注册其他枚举值转换处理类: oracle的BigDecimal转Enum等
            AllEnumConverterRegister.registerEnumConverterFactory(registry);
        }
    }

    /** 设置插件的ConversionService **/
    protected static void fillConversionService(DbPluginContainer plugins) {
        ConversionService conversionService = plugins.getConversionService();
        if (conversionService != null) {
            MapToBeanConverter mapToBeanConverter = plugins.getMapToBeanConverter();
            if (mapToBeanConverter instanceof ConversionServiceAware) {
                ((ConversionServiceAware) mapToBeanConverter).setConversionService(conversionService);
            }
            VariableToDbValueConverter toDbValueConverter = plugins.getToDbValueConverter();
            if (toDbValueConverter instanceof ConversionServiceAware) {
                ((ConversionServiceAware) toDbValueConverter).setConversionService(conversionService);
            }
        }
    }

    /** 如果插件是Converter, 将其注册到ConverterRegistry **/
    protected static void registerConverter(DbPluginContainer plugins) {
        ConversionService conversionService = plugins.getConversionService();
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

    /** 数据库配置选项 **/
    private Config dbConfig;

    /** 设置数据库配置选项 **/
    public void setDbConfig(Properties config) {
        this.dbConfig = new Config(config);
    }

    /** 设置数据库配置选项 **/
    public void addDbConfig(String key, String value) {
        if (dbConfig == null) {
            dbConfig = new Config();
        }
        this.dbConfig.put(key, value);
    }

    /** 获取数据库配置选项 **/
    public Config getDbConfig() {
        return this.dbConfig;
    }

    /**
     * 获取数据库配置选项
     * 
     * @param force 是否强制返回非空对象
     * @return 配置选项
     */
    public Config getDbConfig(boolean force) {
        if (dbConfig == null) {
            dbConfig = new Config();
        }
        return this.dbConfig;
    }

    /** 可用的数据库类型 **/
    private List<DbType> availableDbTypes;

    /** 可用的数据库类型 **/
    public List<DbType> getAvailableDbTypes() {
        return availableDbTypes;
    }

    /** 可用的数据库类型 **/
    public void setAvailableDbTypes(List<DbType> dbTypes) {
        this.availableDbTypes = dbTypes;
    }

    /** 可用的数据库类型 **/
    public <E extends Enum<?>> void addAvailableDbType(Class<E> clazz) {
        if (!DbType.class.isAssignableFrom(clazz)) {
            String msg = clazz.getName() + " is not assignable for " + DbType.class.getName();
            throw new IllegalArgumentException(msg);
        }
        E[] array = clazz.getEnumConstants();
        if (array.length == 0) {
            return;
        }
        // key=DbTypeLowerCase, value=DbTypeSourceDesc
        Map<String, String> oldDbTypes = new HashMap<>();
        if (this.availableDbTypes == null) {
            this.availableDbTypes = new ArrayList<>();
        } else {
            for (DbType item : this.availableDbTypes) {
                oldDbTypes.put(item.name().toLowerCase(), item.getClass().getName() + '.' + item.name());
            }
        }
        List<String> conflicts = new ArrayList<>();
        for (E item : array) {
            String dbKey = item.name().toLowerCase();
            if (oldDbTypes.containsKey(dbKey)) {
                String fmt = "%s.%s conflict with %s";
                conflicts.add(String.format(fmt, item.getClass().getName(), item.name(), oldDbTypes.get(dbKey)));
                continue;
            }
            this.availableDbTypes.add((DbType) item);
            oldDbTypes.put(dbKey, item.getClass().getName() + '.' + item.name());
        }
        if (!conflicts.isEmpty()) {
            log.warn(ConvertTools.joinToString(conflicts, "\n\t"));
        }
    }

    /** SQL标签库 **/
    private Taglib sqlTaglib;

    /** 获取SQL标签库 **/
    public Taglib getSqlTaglib() {
        return sqlTaglib;
    }

    /** 设置SQL标签库 **/
    public void setSqlTaglib(Taglib taglib) {
        this.sqlTaglib = taglib;
    }

    /** 设置SQL标签库路径 **/
    public void setSqlTaglibPath(String taglibPath) {
        this.sqlTaglib = new Taglib(taglibPath);
    }

    /** Spring的类型转换处理类 **/
    private ConversionService conversionService;

    /** Spring的类型转换处理类 **/
    public ConversionService getConversionService() {
        return conversionService;
    }

    /** Spring的类型转换处理类 **/
    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    /** 数据表和列信息扫描类 **/
    private TableInfoScans tableInfoScans;

    /** 数据表和列信息扫描类 **/
    public void setTableInfoScans(TableInfoScans tableInfoScans) {
        this.tableInfoScans = tableInfoScans;
    }

    /** 数据表和列信息扫描类 **/
    public TableInfoScans getTableInfoScans() {
        return tableInfoScans;
    }

    /** 实体类字段数据填充策略 **/
    private EntityFieldFillStrategy entityFieldFillStrategy;
    /** 实体类逻辑删除数据状态填充策略 **/
    private EntityDataStateFillStrategy<?> dataStateFillStrategy;

    /** 实体类字段数据填充策略 **/
    public void setEntityFieldFillStrategy(EntityFieldFillStrategy entityFieldFillStrategy) {
        this.entityFieldFillStrategy = entityFieldFillStrategy;
    }

    /** 实体类逻辑删除数据状态填充策略 **/
    public EntityFieldFillStrategy getEntityFieldFillStrategy() {
        return entityFieldFillStrategy;
    }

    /** 实体类逻辑删除数据状态填充策略 **/
    public EntityDataStateFillStrategy<?> getEntityDataStateFillStrategy() {
        return dataStateFillStrategy;
    }

    /** 实体类逻辑删除数据状态填充策略 **/
    public void setEntityDataStateFillStrategy(EntityDataStateFillStrategy<?> dataStateFillStrategy) {
        this.dataStateFillStrategy = dataStateFillStrategy;
    }

    /** 数据库原生值的转换处理类(sysdate, CURRENT_TIMESTAMP等) **/
    private RawValueConverter rawValueConverter;

    /** 数据库原生值的转换处理类(sysdate, CURRENT_TIMESTAMP等) **/
    public RawValueConverter getRawValueConverter() {
        return rawValueConverter;
    }

    /** 数据库原生值的转换处理类(sysdate, CURRENT_TIMESTAMP等) **/
    public void setRawValueConverter(RawValueConverter rawValueConverter) {
        this.rawValueConverter = rawValueConverter;
    }

    /** 数据转换处理类 **/
    private VariableToDbValueConverter toDbValueConverter;

    /** 数据转换处理类 **/
    public void setToDbValueConverter(VariableToDbValueConverter toDbValueConverter) {
        this.toDbValueConverter = toDbValueConverter;
    }

    /** 数据转换处理类 **/
    public VariableToDbValueConverter getToDbValueConverter() {
        return toDbValueConverter;
    }

    /** Map到JavaBean的转换处理类 **/
    private MapToBeanConverter mapToBeanConverter;

    /** Map到JavaBean的转换处理类 **/
    public void setMapToBeanConverter(MapToBeanConverter mapToBeanConverter) {
        this.mapToBeanConverter = mapToBeanConverter;
    }

    /** Map到JavaBean的转换处理类 **/
    public MapToBeanConverter getMapToBeanConverter() {
        return mapToBeanConverter;
    }

    /** JavaBean到Map的转换处理类 **/
    private BeanToMapConverter beanToMapConverter;

    /** JavaBean到Map的转换处理类 **/
    public void setBeanToMapConverter(BeanToMapConverter beanToMapConverter) {
        this.beanToMapConverter = beanToMapConverter;
    }

    /** JavaBean到Map的转换处理类 **/
    public BeanToMapConverter getBeanToMapConverter() {
        return beanToMapConverter;
    }

    /** JavaBean到数据库条件的转换处理类 **/
    private DbConditionConverter dbConditionConverter;

    /** JavaBean到数据库条件的转换处理类 **/
    public void setDbConditionConverter(DbConditionConverter dbConditionConverter) {
        this.dbConditionConverter = dbConditionConverter;
    }

    /** JavaBean到数据库条件的转换处理类 **/
    public DbConditionConverter getDbConditionConverter() {
        return dbConditionConverter;
    }

    /** 数据库运算符容器 **/
    private DbOperatorContainer operatorContainer;

    /** 数据库运算符容器 **/
    public void setOperatorContainer(DbOperatorContainer operatorContainer) {
        this.operatorContainer = operatorContainer;
    }

    /** 数据库运算符容器 **/
    public DbOperatorContainer getOperatorContainer() {
        return operatorContainer;
    }

    /** SQL格式化接口 **/
    private SqlFormatter sqlFormatter;

    /** SQL格式化接口 **/
    public void setSqlFormatter(SqlFormatter sqlFormatter) {
        this.sqlFormatter = sqlFormatter;
    }

    /** SQL格式化接口 **/
    public SqlFormatter getSqlFormatter() {
        return sqlFormatter;
    }

    /** 数据库版本信息查询接口 **/
    private DbVersionFinder dbVersionFinder;

    /** 数据库版本信息查询接口 **/
    public void setDbVersionFinder(DbVersionFinder dbVersionFinder) {
        this.dbVersionFinder = dbVersionFinder;
    }

    /** 数据库版本信息查询接口 **/
    public DbVersionFinder getDbVersionFinder() {
        return dbVersionFinder;
    }

    /** SQL模板扫描接口 **/
    private SqlFileScanner sqlFileScanner;

    /** SQL模板扫描接口 **/
    public SqlFileScanner getSqlFileScanner() {
        return sqlFileScanner;
    }

    /** SQL模板扫描接口 **/
    public void setSqlFileScanner(SqlFileScanner sqlFileScanner) {
        this.sqlFileScanner = sqlFileScanner;
    }

    /** 默认的批量新增处理类 **/
    private BatchInsertExecutor defaultBatchInsertExecutor;
    /** 默认的批量更新处理类 **/
    private BatchUpdateExecutor defaultBatchUpdateExecutor;
    /** 批量新增处理类列表 **/
    private List<BatchInsertExecutor> batchInsertExecutors = new ArrayList<>();
    /** 批量更新处理类列表 **/
    private List<BatchUpdateExecutor> batchUpdateExecutors = new ArrayList<>();

    /** 获取默认的批量新增处理类 **/
    public BatchInsertExecutor getDefaultBatchInsertExecutor() {
        return defaultBatchInsertExecutor;
    }

    /** 设置默认的批量新增处理类 **/
    public void setDefaultBatchInsertExecutor(BatchInsertExecutor batchInsertExecutor) {
        this.defaultBatchInsertExecutor = batchInsertExecutor;
    }

    /** 获取批量新增处理类列表 **/
    public List<BatchInsertExecutor> getBatchInsertExecutors() {
        return this.batchInsertExecutors;
    }

    /** 设置批量新增处理类列表 **/
    public void setBatchInsertExecutors(List<BatchInsertExecutor> batchInsertExecutors) {
        this.batchInsertExecutors = batchInsertExecutors;
    }

    /** 增加新增处理类 **/
    public void addBatchInsertExecutor(BatchInsertExecutor batchInsertExecutor) {
        this.batchInsertExecutors.add(batchInsertExecutor);
    }

    /** 获取默认的批量更新处理类 **/
    public BatchUpdateExecutor getDefaultBatchUpdateExecutor() {
        return defaultBatchUpdateExecutor;
    }

    /** 设置默认的批量更新处理类 **/
    public void setDefaultBatchUpdateExecutor(BatchUpdateExecutor batchUpdateExecutor) {
        this.defaultBatchUpdateExecutor = batchUpdateExecutor;
    }

    /** 获取批量更新处理类列表 **/
    public List<BatchUpdateExecutor> getBatchUpdateExecutors() {
        return this.batchUpdateExecutors;
    }

    /** 设置批量更新处理类列表 **/
    public void setBatchUpdateExecutors(List<BatchUpdateExecutor> batchUpdateExecutors) {
        this.batchUpdateExecutors = batchUpdateExecutors;
    }

    /** 增加更新处理类 **/
    public void addBatchUpdateExecutor(BatchUpdateExecutor batchUpdateExecutor) {
        this.batchUpdateExecutors.add(batchUpdateExecutor);
    }

    /** Where条件的自定义条件构造器 **/
    private List<WhereSqlBuilder<? extends WhereCondition>> whereSqlBuilders = new ArrayList<>();

    /** 增加Where条件的自定义条件构造器 **/
    public <T extends WhereCondition> void addWhereSqlBuilder(WhereSqlBuilder<T> builder) {
        whereSqlBuilders.add(builder);
    }

    /** 设置Where条件的自定义条件构造器 **/
    public <T extends WhereCondition> void setWhereSqlBuilders(List<WhereSqlBuilder<T>> builders) {
        whereSqlBuilders.clear();
        whereSqlBuilders.addAll(builders);
    }

    /** 获取Where条件的自定义条件构造器 **/
    // JDK8+
    // public <T extends WhereCondition, C extends T, B extends WhereSqlBuilder<T>> B getWhereSqlBuilder(Class<C> type) {
    @SuppressWarnings("unchecked")
    public <T extends WhereCondition, B extends WhereSqlBuilder<T>> B getWhereSqlBuilder(Class<T> type) {
        for (WhereSqlBuilder<? extends WhereCondition> item : whereSqlBuilders) {
            if (item.supported() == type) {
                return (B) item;
            }
        }
        for (WhereSqlBuilder<? extends WhereCondition> item : whereSqlBuilders) {
            if (item.supported() != null && item.supported().isAssignableFrom(type)) {
                return (B) item;
            }
        }
        return null;
    }

    /** DbUpdate条件的自定义条件构造器 **/
    private List<UpdateSqlBuilder<? extends UpdateCondition>> UpdateSqlBuilders = new ArrayList<>();

    /** 增加DbUpdate条件的自定义条件构造器 **/
    public <T extends UpdateCondition> void addUpdateSqlBuilder(UpdateSqlBuilder<T> builder) {
        UpdateSqlBuilders.add(builder);
    }

    /** 设置DbUpdate条件的自定义条件构造器 **/
    public <T extends UpdateCondition> void setUpdateSqlBuilders(List<UpdateSqlBuilder<T>> builders) {
        UpdateSqlBuilders.clear();
        UpdateSqlBuilders.addAll(builders);
    }

    /** 获取DbUpdate条件的自定义条件构造器 **/
    // JDK8+
    // public <T extends UpdateCondition, C extends T, B extends UpdateSqlBuilder<T>> B getUpdateSqlBuilder(Class<C> type) {
    @SuppressWarnings("unchecked")
    public <T extends UpdateCondition, B extends UpdateSqlBuilder<T>> B getUpdateSqlBuilder(Class<T> type) {
        for (UpdateSqlBuilder<? extends UpdateCondition> item : UpdateSqlBuilders) {
            if (item.supported() == type) {
                return (B) item;
            }
        }
        for (UpdateSqlBuilder<? extends UpdateCondition> item : UpdateSqlBuilders) {
            if (item.supported() != null && item.supported().isAssignableFrom(type)) {
                return (B) item;
            }
        }
        return null;
    }

    /** 排序条件的自定义条件构造器 **/
    private List<OrderBySqlBuilder<? extends OrderByCondition>> OrderBySqlBuilders = new ArrayList<>();

    /** 增加排序条件的自定义条件构造器 **/
    public <T extends OrderByCondition> void addOrderBySqlBuilder(OrderBySqlBuilder<T> builder) {
        OrderBySqlBuilders.add(builder);
    }

    /** 设置排序条件的自定义条件构造器 **/
    public <T extends OrderByCondition> void setOrderBySqlBuilders(List<OrderBySqlBuilder<T>> builders) {
        OrderBySqlBuilders.clear();
        OrderBySqlBuilders.addAll(builders);
    }

    /** 获取排序条件的自定义条件构造器 **/
    @SuppressWarnings("unchecked")
    public <T extends OrderByCondition, B extends OrderBySqlBuilder<T>> B getOrderBySqlBuilder(Class<T> type) {
        for (OrderBySqlBuilder<? extends OrderByCondition> item : OrderBySqlBuilders) {
            if (item.supported() == type) {
                return (B) item;
            }
        }
        for (OrderBySqlBuilder<? extends OrderByCondition> item : OrderBySqlBuilders) {
            if (item.supported() != null && item.supported().isAssignableFrom(type)) {
                return (B) item;
            }
        }
        return null;
    }
}
