
package com.gitee.qdbp.jdbc.biz;

import java.util.List;
import com.gitee.qdbp.jdbc.api.EasyJoinQuery;
import com.gitee.qdbp.jdbc.api.SqlBufferJdbcOperations;
import com.gitee.qdbp.jdbc.condition.TableJoin;
import com.gitee.qdbp.jdbc.condition.TableJoin.JoinItem;
import com.gitee.qdbp.jdbc.plugins.ModelDataExecutor;
import com.gitee.qdbp.jdbc.sql.build.QuerySqlBuilder;

/**
 * 基础表连接查询操作
 *
 * @author 赵卉华
 * @version 190606
 */
public class EasyJoinQueryImpl<T> extends EasyTableQueryImpl<T> implements EasyJoinQuery<T>  {

    EasyJoinQueryImpl(TableJoin tables, Class<T> resultType, QuerySqlBuilder sqlBuilder,
            ModelDataExecutor modelDataExecutor, SqlBufferJdbcOperations jdbcOperations) {
        super(resultType, sqlBuilder, modelDataExecutor, jdbcOperations);
        List<JoinItem> joins = tables.getJoins();
        for (JoinItem item : joins) {
            modelDataExecutor.fillDataEffectiveFlag(item.getWhere());
        }
    }

}
