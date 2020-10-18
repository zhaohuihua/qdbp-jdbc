
package com.gitee.qdbp.jdbc.biz;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.RowMapper;
import com.gitee.qdbp.able.beans.KeyValue;
import com.gitee.qdbp.able.exception.ServiceException;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.able.jdbc.condition.DbWhere.EmptiableWhere;
import com.gitee.qdbp.able.jdbc.fields.Fields;
import com.gitee.qdbp.able.jdbc.ordering.OrderPaging;
import com.gitee.qdbp.able.jdbc.ordering.Orderings;
import com.gitee.qdbp.able.jdbc.paging.PageList;
import com.gitee.qdbp.able.jdbc.paging.PartList;
import com.gitee.qdbp.jdbc.api.SqlBufferJdbcOperations;
import com.gitee.qdbp.jdbc.exception.DbErrorCode;
import com.gitee.qdbp.jdbc.model.FieldScene;
import com.gitee.qdbp.jdbc.plugins.EntityFieldFillExecutor;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.result.KeyIntegerMapper;
import com.gitee.qdbp.jdbc.result.RowToBeanMapper;
import com.gitee.qdbp.jdbc.result.SingleColumnMapper;
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
    protected EntityFieldFillExecutor entityFieldFillExecutor;
    protected SqlBufferJdbcOperations jdbc;
    protected SqlDialect dialect;

    /**
     * 构造函数
     * 
     * @param sqlBuilder SQL生成工具
     * @param entityFieldFillExecutor 实体业务处理接口
     * @param jdbcOperations SqlBuffer数据库操作类
     * @param rowToBeanMapper 结果转换接口
     */
    public BaseQueryerImpl(QuerySqlBuilder sqlBuilder, EntityFieldFillExecutor entityFieldFillExecutor,
            SqlBufferJdbcOperations jdbcOperations, RowToBeanMapper<T> rowToBeanMapper) {
        this.rowToBeanMapper = rowToBeanMapper;
        this.sqlBuilder = sqlBuilder;
        this.entityFieldFillExecutor = entityFieldFillExecutor;
        this.jdbc = jdbcOperations;
        this.dialect = jdbcOperations.getSqlDialect();
    }

    /**
     * 获取SQL方言处理类
     * 
     * @return SQL方言处理类
     */
    public SqlDialect getSqlDialect() {
        return this.dialect;
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
        return find(Fields.ALL, where);
    }

    public T find(Fields fields, DbWhere where) {
        VerifyTools.requireNotBlank(where, "where");
        entityFieldFillExecutor.fillQueryWhereDataState(where, getMajorTableAlias());
        entityFieldFillExecutor.fillQueryWhereParams(where, getMajorTableAlias());
        SqlBuffer buffer = sqlBuilder.buildFindSql(fields, where);
        return jdbc.queryForObject(buffer, rowToBeanMapper);
    }

    public List<T> listAll() {
        return listAll(Fields.ALL, Orderings.NONE);
    }

    public List<T> listAll(Fields fields) {
        return listAll(Fields.ALL, Orderings.NONE);
    }

    public List<T> listAll(Orderings orderings) {
        return this.listAll(Fields.ALL, orderings);
    }

    public List<T> listAll(Fields fields, Orderings orderings) {
        DbWhere where = new DbWhere();
        entityFieldFillExecutor.fillQueryWhereDataState(where, getMajorTableAlias());
        entityFieldFillExecutor.fillQueryWhereParams(where, getMajorTableAlias());
        return this.doList(fields, where, orderings);
    }

    public PageList<T> list(DbWhere where, OrderPaging odpg) {
        return list(Fields.ALL, where, odpg);
    }

    public PageList<T> list(Fields fields, DbWhere where, OrderPaging odpg) {
        // DbWhere readyWhere = checkWhere(where); // 带分页查询列表, 允许条件为空, 因此不检查
        DbWhere readyWhere = where;
        if (where == null || where == DbWhere.NONE) {
            readyWhere = new DbWhere();
        }
        entityFieldFillExecutor.fillQueryWhereDataState(readyWhere, getMajorTableAlias());
        entityFieldFillExecutor.fillQueryWhereParams(readyWhere, getMajorTableAlias());
        return this.doList(fields, readyWhere, odpg);
    }

    public List<T> list(DbWhere where, Orderings orderings) throws ServiceException {
        return list(Fields.ALL, where, orderings);
    }

    public List<T> list(Fields fields, DbWhere where, Orderings orderings) throws ServiceException {
        DbWhere readyWhere = checkWhere(where);
        entityFieldFillExecutor.fillQueryWhereDataState(readyWhere, getMajorTableAlias());
        entityFieldFillExecutor.fillQueryWhereParams(readyWhere, getMajorTableAlias());
        return this.doList(fields, readyWhere, orderings);
    }

    protected List<T> doList(Fields fields, DbWhere where, Orderings orderings) {
        SqlBuffer buffer = sqlBuilder.buildListSql(fields, where, orderings);
        return jdbc.query(buffer, rowToBeanMapper);
    }

    protected PageList<T> doList(Fields fields, DbWhere where, OrderPaging odpg) {
        SqlBuffer wsb = sqlBuilder.helper().buildWhereSql(where, true);
        SqlBuffer qsb = sqlBuilder.buildListSql(fields, wsb, odpg.getOrderings());
        SqlBuffer csb = null;
        if (odpg.isPaging() && odpg.isNeedCount()) {
            csb = sqlBuilder.buildCountSql(wsb);
        }

        PartList<T> list = PagingQuery.queryForList(jdbc, qsb, csb, odpg, rowToBeanMapper);
        return list == null ? null : new PageList<T>(list, list.getTotal());
    }

    public <V> V findFieldValue(String fieldName, DbWhere where, Class<V> valueClazz) throws ServiceException {
        DbWhere readyWhere = checkWhere(where);
        entityFieldFillExecutor.fillQueryWhereDataState(readyWhere, getMajorTableAlias());
        entityFieldFillExecutor.fillQueryWhereParams(readyWhere, getMajorTableAlias());
        PageList<V> list = doListFieldValues(fieldName, false, readyWhere, null, valueClazz);
        return VerifyTools.isBlank(list) ? null : list.get(0);
    }

    public <V> List<V> listFieldValues(String fieldName, boolean distinct, DbWhere where, Orderings orderings,
            Class<V> valueClazz) throws ServiceException {
        DbWhere readyWhere = checkWhere(where);
        entityFieldFillExecutor.fillQueryWhereDataState(readyWhere, getMajorTableAlias());
        entityFieldFillExecutor.fillQueryWhereParams(readyWhere, getMajorTableAlias());
        PageList<V> result = doListFieldValues(fieldName, distinct, readyWhere, OrderPaging.of(orderings), valueClazz);
        return result == null ? null : result.toList();
    }

    public <V> PageList<V> listFieldValues(String fieldName, boolean distinct, DbWhere where, OrderPaging odpg,
            Class<V> valueClazz) throws ServiceException {
        DbWhere readyWhere = checkWhere(where);
        entityFieldFillExecutor.fillQueryWhereDataState(readyWhere, getMajorTableAlias());
        entityFieldFillExecutor.fillQueryWhereParams(readyWhere, getMajorTableAlias());
        return doListFieldValues(fieldName, distinct, readyWhere, odpg, valueClazz);
    }

    protected <V> PageList<V> doListFieldValues(String fieldName, boolean distinct, DbWhere where, OrderPaging odpg,
            Class<V> valueClazz) throws ServiceException {
        SqlBuffer wsb = sqlBuilder.helper().buildWhereSql(where, true);
        SqlBuffer qsb = sqlBuilder.buildListFieldValuesSql(fieldName, distinct, wsb, odpg.getOrderings());
        SqlBuffer csb = null;
        if (odpg.isPaging() && odpg.isNeedCount()) {
            csb = sqlBuilder.buildCountSql(wsb);
        }
        String columnName = sqlBuilder.helper().getColumnName(FieldScene.CONDITION, fieldName);
        RowMapper<V> rowMapper = new SingleColumnMapper<>(columnName, valueClazz);
        PartList<V> list = PagingQuery.queryForList(jdbc, qsb, csb, odpg, rowMapper);
        return list == null ? null : new PageList<V>(list, list.getTotal());
    }

    public int count(DbWhere where) throws ServiceException {
        DbWhere readyWhere = checkWhere(where);
        entityFieldFillExecutor.fillQueryWhereDataState(readyWhere, getMajorTableAlias());
        entityFieldFillExecutor.fillQueryWhereParams(readyWhere, getMajorTableAlias());
        return doCount(readyWhere);
    }

    protected int doCount(DbWhere readyWhere) throws ServiceException {
        SqlBuffer buffer = sqlBuilder.buildCountSql(readyWhere);
        return jdbc.queryForObject(buffer, Integer.class);
    }

    public Map<String, Integer> groupCount(String groupBy, DbWhere where) throws ServiceException {
        VerifyTools.requireNotBlank(groupBy, "groupBy");
        DbWhere readyWhere = checkWhere(where);
        entityFieldFillExecutor.fillQueryWhereDataState(readyWhere, getMajorTableAlias());
        entityFieldFillExecutor.fillQueryWhereParams(readyWhere, getMajorTableAlias());
        return this.doGroupCount(groupBy, readyWhere);
    }

    protected Map<String, Integer> doGroupCount(String groupBy, DbWhere readyWhere) throws ServiceException {
        SqlBuffer buffer = sqlBuilder.buildGroupCountSql(groupBy, readyWhere);
        List<KeyValue<Integer>> list = jdbc.query(buffer, KEY_INTEGER_MAPPER);
        return KeyValue.toMap(list);
    }

    protected static KeyIntegerMapper KEY_INTEGER_MAPPER = new KeyIntegerMapper();

    protected DbWhere checkWhere(DbWhere where) {
        if (where == null || (where.isEmpty() && !(where instanceof EmptiableWhere))) {
            String details = "If you want to find all records, please use DbWhere.NONE";
            throw new ServiceException(DbErrorCode.DB_WHERE_MUST_NOT_BE_EMPTY, details);
        } else if (where == DbWhere.NONE) {
            return new DbWhere();
        } else {
            return where;
        }
    }

}
