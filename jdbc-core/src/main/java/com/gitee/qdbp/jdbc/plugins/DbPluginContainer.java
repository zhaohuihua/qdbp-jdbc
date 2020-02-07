package com.gitee.qdbp.jdbc.plugins;

import java.util.ArrayList;
import java.util.List;
import com.gitee.qdbp.able.jdbc.base.OrderByCondition;
import com.gitee.qdbp.able.jdbc.base.UpdateCondition;
import com.gitee.qdbp.able.jdbc.base.WhereCondition;
import com.gitee.qdbp.jdbc.plugins.impl.DataSourceDbVersionFinder;
import com.gitee.qdbp.jdbc.plugins.impl.FastJsonDbConditionConverter;
import com.gitee.qdbp.jdbc.plugins.impl.FastJsonMapToBeanConverter;
import com.gitee.qdbp.jdbc.plugins.impl.SimpleDbOperatorContainer;
import com.gitee.qdbp.jdbc.plugins.impl.SimpleEntityFillHandler;
import com.gitee.qdbp.jdbc.plugins.impl.SimpleSqlFormatter;
import com.gitee.qdbp.jdbc.plugins.impl.SimpleTableInfoScans;
import com.gitee.qdbp.jdbc.plugins.impl.SimpleVarToDbValueConverter;

/**
 * 自定义插件容器
 *
 * @author zhaohuihua
 * @version 190601
 */
public class DbPluginContainer {

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
     * @version 200129
     */
    public static class InnerInstance {

        public static final DbPluginContainer INSTANCE = new DbPluginContainer();
        static {
            checkAndSetDefaultPorperty(INSTANCE);
        }
    }

    private static void checkAndSetDefaultPorperty(DbPluginContainer container) {
        if (container.getTableInfoScans() == null) {
            container.setTableInfoScans(new SimpleTableInfoScans());
        }
        if (container.getEntityFillHandler() == null) {
            container.setEntityFillHandler(new SimpleEntityFillHandler<>());
        }
        if (container.getToDbValueConverter() == null) {
            container.setToDbValueConverter(new SimpleVarToDbValueConverter());
        }
        if (container.getMapToBeanConverter() == null) {
            container.setMapToBeanConverter(new FastJsonMapToBeanConverter());
        }
        if (container.getDbConditionConverter() == null) {
            container.setDbConditionConverter(new FastJsonDbConditionConverter());
        }
        if (container.getOperatorContainer() == null) {
            container.setOperatorContainer(new SimpleDbOperatorContainer());
        }
        if (container.getSqlFormatter() == null) {
            container.setSqlFormatter(new SimpleSqlFormatter());
        }
        if (container.getDbVersionFinder() == null) {
            container.setDbVersionFinder(new DataSourceDbVersionFinder());
        }
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

    /** 实体数据填充处理类 **/
    private EntityFillHandler entityFillHandler;

    /** 实体数据填充处理类 **/
    public void setEntityFillHandler(EntityFillHandler entityFillHandler) {
        this.entityFillHandler = entityFillHandler;
    }

    /** 实体数据填充处理类 **/
    public EntityFillHandler getEntityFillHandler() {
        return entityFillHandler;
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

    private List<WhereSqlBuilder<? extends WhereCondition>> whereSqlBuilders = new ArrayList<>();

    public <T extends WhereCondition> void addWhereSqlBuilder(WhereSqlBuilder<T> builder) {
        whereSqlBuilders.add(builder);
    }

    public <T extends WhereCondition> void setWhereSqlBuilder(List<WhereSqlBuilder<T>> builders) {
        whereSqlBuilders.clear();
        whereSqlBuilders.addAll(builders);
    }

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

    private List<UpdateSqlBuilder<? extends UpdateCondition>> UpdateSqlBuilders = new ArrayList<>();

    public <T extends UpdateCondition> void addUpdateSqlBuilder(UpdateSqlBuilder<T> builder) {
        UpdateSqlBuilders.add(builder);
    }

    public <T extends UpdateCondition> void setUpdateSqlBuilder(List<UpdateSqlBuilder<T>> builders) {
        UpdateSqlBuilders.clear();
        UpdateSqlBuilders.addAll(builders);
    }

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

    private List<OrderBySqlBuilder<? extends OrderByCondition>> OrderBySqlBuilders = new ArrayList<>();

    public <T extends OrderByCondition> void addOrderBySqlBuilder(OrderBySqlBuilder<T> builder) {
        OrderBySqlBuilders.add(builder);
    }

    public <T extends OrderByCondition> void setOrderBySqlBuilder(List<OrderBySqlBuilder<T>> builders) {
        OrderBySqlBuilders.clear();
        OrderBySqlBuilders.addAll(builders);
    }

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
