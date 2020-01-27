package com.gitee.qdbp.jdbc.plugins;

import java.util.ArrayList;
import java.util.List;
import com.gitee.qdbp.able.jdbc.base.OrderByCondition;
import com.gitee.qdbp.able.jdbc.base.UpdateCondition;
import com.gitee.qdbp.able.jdbc.base.WhereCondition;
import com.gitee.qdbp.jdbc.plugins.impl.DataSourceDbVersionFinder;
import com.gitee.qdbp.jdbc.plugins.impl.SimpleDataConvertHelper;
import com.gitee.qdbp.jdbc.plugins.impl.SimpleDbOperatorContainer;
import com.gitee.qdbp.jdbc.plugins.impl.SimpleModelDataHandler;
import com.gitee.qdbp.jdbc.plugins.impl.SimpleSqlFormatter;
import com.gitee.qdbp.jdbc.plugins.impl.SimpleTableInfoScans;

/**
 * 自定义插件容器
 *
 * @author zhaohuihua
 * @version 190601
 */
public class DbPluginContainer {

    public static final DbPluginContainer global = new DbPluginContainer();

    private DbOperatorContainer operatorContainer = new SimpleDbOperatorContainer();

    public void registerOperatorContainer(DbOperatorContainer operatorContainer) {
        this.operatorContainer = operatorContainer;
    }

    public DbOperatorContainer getOperatorContainer() {
        return operatorContainer;
    }

    private SqlFormatter sqlFormatter = new SimpleSqlFormatter();

    public void registerSqlFormatter(SqlFormatter sqlFormatter) {
        this.sqlFormatter = sqlFormatter;
    }

    public SqlFormatter getSqlFormatter() {
        return sqlFormatter;
    }

    private TableInfoScans tableInfoScans = new SimpleTableInfoScans();

    public void registerTableInfoScans(TableInfoScans tableInfoScans) {
        this.tableInfoScans = tableInfoScans;
    }

    public TableInfoScans getTableInfoScans() {
        return tableInfoScans;
    }

    private ModelDataHandler modelDataHandler = new SimpleModelDataHandler<>();

    public void registerModelDataHandler(ModelDataHandler modelDataHandler) {
        this.modelDataHandler = modelDataHandler;
    }

    public ModelDataHandler getModelDataHandler() {
        return modelDataHandler;
    }

    private DbVersionFinder dbVersionFinder = new DataSourceDbVersionFinder();

    public void registerDbVersionFinder(DbVersionFinder dbVersionFinder) {
        this.dbVersionFinder = dbVersionFinder;
    }

    public DbVersionFinder getDbVersionFinder() {
        return dbVersionFinder;
    }

    private DataConvertHelper dataConvertHelper = new SimpleDataConvertHelper();

    public void registerDataConvertHelper(DataConvertHelper dataConvertHelper) {
        this.dataConvertHelper = dataConvertHelper;
    }

    public DataConvertHelper getDataConvertHelper() {
        return dataConvertHelper;
    }

    private List<WhereSqlBuilder<? extends WhereCondition>> whereSqlBuilders = new ArrayList<>();

    public <T extends WhereCondition> void registerWhereSqlBuilder(WhereSqlBuilder<T> builder) {
        whereSqlBuilders.add(builder);
    }

    public <T extends WhereCondition> void registerWhereSqlBuilder(List<WhereSqlBuilder<T>> builders) {
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

    public <T extends UpdateCondition> void registerUpdateSqlBuilder(UpdateSqlBuilder<T> builder) {
        UpdateSqlBuilders.add(builder);
    }

    public <T extends UpdateCondition> void registerUpdateSqlBuilder(List<UpdateSqlBuilder<T>> builders) {
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

    public <T extends OrderByCondition> void registerOrderBySqlBuilder(OrderBySqlBuilder<T> builder) {
        OrderBySqlBuilders.add(builder);
    }

    public <T extends OrderByCondition> void registerOrderBySqlBuilder(List<OrderBySqlBuilder<T>> builders) {
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
