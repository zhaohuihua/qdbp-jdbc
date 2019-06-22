package com.gitee.qdbp.jdbc.sql.build;

import java.util.List;
import com.gitee.qdbp.able.exception.ServiceException;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.able.jdbc.ordering.Ordering;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.fragment.QueryFragmentHelper;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 完整的查询SQL生成工具<br>
 * 将QueryFragmentHelper生成的SQL片段拼接成完整SQL
 *
 * @author zhaohuihua
 * @version 190607
 */
public class QuerySqlBuilder {

    protected QueryFragmentHelper sqlBuilder; // TODO sqlHelper

    public QuerySqlBuilder(QueryFragmentHelper sqlHelper) {
        this.sqlBuilder = sqlHelper;
    }

    public QueryFragmentHelper helper() {
        return sqlBuilder;
    }

    public SqlBuffer buildFindSql(DbWhere where) {
        SqlBuffer buffer = new SqlBuffer();
        // SELECT ... FROM
        buffer.append("SELECT").append(' ', sqlBuilder.buildFieldsSql());
        buffer.append(' ', sqlBuilder.buildFromSql());
        // WHERE ...
        buffer.append(' ', sqlBuilder.buildWhereSql(where, true));
        return buffer;
    }

    public SqlBuffer buildListSql(DbWhere where, List<Ordering> orderings) {
        SqlBuffer wsb = sqlBuilder.buildWhereSql(where, true);
        return buildListSql(wsb, orderings);
    }

    public SqlBuffer buildListSql(SqlBuffer whereSql, List<Ordering> orderings) {
        SqlBuffer buffer = new SqlBuffer();
        // SELECT ... FROM
        buffer.append("SELECT");
        buffer.append(' ', sqlBuilder.buildFieldsSql());
        buffer.append(' ', sqlBuilder.buildFromSql());
        // WHERE ...
        buffer.append(' ', whereSql);
        if (VerifyTools.isNotBlank(orderings)) {
            buffer.append(' ', sqlBuilder.buildOrderBySql(orderings, true));
        }
        return buffer;
    }

    public SqlBuffer buildCountSql(DbWhere where) {
        SqlBuffer wsb = sqlBuilder.buildWhereSql(where, true);
        return buildCountSql(wsb);
    }

    public SqlBuffer buildCountSql(SqlBuffer whereSql) {
        SqlBuffer buffer = new SqlBuffer();
        // SELECT COUNT(*) FROM
        buffer.append("SELECT").append(' ', "COUNT(*)").append(' ', sqlBuilder.buildFromSql());
        // WHERE ...
        buffer.append(' ', whereSql);
        return buffer;
    }

    public SqlBuffer buildGroupCountSql(String groupBy, DbWhere where) {
        // 字段列表
        SqlBuffer fields = sqlBuilder.buildFieldsSql(groupBy);

        SqlBuffer buffer = new SqlBuffer();
        // SELECT ... FROM
        buffer.append("SELECT");
        buffer.append(' ', fields).append(',').append("COUNT(*)");
        buffer.append(' ', sqlBuilder.buildFromSql());
        // WHERE ...
        buffer.append(' ', sqlBuilder.buildWhereSql(where, true));
        // GROUP BY ...
        buffer.append(' ', "GROUP BY").append(' ', fields);
        return buffer;
    }

    public SqlBuffer buildListFieldValuesSql(String fieldName, boolean distinct, DbWhere where,
            List<Ordering> orderings) throws ServiceException {
        SqlBuffer buffer = new SqlBuffer();

        // SELECT ... FROM
        buffer.append("SELECT", ' ');
        if (distinct) {
            buffer.append("DISTINCT", ' ');
        }
        buffer.append(sqlBuilder.buildFieldsSql(fieldName));
        buffer.append(' ', sqlBuilder.buildFromSql());
        // WHERE ...
        buffer.append(' ', sqlBuilder.buildWhereSql(where, true));
        // ORDER BY ...
        if (VerifyTools.isNotBlank(orderings)) {
            buffer.append(' ', sqlBuilder.buildOrderBySql(orderings, true));
        }
        return buffer;
    }
}
