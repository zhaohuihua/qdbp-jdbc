package com.gitee.qdbp.jdbc.plugins;

import java.util.ArrayList;
import java.util.List;
import com.gitee.qdbp.able.model.db.OrderByCondition;
import com.gitee.qdbp.able.model.db.WhereCondition;
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

    private ModelDataHandler modelDataHandler;

    public void registerModelDataHandler(ModelDataHandler modelDataHandler) {
        this.modelDataHandler = modelDataHandler;
    }

    public ModelDataHandler getModelDataHandler() {
        return modelDataHandler;
    }

    private DbVersionFinder dbVersionFinder;

    public void registerDbVersionFinder(DbVersionFinder dbVersionFinder) {
        this.dbVersionFinder = dbVersionFinder;
    }

    public DbVersionFinder getDbVersionFinder() {
        return dbVersionFinder;
    }

    private List<WhereSqlBuilder<? extends WhereCondition>> whereSqlBuilders = new ArrayList<>();

    public <T extends WhereCondition> void registerWhereSqlBuilder(WhereSqlBuilder<T> builder) {
        whereSqlBuilders.add(builder);
    }

    public <T extends WhereCondition> void registerWhereSqlBuilder(List<WhereSqlBuilder<T>> builders) {
        whereSqlBuilders.addAll(builders);
    }

    @SuppressWarnings("unchecked")
    public <T extends WhereCondition, B extends WhereSqlBuilder<T>> B getWhereSqlBuilder(Class<T> type) {
        // public <T extends WhereCondition, C extends T, B extends WhereSqlBuilder<T>> B getWhereSqlBuilder(Class<C> type) {
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
