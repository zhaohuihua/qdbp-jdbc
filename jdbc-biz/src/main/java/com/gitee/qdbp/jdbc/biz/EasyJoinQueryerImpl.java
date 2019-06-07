
package com.gitee.qdbp.jdbc.biz;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.gitee.qdbp.able.exception.ServiceException;
import com.gitee.qdbp.able.model.ordering.OrderPaging;
import com.gitee.qdbp.able.model.ordering.Ordering;
import com.gitee.qdbp.able.model.paging.PartList;
import com.gitee.qdbp.jdbc.api.SqlBufferJdbcOperations;
import com.gitee.qdbp.jdbc.api.EasyJoinQueryer;
import com.gitee.qdbp.jdbc.biz.EasyCrudDaoImpl.FirstColumnMapper;
import com.gitee.qdbp.jdbc.condition.DbWhere;
import com.gitee.qdbp.jdbc.condition.DbWhere.EmptyDbWhere;
import com.gitee.qdbp.jdbc.plugins.ModelDataExecutor;
import com.gitee.qdbp.jdbc.condition.TableJoin;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.fragment.TableQueryFragmentHelper;
import com.gitee.qdbp.jdbc.utils.DbTools;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 基础表连接查询操作
 *
 * @author 赵卉华
 * @version 190606
 */
public class EasyJoinQueryerImpl implements EasyJoinQueryer {

    private static Logger log = LoggerFactory.getLogger(EasyJoinQueryerImpl.class);

    private TableJoin tables;
    private TableQueryFragmentHelper sqlBuilder;
    private ModelDataExecutor modelDataExecutor;
    private SqlBufferJdbcOperations baseJdbcOperations;

    EasyJoinQueryerImpl(TableJoin tables, TableQueryFragmentHelper sqlBuilder,
            ModelDataExecutor modelDataExecutor, SqlBufferJdbcOperations baseJdbcOperations) {
        this.tables = tables;
        this.sqlBuilder = sqlBuilder;
        this.modelDataExecutor = modelDataExecutor;
        this.baseJdbcOperations = baseJdbcOperations;
        for (Item item : tables) {
            modelDataExecutor.fillDataEffectiveFlag(item);
        }
    }

    @Override
    public <T> T find(DbWhere where, Class<T> resultType) {
        if (where == null || where.isEmpty()) {
            throw new IllegalArgumentException("where can't be empty");
        }
        modelDataExecutor.fillDataEffectiveFlag(where);
        SqlBuffer buffer = new SqlBuffer();
        // SELECT ... FROM
        buffer.append("SELECT").append(' ', sqlBuilder.buildFieldsSql());
        buffer.append(' ', "FROM").append(' ', sqlBuilder.getTableName());
        // WHERE ...
        buffer.append(' ', sqlBuilder.buildWhereSql(where));

        Map<String, Object> map = baseJdbcOperations.queryForMap(buffer);
        return map == null ? null : DbTools.resultToBean(map, resultType);
    }

    @Override
    public <T> List<T> listAll() {
        return listAll(null);
    }

    @Override
    public <T> List<T> listAll(List<Ordering> orderings) {
        DbWhere where = new DbWhere();
        entityTools.fillDataEffectiveFlag(where);
        SqlBuffer buffer = new SqlBuffer();
        // SELECT ... FROM
        buffer.append("SELECT");
        buffer.append(' ', sqlBuilder.buildFieldsSql());
        buffer.append(' ', "FROM").append(' ', sqlBuilder.getTableName());
        // WHERE ...
        buffer.append(' ', sqlBuilder.buildWhereSql(where));
        if (VerifyTools.isNotBlank(orderings)) {
            buffer.append(' ', sqlBuilder.buildOrderBySql(orderings));
        }

        return baseJdbcOperations.queryForList(buffer, resultType);
    }

    @Override
    public <T> PartList<T> list(DbWhere where, OrderPaging odpg) {
        // DbWhere readyWhere = checkWhere(where); // 带分页查询列表, 允许条件为空, 因此不检查
        DbWhere readyWhere = where;
        if (where == null || where instanceof EmptyDbWhere) {
            readyWhere = new DbWhere();
        }
        entityTools.fillDataEffectiveFlag(readyWhere);

        // WHERE条件
        SqlBuffer wsb = sqlBuilder.buildWhereSql(readyWhere);
        return this.doList(wsb, odpg);
    }

    private PartList<T> doList(SqlBuffer wsb, OrderPaging odpg) {
        SqlBuffer qsb = new SqlBuffer();
        // SELECT ... FROM
        qsb.append("SELECT").append(' ', sqlBuilder.buildFieldsSql());
        qsb.append(' ', "FROM").append(' ', sqlBuilder.getTableName());
        // WHERE ...
        qsb.append(' ', wsb);

        // ORDER BY ...
        List<Ordering> orderings = odpg.getOrderings();
        if (VerifyTools.isNotBlank(orderings)) {
            qsb.append(' ', sqlBuilder.buildOrderBySql(orderings));
        }

        SqlBuffer csb = new SqlBuffer();
        if (odpg.isPaging() && odpg.isNeedCount()) {
            // SELECT COUNT(*) FROM
            csb.append("SELECT").append(' ', "COUNT(*)").append(' ', "FROM").append(' ', sqlBuilder.getTableName());
            // WHERE ...
            csb.append(' ', wsb);
        }

        return baseJdbcOperations.queryForList(qsb, csb, odpg, sqlBuilder.getDialect(), resultType);
    }

    public <V> V findFieldValue(String fieldName, DbWhere where, Class<V> valueClazz) throws ServiceException {
        DbWhere readyWhere = checkWhere(where);
        entityTools.fillDataEffectiveFlag(readyWhere);
        List<V> list = doListFieldValues(fieldName, false, readyWhere, null, valueClazz);
        return VerifyTools.isBlank(list) ? null : list.get(0);
    }

    @Override
    public <V> List<V> listFieldValues(String fieldName, boolean distinct, DbWhere where, List<Ordering> orderings,
            Class<V> valueClazz) throws ServiceException {
        DbWhere readyWhere = checkWhere(where);
        entityTools.fillDataEffectiveFlag(readyWhere);
        return doListFieldValues(fieldName, distinct, readyWhere, null, valueClazz);
    }

    private <V> List<V> doListFieldValues(String fieldName, boolean distinct, DbWhere where, List<Ordering> orderings,
            Class<V> valueClazz) throws ServiceException {
        SqlBuffer buffer = new SqlBuffer();
        // SELECT ... FROM
        buffer.append("SELECT", ' ');
        if (distinct) {
            buffer.append("DISTINCT", ' ');
        }
        buffer.append(sqlBuilder.buildFieldsSql(fieldName));
        buffer.append(' ', "FROM").append(' ', sqlBuilder.getTableName());
        // WHERE ...
        buffer.append(' ', sqlBuilder.buildWhereSql(where));
        // ORDER BY ...
        if (VerifyTools.isNotBlank(orderings)) {
            buffer.append(' ', sqlBuilder.buildOrderBySql(orderings));
        }
        return baseJdbcOperations.query(buffer, new FirstColumnMapper<>(valueClazz));
    }

    @Override
    public int count(DbWhere where) throws ServiceException {
        return 0;
    }

    @Override
    public Map<String, Integer> groupCount(String groupBy, DbWhere where) throws ServiceException {
        return null;
    }

}
