package com.gitee.qdbp.jdbc.biz;

import java.util.HashMap;
import java.util.Map;
import org.springframework.core.convert.ConversionService;
import com.gitee.qdbp.able.jdbc.condition.TableJoin;
import com.gitee.qdbp.jdbc.api.QdbcBoot;
import com.gitee.qdbp.jdbc.api.CrudDao;
import com.gitee.qdbp.jdbc.api.JoinQueryer;
import com.gitee.qdbp.jdbc.api.SqlBufferJdbcOperations;

/**
 * 基础增删改查对象的构造器
 *
 * @author 赵卉华
 * @version 190601
 */
public class QdbcBootImpl implements QdbcBoot {

    /** Spring的类型转换处理类 **/
    private ConversionService conversionService;
    /** SqlBuffer数据库操作类 **/
    private SqlBufferJdbcOperations sqlBufferJdbcOperations;

    private Map<Class<?>, CrudDao<?>> crudDaoCache = new HashMap<>();
    private Map<String, JoinQueryer<?>> joinQueryCache = new HashMap<>();

    /** {@inheritDoc} **/
    @Override
    @SuppressWarnings("unchecked")
    public <T> CrudDao<T> buildCrudDao(Class<T> clazz) {
        if (crudDaoCache.containsKey(clazz)) {
            return (CrudDao<T>) crudDaoCache.get(clazz);
        } else {
            CrudDao<T> instance = new EasyCrudDaoImpl<>(clazz, sqlBufferJdbcOperations);
            crudDaoCache.put(clazz, instance);
            return instance;
        }
    }

    /** {@inheritDoc} **/
    @Override
    @SuppressWarnings("unchecked")
    public <T> JoinQueryer<T> buildJoinQuery(TableJoin tables, Class<T> resultType) {
        String cacheKey = buildCacheKey(tables, resultType);
        if (joinQueryCache.containsKey(cacheKey)) {
            return (JoinQueryer<T>) joinQueryCache.get(cacheKey);
        } else {
            JoinQueryer<T> instance = new EasyJoinQueryImpl<>(tables, resultType, sqlBufferJdbcOperations);
            joinQueryCache.put(cacheKey, instance);
            return instance;
        }
    }

    private String buildCacheKey(TableJoin tables, Class<?> resultType) {
        return TableJoin.buildCacheKey(tables, false) + '>' + resultType;
    }

    /** Spring的类型转换处理类 **/
    public ConversionService getConversionService() {
        return conversionService;
    }

    /** Spring的类型转换处理类 **/
    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
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
