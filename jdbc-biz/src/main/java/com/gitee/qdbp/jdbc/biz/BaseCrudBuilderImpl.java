package com.gitee.qdbp.jdbc.biz;

import java.util.HashMap;
import java.util.Map;
import com.gitee.qdbp.jdbc.api.BaseCrudBuilder;
import com.gitee.qdbp.jdbc.api.BaseCrudDao;
import com.gitee.qdbp.jdbc.api.SqlBufferJdbcOperations;
import com.gitee.qdbp.jdbc.model.DbVersion;
import com.gitee.qdbp.jdbc.plugins.ModelDataExecutor;
import com.gitee.qdbp.jdbc.sql.SqlBuilder;
import com.gitee.qdbp.jdbc.sql.SqlDialect;
import com.gitee.qdbp.jdbc.utils.DbTools;

/**
 * 基础增删改查对象的构造器
 *
 * @author 赵卉华
 * @version 190601
 */
public class BaseCrudBuilderImpl implements BaseCrudBuilder {

    private SqlBufferJdbcOperations sqlBufferJdbcOperations;

    private DbVersion dbVersion;
    private SqlDialect sqlDialect;
    private Map<Class<?>, BaseCrudDao<?>> daoCache = new HashMap<>();
    private Map<Class<?>, SqlBuilder> sqlBuilderCache = new HashMap<>();

    /** {@inheritDoc} **/
    @Override
    @SuppressWarnings("unchecked")
    public <T> BaseCrudDao<T> buildDao(Class<T> clazz) {
        if (daoCache.containsKey(clazz)) {
            return (BaseCrudDao<T>) daoCache.get(clazz);
        } else {
            SqlBuilder sqlBuilder = buildSqlBuilder(clazz);
            ModelDataExecutor modelExecutor = new ModelDataExecutor(clazz);
            BaseCrudDao<T> instance = new BaseCrudDaoImpl<>(clazz, sqlBuilder, modelExecutor, sqlBufferJdbcOperations);
            daoCache.put(clazz, instance);
            return instance;
        }
    }

    @Override
    public SqlDialect buildDialect() {
        if (sqlDialect == null) {
            DbVersion dbVersion = findDbVersion();
            sqlDialect = new SqlDialect(dbVersion);
        }
        return sqlDialect;
    }

    @Override
    public SqlBuilder buildSqlBuilder(Class<?> clazz) {
        if (sqlBuilderCache.containsKey(clazz)) {
            return sqlBuilderCache.get(clazz);
        } else {
            SqlBuilder instance = new SqlBuilder(clazz, buildDialect());
            sqlBuilderCache.put(clazz, instance);
            return instance;
        }
    }

    @Override
    public DbVersion findDbVersion() {
        if (dbVersion == null) {
            dbVersion = DbTools.findDbVersion(sqlBufferJdbcOperations.getJdbcOperations());
        }
        return dbVersion;
    }

    public void setSqlBufferJdbcOperations(SqlBufferJdbcOperations sqlBufferJdbcOperations) {
        this.sqlBufferJdbcOperations = sqlBufferJdbcOperations;
    }

}