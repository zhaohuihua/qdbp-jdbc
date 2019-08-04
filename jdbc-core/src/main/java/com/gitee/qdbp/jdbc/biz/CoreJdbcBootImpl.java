package com.gitee.qdbp.jdbc.biz;

import java.util.HashMap;
import java.util.Map;
import com.gitee.qdbp.able.jdbc.condition.TableJoin;
import com.gitee.qdbp.jdbc.api.CoreJdbcBoot;
import com.gitee.qdbp.jdbc.api.EasyCrudDao;
import com.gitee.qdbp.jdbc.api.EasyJoinQuery;
import com.gitee.qdbp.jdbc.api.SqlBufferJdbcOperations;
import com.gitee.qdbp.jdbc.utils.DbTools;

/**
 * 基础增删改查对象的构造器
 *
 * @author 赵卉华
 * @version 190601
 */
public class CoreJdbcBootImpl implements CoreJdbcBoot {

    private SqlBufferJdbcOperations sqlBufferJdbcOperations;

    private Map<Class<?>, EasyCrudDao<?>> crudDaoCache = new HashMap<>();
    private Map<String, EasyJoinQuery<?>> joinQueryCache = new HashMap<>();

    /** {@inheritDoc} **/
    @Override
    @SuppressWarnings("unchecked")
    public <T> EasyCrudDao<T> buildCrudDao(Class<T> clazz) {
        if (crudDaoCache.containsKey(clazz)) {
            return (EasyCrudDao<T>) crudDaoCache.get(clazz);
        } else {
            EasyCrudDao<T> instance = new EasyCrudDaoImpl<>(clazz, sqlBufferJdbcOperations);
            crudDaoCache.put(clazz, instance);
            return instance;
        }
    }

    /** {@inheritDoc} **/
    @Override
    @SuppressWarnings("unchecked")
    public <T> EasyJoinQuery<T> buildJoinQuery(TableJoin tables, Class<T> resultType) {
        String cacheKey = DbTools.buildCacheKey(tables);
        if (joinQueryCache.containsKey(cacheKey)) {
            return (EasyJoinQuery<T>) joinQueryCache.get(cacheKey);
        } else {
            EasyJoinQuery<T> instance = new EasyJoinQueryImpl<>(tables, resultType, sqlBufferJdbcOperations);
            joinQueryCache.put(cacheKey, instance);
            return instance;
        }
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBufferJdbcOperations getSqlBufferJdbcOperations() {
        return this.sqlBufferJdbcOperations;
    }

    public void setSqlBufferJdbcOperations(SqlBufferJdbcOperations sqlBufferJdbcOperations) {
        this.sqlBufferJdbcOperations = sqlBufferJdbcOperations;
    }

}
