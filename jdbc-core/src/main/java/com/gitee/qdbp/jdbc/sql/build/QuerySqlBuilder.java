package com.gitee.qdbp.jdbc.sql.build;

import com.gitee.qdbp.able.exception.ServiceException;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.able.jdbc.ordering.Orderings;
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

    protected QueryFragmentHelper sqlHelper;

    public QuerySqlBuilder(QueryFragmentHelper sqlHelper) {
        this.sqlHelper = sqlHelper;
    }

    public QueryFragmentHelper helper() {
        return sqlHelper;
    }

    public SqlBuffer buildFindSql(DbWhere where) {
        SqlBuffer buffer = new SqlBuffer();
        // SELECT ... FROM
        buffer.append("SELECT").append(' ', sqlHelper.buildSelectFieldsSql());
        buffer.append('\n', sqlHelper.buildFromSql());
        // WHERE ...
        buffer.append('\n', sqlHelper.buildWhereSql(where, true));
        return buffer;
    }

    public SqlBuffer buildListSql(DbWhere where, Orderings orderings) {
        SqlBuffer wsb = sqlHelper.buildWhereSql(where, true);
        return buildListSql(wsb, orderings);
    }

    public SqlBuffer buildListSql(SqlBuffer whereSql, Orderings orderings) {
        SqlBuffer buffer = new SqlBuffer();
        // SELECT ... FROM
        buffer.append("SELECT").append(' ', sqlHelper.buildSelectFieldsSql());
        buffer.append('\n', sqlHelper.buildFromSql());
        // WHERE ...
        buffer.append('\n', whereSql);
        if (VerifyTools.isNotBlank(orderings)) {
            buffer.append('\n', sqlHelper.buildOrderBySql(orderings, true));
        }
        return buffer;
    }

    public SqlBuffer buildCountSql(DbWhere where) {
        SqlBuffer wsb = sqlHelper.buildWhereSql(where, true);
        return buildCountSql(wsb);
    }

    public SqlBuffer buildCountSql(SqlBuffer whereSql) {
        SqlBuffer buffer = new SqlBuffer();
        // SELECT COUNT(*) FROM
        buffer.append("SELECT").append(' ', "COUNT(*)").append(' ', sqlHelper.buildFromSql());
        // WHERE ...
        buffer.append('\n', whereSql);
        return buffer;
    }

    public SqlBuffer buildGroupCountSql(String groupBy, DbWhere where) {
        // 字段列表
        SqlBuffer fields = sqlHelper.buildByFieldsSql(groupBy);

        SqlBuffer buffer = new SqlBuffer();
        // SELECT ... FROM
        buffer.append("SELECT");
        buffer.append(' ', fields).append(',').append("COUNT(*)");
        buffer.append('\n', sqlHelper.buildFromSql());
        // WHERE ...
        buffer.append('\n', sqlHelper.buildWhereSql(where, true));
        // GROUP BY ...
        buffer.append('\n', "GROUP BY").append(' ', fields);
        return buffer;
    }

    public SqlBuffer buildListFieldValuesSql(String fieldName, boolean distinct, DbWhere where,
            Orderings orderings) throws ServiceException {
        SqlBuffer wsb = sqlHelper.buildWhereSql(where, true);
        return buildListFieldValuesSql(fieldName, distinct, wsb, orderings);
    }

    public SqlBuffer buildListFieldValuesSql(String fieldName, boolean distinct, SqlBuffer where,
            Orderings orderings) throws ServiceException {
        SqlBuffer buffer = new SqlBuffer();

        // SELECT ... FROM
        buffer.append("SELECT", ' ');
        if (distinct) {
            buffer.append("DISTINCT", ' ');
        }
        buffer.append(sqlHelper.buildSelectFieldsSql(fieldName));
        buffer.append('\n', sqlHelper.buildFromSql());
        // WHERE ...
        buffer.append('\n', where);
        // ORDER BY ...
        if (VerifyTools.isNotBlank(orderings)) {
            buffer.append('\n', sqlHelper.buildOrderBySql(orderings, true));
        }
        return buffer;
    }
}
