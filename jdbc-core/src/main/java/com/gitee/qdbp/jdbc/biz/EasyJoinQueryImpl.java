
package com.gitee.qdbp.jdbc.biz;

import java.util.List;
import com.gitee.qdbp.able.jdbc.condition.TableJoin;
import com.gitee.qdbp.able.jdbc.condition.TableJoin.JoinItem;
import com.gitee.qdbp.jdbc.api.EasyJoinQuery;
import com.gitee.qdbp.jdbc.api.SqlBufferJdbcOperations;
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
public class EasyJoinQueryImpl<T> extends EasyBaseQueryImpl<T> implements EasyJoinQuery<T> {

    private String majorTableAlias;

    public EasyJoinQueryImpl(TableJoin t, Class<T> r, SqlBufferJdbcOperations jdbc) {
        super(newQuerySqlBuilder(t, jdbc), DbTools.getEntityFillExecutor(t), jdbc, new TablesRowToProperyMapper<>(t, r));
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

    /** {@inheritDoc} **/
    @Override
    protected String getMajorTableAlias() {
        return majorTableAlias;
    }

}
