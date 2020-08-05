package com.gitee.qdbp.jdbc.sql.build;

import com.gitee.qdbp.able.exception.ServiceException;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.able.jdbc.fields.Fields;
import com.gitee.qdbp.able.jdbc.fields.IncludeFields;
import com.gitee.qdbp.able.jdbc.ordering.Orderings;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.SqlBuilder;
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
        return buildFindSql(Fields.ALL, where);
    }

    public SqlBuffer buildFindSql(Fields fields, DbWhere where) {
        SqlBuilder buffer = new SqlBuilder();
        // SELECT ... FROM
        buffer.ad("SELECT").ad(sqlHelper.buildSelectFieldsSql(fields));
        buffer.newline().ad(sqlHelper.buildFromSql());
        // WHERE ...
        if (VerifyTools.isNotBlank(where)) {
            buffer.newline().ad(sqlHelper.buildWhereSql(where, true));
        }
        return buffer.out();
    }

    public SqlBuffer buildListSql(DbWhere where, Orderings orderings) {
        SqlBuffer wsb = sqlHelper.buildWhereSql(where, true);
        return buildListSql(Fields.ALL, wsb, orderings);
    }

    public SqlBuffer buildListSql(Fields fields, DbWhere where, Orderings orderings) {
        SqlBuffer wsb = sqlHelper.buildWhereSql(where, true);
        return buildListSql(fields, wsb, orderings);
    }

    public SqlBuffer buildListSql(SqlBuffer whereSql, Orderings orderings) {
        return buildListSql(Fields.ALL, whereSql, orderings);
    }

    public SqlBuffer buildListSql(Fields fields, SqlBuffer whereSql, Orderings orderings) {
        SqlBuilder buffer = new SqlBuilder();
        // SELECT ... FROM
        buffer.ad("SELECT").ad(sqlHelper.buildSelectFieldsSql(fields));
        buffer.newline().ad(sqlHelper.buildFromSql());
        // WHERE ...
        if (VerifyTools.isNotBlank(whereSql)) {
            buffer.newline().ad(whereSql);
        }
        if (VerifyTools.isNotBlank(orderings)) {
            buffer.newline().ad(sqlHelper.buildOrderBySql(orderings, true));
        }
        return buffer.out();
    }

    public SqlBuffer buildCountSql(DbWhere where) {
        SqlBuffer wsb = sqlHelper.buildWhereSql(where, true);
        return buildCountSql(wsb);
    }

    public SqlBuffer buildCountSql(SqlBuffer whereSql) {
        SqlBuilder buffer = new SqlBuilder();
        // SELECT COUNT(*) FROM
        buffer.ad("SELECT").ad("COUNT(*)").ad(sqlHelper.buildFromSql());
        // WHERE ...
        if (VerifyTools.isNotBlank(whereSql)) {
            buffer.newline().ad(whereSql);
        }
        return buffer.out();
    }

    public SqlBuffer buildGroupCountSql(String groupBy, DbWhere where) {
        return buildGroupCountSql(new IncludeFields(groupBy), where);
    }

    public SqlBuffer buildGroupCountSql(Fields fields, DbWhere where) {
        // 字段列表
        SqlBuffer fieldSql = sqlHelper.buildSelectFieldsSql(fields);
        SqlBuffer groupBySql = sqlHelper.buildByFieldsSql(fields);

        SqlBuilder buffer = new SqlBuilder();
        // SELECT ... FROM
        buffer.ad("SELECT");
        buffer.ad(fieldSql).ad(',').ad("COUNT(*)");
        buffer.newline().ad(sqlHelper.buildFromSql());
        // WHERE ...
        if (VerifyTools.isNotBlank(where)) {
            buffer.newline().ad(sqlHelper.buildWhereSql(where, true));
        }
        // GROUP BY ...
        buffer.newline().ad("GROUP BY").ad(groupBySql);
        return buffer.out();
    }

    public SqlBuffer buildListFieldValuesSql(String fieldName, boolean distinct, DbWhere where, Orderings orderings)
            throws ServiceException {
        SqlBuffer wsb = sqlHelper.buildWhereSql(where, true);
        return buildListFieldValuesSql(fieldName, distinct, wsb, orderings);
    }

    public SqlBuffer buildListFieldValuesSql(String fieldName, boolean distinct, SqlBuffer where, Orderings orderings)
            throws ServiceException {
        SqlBuilder buffer = new SqlBuilder();
        // SELECT ... FROM
        buffer.ad("SELECT");
        if (distinct) {
            buffer.ad("DISTINCT");
        }
        buffer.ad(sqlHelper.buildSelectFieldsSql(fieldName));
        buffer.newline().ad(sqlHelper.buildFromSql());
        // WHERE ...
        if (VerifyTools.isNotBlank(where)) {
            buffer.newline().ad(where);
        }
        // ORDER BY ...
        if (VerifyTools.isNotBlank(orderings)) {
            buffer.newline().ad(sqlHelper.buildOrderBySql(orderings, true));
        }
        return buffer.out();
    }
}
