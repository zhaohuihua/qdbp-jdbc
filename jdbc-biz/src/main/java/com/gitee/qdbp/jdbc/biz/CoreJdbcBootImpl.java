package com.gitee.qdbp.jdbc.biz;

import java.util.HashMap;
import java.util.Map;
import com.gitee.qdbp.jdbc.api.CoreJdbcBoot;
import com.gitee.qdbp.jdbc.api.EasyCrudDao;
import com.gitee.qdbp.jdbc.api.SqlBufferJdbcOperations;
import com.gitee.qdbp.jdbc.model.DbVersion;
import com.gitee.qdbp.jdbc.plugins.ModelDataExecutor;
import com.gitee.qdbp.jdbc.sql.build.CrudSqlBuilder;
import com.gitee.qdbp.jdbc.sql.fragment.CrudFragmentHelper;
import com.gitee.qdbp.jdbc.sql.fragment.TableCrudFragmentHelper;
import com.gitee.qdbp.jdbc.utils.DbTools;

/**
 * 基础增删改查对象的构造器
 *
 * @author 赵卉华
 * @version 190601
 */
public class CoreJdbcBootImpl implements CoreJdbcBoot {

    private SqlBufferJdbcOperations sqlBufferJdbcOperations;

    private DbVersion dbVersion;
    private Map<Class<?>, EasyCrudDao<?>> daoCache = new HashMap<>();
    private Map<Class<?>, CrudFragmentHelper> crudHelperCache = new HashMap<>();

    /** {@inheritDoc} **/
    @Override
    @SuppressWarnings("unchecked")
    public <T> EasyCrudDao<T> buildCrudDao(Class<T> clazz) {
        if (daoCache.containsKey(clazz)) {
            return (EasyCrudDao<T>) daoCache.get(clazz);
        } else {
            CrudFragmentHelper sqlHelper = buildSqlFragmentHelper(clazz);
            CrudSqlBuilder sqlBuilder = new CrudSqlBuilder(sqlHelper);
            ModelDataExecutor modelExecutor = new ModelDataExecutor(clazz);
            EasyCrudDao<T> instance = new EasyCrudDaoImpl<>(clazz, sqlBuilder, modelExecutor, sqlBufferJdbcOperations);
            daoCache.put(clazz, instance);
            return instance;
        }
    }

    @Override
    public CrudFragmentHelper buildSqlFragmentHelper(Class<?> clazz) {
        if (crudHelperCache.containsKey(clazz)) {
            return crudHelperCache.get(clazz);
        } else {
            CrudFragmentHelper instance = new TableCrudFragmentHelper(clazz);
            crudHelperCache.put(clazz, instance);
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
