package com.gitee.qdbp.jdbc.biz;

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import com.gitee.qdbp.able.jdbc.condition.TableJoin;
import com.gitee.qdbp.jdbc.api.CrudDao;
import com.gitee.qdbp.jdbc.api.JoinQueryer;
import com.gitee.qdbp.jdbc.api.QdbcBoot;
import com.gitee.qdbp.jdbc.api.SqlBufferJdbcOperations;
import com.gitee.qdbp.jdbc.api.SqlDao;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.build.CrudSqlBuilder;
import com.gitee.qdbp.jdbc.sql.build.QuerySqlBuilder;
import com.gitee.qdbp.jdbc.sql.fragment.CrudFragmentHelper;
import com.gitee.qdbp.jdbc.sql.fragment.TableCrudFragmentHelper;
import com.gitee.qdbp.jdbc.sql.fragment.TableJoinFragmentHelper;

/**
 * 基础增删改查对象的构造器
 *
 * @author 赵卉华
 * @version 190601
 */
public class QdbcBootImpl implements QdbcBoot {

    /** SqlBuffer数据库操作类 **/
    private SqlBufferJdbcOperations sqlBufferJdbcOperations;

    private Map<Class<?>, CrudDao<?>> crudDaoCache = new HashMap<>();
    private Map<String, JoinQueryer<?>> joinQueryCache = new HashMap<>();
    /** 批量执行时的大小限制常量(0为无限制) **/
    protected static int DEFAULT_BATCH_SIZE = 500;
    /** 批量执行时的默认大小限制(0为无限制) **/
    protected int defaultBatchSize = DEFAULT_BATCH_SIZE;

    public QdbcBootImpl() {
    }

    public QdbcBootImpl(SqlBufferJdbcOperations operations) {
        this.setSqlBufferJdbcOperations(operations);
    }

    /** 批量执行时的默认大小限制(0为无限制) **/
    public int getDefaultBatchSize() {
        return defaultBatchSize;
    }

    /** 批量执行时的默认大小限制(0为无限制) **/
    public void setDefaultBatchSize(int batchSize) {
        this.defaultBatchSize = batchSize;
        for (CrudDao<?> item : crudDaoCache.values()) {
            if (item instanceof CrudDaoImpl) {
                ((CrudDaoImpl<?>) item).setDefaultBatchSize(defaultBatchSize);
            }
        }
    }

    /** {@inheritDoc} **/
    @Override
    @SuppressWarnings("unchecked")
    public <T> CrudDao<T> buildCrudDao(Class<T> clazz) {
        if (crudDaoCache.containsKey(clazz)) {
            return (CrudDao<T>) crudDaoCache.get(clazz);
        } else {
            CrudDaoImpl<T> instance = new CrudDaoImpl<>(clazz, sqlBufferJdbcOperations);
            instance.setDefaultBatchSize(this.defaultBatchSize);
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
            JoinQueryer<T> instance = new JoinQueryerImpl<>(tables, resultType, sqlBufferJdbcOperations);
            joinQueryCache.put(cacheKey, instance);
            return instance;
        }
    }

    /** {@inheritDoc} **/
    @Override
    public CrudSqlBuilder buildSqlBuilder(Class<?> clazz) {
        SqlDialect dialect = sqlBufferJdbcOperations.findSqlDialect();
        CrudFragmentHelper sqlHelper = new TableCrudFragmentHelper(clazz, dialect);
        return new CrudSqlBuilder(sqlHelper);
    }

    /** {@inheritDoc} **/
    @Override
    public QuerySqlBuilder buildSqlBuilder(TableJoin tables) {
        SqlDialect dialect = sqlBufferJdbcOperations.findSqlDialect();
        TableJoinFragmentHelper sqlHelper = new TableJoinFragmentHelper(tables, dialect);
        return new QuerySqlBuilder(sqlHelper);
    }

    /** {@inheritDoc} **/
    @Override
    public SqlDao getSqlDao() {
        return sqlBufferJdbcOperations == null ? null : sqlBufferJdbcOperations.getSqlDao();
    }

    private String buildCacheKey(TableJoin tables, Class<?> resultType) {
        return TableJoin.buildCacheKey(tables, false) + '>' + resultType;
    }

    /** {@inheritDoc} **/
    @Override
    public SqlDialect getSqlDialect() {
        return sqlBufferJdbcOperations == null ? null : sqlBufferJdbcOperations.findSqlDialect();
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBufferJdbcOperations getSqlBufferJdbcOperations() {
        return this.sqlBufferJdbcOperations;
    }

    public void setSqlBufferJdbcOperations(SqlBufferJdbcOperations sqlBufferJdbcOperations) {
        this.sqlBufferJdbcOperations = sqlBufferJdbcOperations;
    }

    /**
     * 根据数据库构造QdbcBoot
     * 
     * @param datasource 数据源
     * @return QdbcBoot
     * @since 3.1.1
     */
    public static QdbcBootImpl buildWith(DataSource datasource) {
        JdbcTemplate jdbcOperations = new JdbcTemplate(datasource);
        return buildWith(jdbcOperations);
    }

    /**
     * 根据JdbcOperations构造QdbcBoot
     * 
     * @param jdbcOperations JdbcOperations
     * @return QdbcBoot
     * @since 3.1.1
     */
    public static QdbcBootImpl buildWith(JdbcOperations jdbcOperations) {
        NamedParameterJdbcOperations operations = new NamedParameterJdbcTemplate(jdbcOperations);
        return buildWith(operations);
    }

    /**
     * 根据JdbcOperations构造QdbcBoot
     * 
     * @param jdbcOperations NamedParameterJdbcOperations
     * @return QdbcBoot
     * @since 3.1.1
     */
    public static QdbcBootImpl buildWith(NamedParameterJdbcOperations jdbcOperations) {
        SqlBufferJdbcOperations options = new SqlBufferJdbcTemplate(jdbcOperations);
        return new QdbcBootImpl(options);
    }
}
