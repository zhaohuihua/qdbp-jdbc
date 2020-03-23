
package com.gitee.qdbp.jdbc.biz;

import java.util.List;
import java.util.Map;
import com.gitee.qdbp.able.beans.KeyValue;
import com.gitee.qdbp.able.exception.ServiceException;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.able.jdbc.condition.DbWhere.EmptiableWhere;
import com.gitee.qdbp.able.jdbc.ordering.OrderPaging;
import com.gitee.qdbp.able.jdbc.ordering.Ordering;
import com.gitee.qdbp.able.jdbc.paging.PageList;
import com.gitee.qdbp.able.jdbc.paging.PartList;
import com.gitee.qdbp.jdbc.api.SqlBufferJdbcOperations;
import com.gitee.qdbp.jdbc.plugins.EntityFillExecutor;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.result.FirstColumnMapper;
import com.gitee.qdbp.jdbc.result.KeyIntegerMapper;
import com.gitee.qdbp.jdbc.result.RowToBeanMapper;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.build.QuerySqlBuilder;
import com.gitee.qdbp.jdbc.utils.PagingQuery;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 基础表查询操作
 *
 * @author 赵卉华
 * @version 190608
 */
public abstract class BaseQueryerImpl<T> {

    protected RowToBeanMapper<T> rowToBeanMapper;
    protected QuerySqlBuilder sqlBuilder;
    protected EntityFillExecutor entityFillExecutor;
    protected SqlBufferJdbcOperations jdbc;
    protected SqlDialect dialect;

    /**
     * 构造函数
     * 
     * @param sqlBuilder SQL生成工具
     * @param entityFillExecutor 实体业务处理接口
     * @param jdbcOperations SqlBuffer数据库操作类
     * @param rowToBeanMapper 结果转换接口
     */
    public BaseQueryerImpl(QuerySqlBuilder sqlBuilder, EntityFillExecutor entityFillExecutor,
            SqlBufferJdbcOperations jdbcOperations, RowToBeanMapper<T> rowToBeanMapper) {
        this.rowToBeanMapper = rowToBeanMapper;
        this.sqlBuilder = sqlBuilder;
        this.entityFillExecutor = entityFillExecutor;
        this.jdbc = jdbcOperations;
        this.dialect = jdbcOperations.findSqlDialect();
    }

    /**
     * 获取主表别名(用于表关联查询)
     * 
     * @return 主表别名
     */
    protected String getMajorTableAlias() {
        return null;
    }

    public T find(DbWhere where) {
        VerifyTools.requireNotBlank(where, "where");
        entityFillExecutor.fillQueryWhereDataStatus(where, getMajorTableAlias());
        SqlBuffer buffer = sqlBuilder.buildFindSql(where);
        return jdbc.queryForObject(buffer, rowToBeanMapper);
    }

    public List<T> listAll() {
        return listAll(null);
    }

    public List<T> listAll(List<Ordering> orderings) {
        DbWhere where = new DbWhere();
        entityFillExecutor.fillQueryWhereDataStatus(where, getMajorTableAlias());
        SqlBuffer buffer = sqlBuilder.buildListSql(where, orderings);
        return jdbc.query(buffer, rowToBeanMapper);
    }

    public PageList<T> list(DbWhere where, OrderPaging odpg) {
        // DbWhere readyWhere = checkWhere(where); // 带分页查询列表, 允许条件为空, 因此不检查
        DbWhere readyWhere = where;
        if (where == null || where == DbWhere.NONE) {
            readyWhere = new DbWhere();
        }
        entityFillExecutor.fillQueryWhereDataStatus(readyWhere, getMajorTableAlias());

        // WHERE条件
        SqlBuffer wsb = sqlBuilder.helper().buildWhereSql(readyWhere, true);
        return this.doList(wsb, odpg);
    }

    private PageList<T> doList(SqlBuffer wsb, OrderPaging odpg) {
        SqlBuffer qsb = sqlBuilder.buildListSql(wsb, odpg.getOrderings());
        SqlBuffer csb = null;
        if (odpg.isPaging() && odpg.isNeedCount()) {
            csb = sqlBuilder.buildCountSql(wsb);
        }

        PartList<T> list = PagingQuery.queryForList(jdbc, qsb, csb, odpg, rowToBeanMapper);
        return list == null ? null : new PageList<T>(list, list.getTotal());
    }

    public <V> V findFieldValue(String fieldName, DbWhere where, Class<V> valueClazz) throws ServiceException {
        DbWhere readyWhere = checkWhere(where);
        entityFillExecutor.fillQueryWhereDataStatus(readyWhere, getMajorTableAlias());
        PageList<V> list = doListFieldValues(fieldName, false, readyWhere, null, valueClazz);
        return VerifyTools.isBlank(list) ? null : list.get(0);
    }

    public <V> PageList<V> listFieldValues(String fieldName, boolean distinct, DbWhere where, OrderPaging odpg,
            Class<V> valueClazz) throws ServiceException {
        DbWhere readyWhere = checkWhere(where);
        entityFillExecutor.fillQueryWhereDataStatus(readyWhere, getMajorTableAlias());
        return doListFieldValues(fieldName, distinct, readyWhere, odpg, valueClazz);
    }

    private <V> PageList<V> doListFieldValues(String fieldName, boolean distinct, DbWhere where, OrderPaging odpg,
            Class<V> valueClazz) throws ServiceException {
        SqlBuffer wsb = sqlBuilder.helper().buildWhereSql(where, true);
        SqlBuffer qsb = sqlBuilder.buildListFieldValuesSql(fieldName, distinct, wsb, odpg.getOrderings());
        SqlBuffer csb = null;
        if (odpg.isPaging() && odpg.isNeedCount()) {
            csb = sqlBuilder.buildCountSql(wsb);
        }
        PartList<V> list = PagingQuery.queryForList(jdbc, qsb, csb, odpg, new FirstColumnMapper<>(valueClazz));
        return list == null ? null : new PageList<V>(list, list.getTotal());
    }

    public int count(DbWhere where) throws ServiceException {
        DbWhere readyWhere = checkWhere(where);
        entityFillExecutor.fillQueryWhereDataStatus(readyWhere, getMajorTableAlias());
        return doCount(readyWhere);
    }

    private int doCount(DbWhere readyWhere) throws ServiceException {
        SqlBuffer buffer = sqlBuilder.buildCountSql(readyWhere);
        return jdbc.queryForObject(buffer, Integer.class);
    }

    public Map<String, Integer> groupCount(String groupBy, DbWhere where) throws ServiceException {
        VerifyTools.requireNotBlank(groupBy, "groupBy");
        DbWhere readyWhere = checkWhere(where);
        entityFillExecutor.fillQueryWhereDataStatus(readyWhere, getMajorTableAlias());
        return this.doGroupCount(groupBy, readyWhere);
    }

    private Map<String, Integer> doGroupCount(String groupBy, DbWhere readyWhere) throws ServiceException {
        SqlBuffer buffer = sqlBuilder.buildGroupCountSql(groupBy, readyWhere);
        List<KeyValue<Integer>> list = jdbc.query(buffer, KEY_INTEGER_MAPPER);
        return KeyValue.toMap(list);
    }

    private static KeyIntegerMapper KEY_INTEGER_MAPPER = new KeyIntegerMapper();

    protected DbWhere checkWhere(DbWhere where) {
        if (where == null || (where.isEmpty() && !(where instanceof EmptiableWhere))) {
            String m = "where must not be " + (where == null ? "null" : "empty") + ", please use DbWhere.NONE";
            throw new IllegalArgumentException(m);
        } else if (where == DbWhere.NONE) {
            return new DbWhere();
        } else {
            return where;
        }
    }

}
