
package com.gitee.qdbp.jdbc.biz;

import java.util.List;
import com.gitee.qdbp.able.jdbc.condition.TableJoin;
import com.gitee.qdbp.able.jdbc.condition.TableJoin.JoinItem;
import com.gitee.qdbp.jdbc.api.JoinQueryer;
import com.gitee.qdbp.jdbc.api.SqlBufferJdbcOperations;
import com.gitee.qdbp.jdbc.model.AllFieldColumn;
import com.gitee.qdbp.jdbc.plugins.EntityFillExecutor;
import com.gitee.qdbp.jdbc.plugins.EntityFillHandler;
import com.gitee.qdbp.jdbc.plugins.MapToBeanConverter;
import com.gitee.qdbp.jdbc.result.RowToBeanMapper;
import com.gitee.qdbp.jdbc.result.TablesRowToProperyMapper;
import com.gitee.qdbp.jdbc.sql.build.QuerySqlBuilder;
import com.gitee.qdbp.jdbc.sql.fragment.TableJoinFragmentHelper;
import com.gitee.qdbp.jdbc.utils.DbTools;

/**
 * 基础表连接查询操作
 *
 * @author 赵卉华
 * @version 190606
 */
public class JoinQueryerImpl<T> extends BaseQueryerImpl<T> implements JoinQueryer<T> {

    private String majorTableAlias;

    public JoinQueryerImpl(TableJoin t, Class<T> r, SqlBufferJdbcOperations jdbc) {
        super(newQuerySqlBuilder(t, jdbc), newEntityFillExecutor(t), jdbc, newRowToBeanMapper(t, r));
        this.majorTableAlias = t.getMajor().getTableAlias();
        List<JoinItem> joins = t.getJoins();
        for (JoinItem item : joins) {
            entityFillExecutor.fillQueryWhereDataStatus(item.getWhere(), item.getTableAlias());
        }
    }

    private static QuerySqlBuilder newQuerySqlBuilder(TableJoin tables, SqlBufferJdbcOperations jdbc) {
        TableJoinFragmentHelper sqlHelper = new TableJoinFragmentHelper(tables, jdbc.findSqlDialect());
        return new QuerySqlBuilder(sqlHelper);
    }

    private static EntityFillExecutor newEntityFillExecutor(TableJoin tables) {
        AllFieldColumn<?> allFields = DbTools.parseToAllFieldColumn(tables);
        if (allFields.isEmpty()) {
            throw new IllegalArgumentException("fields is empty");
        }
        EntityFillHandler handler = DbTools.getEntityFillHandler();
        return new EntityFillExecutor(allFields, handler);
    }

    private static <T> RowToBeanMapper<T> newRowToBeanMapper(TableJoin tables, Class<T> clazz) {
        MapToBeanConverter converter = DbTools.getMapToBeanConverter();
        return new TablesRowToProperyMapper<>(tables, clazz, converter);
    }

    /** {@inheritDoc} **/
    @Override
    protected String getMajorTableAlias() {
        return majorTableAlias;
    }

    @Override
    public QuerySqlBuilder getSqlBuilder() {
        return (QuerySqlBuilder) sqlBuilder;
    }

}
