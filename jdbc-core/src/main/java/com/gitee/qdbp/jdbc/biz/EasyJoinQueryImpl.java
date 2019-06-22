
package com.gitee.qdbp.jdbc.biz;

import java.util.List;
import com.gitee.qdbp.able.jdbc.condition.TableJoin;
import com.gitee.qdbp.able.jdbc.condition.TableJoin.JoinItem;
import com.gitee.qdbp.jdbc.api.EasyJoinQuery;
import com.gitee.qdbp.jdbc.api.SqlBufferJdbcOperations;
import com.gitee.qdbp.jdbc.result.TablesRowToProperyMapper;
import com.gitee.qdbp.jdbc.utils.DbTools;

/**
 * 基础表连接查询操作
 *
 * @author 赵卉华
 * @version 190606
 */
public class EasyJoinQueryImpl<T> extends EasyTableQueryImpl<T> implements EasyJoinQuery<T> {

    private String majorTableAlias;

    public EasyJoinQueryImpl(TableJoin t, Class<T> r, SqlBufferJdbcOperations jdbc) {
        super(DbTools.getCrudSqlBuilder(t), DbTools.getModelDataExecutor(t), jdbc, new TablesRowToProperyMapper<>(t, r));
        this.majorTableAlias = t.getMajor().getTableAlias();
        List<JoinItem> joins = t.getJoins();
        for (JoinItem item : joins) {
            modelDataExecutor.fillQueryWhereDataStatus(item.getWhere(), item.getTableAlias());
        }
    }

    /** {@inheritDoc} **/
    @Override
    protected String getMajorTableAlais() {
        return majorTableAlias;
    }

}
