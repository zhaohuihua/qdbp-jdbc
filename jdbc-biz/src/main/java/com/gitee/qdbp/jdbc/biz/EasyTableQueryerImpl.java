
package com.gitee.qdbp.jdbc.biz;

import java.util.List;
import java.util.Map;
import com.gitee.qdbp.able.beans.KeyValue;
import com.gitee.qdbp.able.exception.ServiceException;
import com.gitee.qdbp.able.model.ordering.OrderPaging;
import com.gitee.qdbp.able.model.ordering.Ordering;
import com.gitee.qdbp.able.model.paging.PageList;
import com.gitee.qdbp.able.model.paging.PartList;
import com.gitee.qdbp.jdbc.api.SqlBufferJdbcOperations;
import com.gitee.qdbp.jdbc.condition.DbWhere;
import com.gitee.qdbp.jdbc.condition.DbWhere.EmptyDbWhere;
import com.gitee.qdbp.jdbc.plugins.ModelDataExecutor;
import com.gitee.qdbp.jdbc.result.FirstColumnMapper;
import com.gitee.qdbp.jdbc.result.KeyIntegerMapper;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.build.QuerySqlBuilder;
import com.gitee.qdbp.jdbc.utils.DbTools;
import com.gitee.qdbp.jdbc.utils.PagingQuery;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 基础表查询操作
 *
 * @author 赵卉华
 * @version 190608
 */
public abstract class EasyTableQueryerImpl<T>{

    protected Class<T> resultType;
    protected QuerySqlBuilder sqlBuilder;
    protected ModelDataExecutor modelDataExecutor;
    protected SqlBufferJdbcOperations jdbc;

    EasyTableQueryerImpl(Class<T> resultType, QuerySqlBuilder sqlBuilder, ModelDataExecutor modelDataExecutor,
            SqlBufferJdbcOperations jdbcOperations) {
        this.resultType = resultType;
        this.sqlBuilder = sqlBuilder;
        this.modelDataExecutor = modelDataExecutor;
        this.jdbc = jdbcOperations;
    }

    public T find(DbWhere where) {
        if (where == null || where.isEmpty()) {
            throw new IllegalArgumentException("where can't be empty");
        }
        modelDataExecutor.fillDataEffectiveFlag(where);
        SqlBuffer buffer = sqlBuilder.buildFindSql(where);
        Map<String, Object> map = jdbc.queryForMap(buffer);
        return map == null ? null : DbTools.resultToBean(map, resultType);
    }

    public List<T> listAll() {
        return listAll(null);
    }

    public List<T> listAll(List<Ordering> orderings) {
        DbWhere where = new DbWhere();
        modelDataExecutor.fillDataEffectiveFlag(where);
        SqlBuffer buffer = sqlBuilder.buildListSql(where, orderings);
        return jdbc.queryForList(buffer, resultType);
    }

    public PageList<T> list(DbWhere where, OrderPaging odpg) {
        // DbWhere readyWhere = checkWhere(where); // 带分页查询列表, 允许条件为空, 因此不检查
        DbWhere readyWhere = where;
        if (where == null || where instanceof EmptyDbWhere) {
            readyWhere = new DbWhere();
        }
        modelDataExecutor.fillDataEffectiveFlag(readyWhere);

        // WHERE条件
        SqlBuffer wsb = sqlBuilder.helper().buildWhereSql(readyWhere);
        return this.doList(wsb, odpg);
    }

    private PageList<T> doList(SqlBuffer wsb, OrderPaging odpg) {
        SqlBuffer qsb = sqlBuilder.buildListSql(wsb, odpg.getOrderings());
        SqlBuffer csb = null;
        if (odpg.isPaging() && odpg.isNeedCount()) {
            csb = sqlBuilder.buildCountSql(wsb);
        }

        PartList<T> list = PagingQuery.queryForList(jdbc, qsb, csb, odpg, resultType);
        return list == null ? null : new PageList<T>(list, list.getTotal());
    }

    public <V> V findFieldValue(String fieldName, DbWhere where, Class<V> valueClazz) throws ServiceException {
        DbWhere readyWhere = checkWhere(where);
        modelDataExecutor.fillDataEffectiveFlag(readyWhere);
        List<V> list = doListFieldValues(fieldName, false, readyWhere, null, valueClazz);
        return VerifyTools.isBlank(list) ? null : list.get(0);
    }

    public <V> List<V> listFieldValues(String fieldName, boolean distinct, DbWhere where, List<Ordering> orderings,
            Class<V> valueClazz) throws ServiceException {
        DbWhere readyWhere = checkWhere(where);
        modelDataExecutor.fillDataEffectiveFlag(readyWhere);
        return doListFieldValues(fieldName, distinct, readyWhere, null, valueClazz);
    }

    private <V> List<V> doListFieldValues(String fieldName, boolean distinct, DbWhere where, List<Ordering> orderings,
            Class<V> valueClazz) throws ServiceException {
        SqlBuffer buffer = sqlBuilder.buildListFieldValuesSql(fieldName, distinct, where, orderings);
        return jdbc.query(buffer, new FirstColumnMapper<>(valueClazz));
    }

    public int count(DbWhere where) throws ServiceException {
        DbWhere readyWhere = checkWhere(where);
        modelDataExecutor.fillDataEffectiveFlag(readyWhere);
        return doCount(readyWhere);
    }

    private int doCount(DbWhere readyWhere) throws ServiceException {
        SqlBuffer buffer = sqlBuilder.buildCountSql(readyWhere);
        return jdbc.queryForObject(buffer, Integer.class);
    }

    public Map<String, Integer> groupCount(String groupBy, DbWhere where) throws ServiceException {
        VerifyTools.requireNotBlank(groupBy, "groupBy");
        DbWhere readyWhere = checkWhere(where);
        modelDataExecutor.fillDataEffectiveFlag(readyWhere);
        return this.doGroupCount(groupBy, readyWhere);
    }

    private Map<String, Integer> doGroupCount(String groupBy, DbWhere readyWhere) throws ServiceException {
        SqlBuffer buffer = sqlBuilder.buildGroupCountSql(groupBy, readyWhere);
        List<KeyValue<Integer>> list = jdbc.query(buffer, KEY_INTEGER_MAPPER);
        return KeyValue.toMap(list);
    }

    private static KeyIntegerMapper KEY_INTEGER_MAPPER = new KeyIntegerMapper();

    protected DbWhere checkWhere(DbWhere where) {
        if (where == null || (where.isEmpty() && !(where instanceof EmptyDbWhere))) {
            throw new IllegalArgumentException("where can't be empty, please use DbWhere.NONE");
        } else if (where instanceof EmptyDbWhere) {
            return new DbWhere();
        } else {
            return where;
        }
    }

}
