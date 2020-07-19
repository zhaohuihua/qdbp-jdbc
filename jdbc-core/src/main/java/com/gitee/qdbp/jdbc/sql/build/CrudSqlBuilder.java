package com.gitee.qdbp.jdbc.sql.build;

import java.util.Map;
import java.util.Set;
import com.gitee.qdbp.able.jdbc.condition.DbUpdate;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.SqlBuilder;
import com.gitee.qdbp.jdbc.sql.fragment.CrudFragmentHelper;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 单表增删改查完整SQL生成工具<br>
 * 将CrudFragmentHelper生成的SQL片段拼接成完整SQL
 *
 * @author zhaohuihua
 * @version 190607
 */
public class CrudSqlBuilder extends QuerySqlBuilder {

    public CrudSqlBuilder(CrudFragmentHelper sqlHelper) {
        super(sqlHelper);
    }

    public CrudFragmentHelper helper() {
        return (CrudFragmentHelper) sqlHelper;
    }

    public SqlBuffer buildInsertSql(Map<String, Object> entity) {
        CrudFragmentHelper sqlHelper = helper();
        String tableName = sqlHelper.getTableName();
        Set<String> fieldNames = entity.keySet();
        SqlBuffer fieldsSqlBuffer = sqlHelper.buildInsertFieldsSql(fieldNames);
        SqlBuffer valuesSqlBuffer = sqlHelper.buildInsertValuesSql(entity);

        SqlBuilder buffer = new SqlBuilder();
        // INSERT INTO (...)
        buffer.ad("INSERT INTO").ad(tableName);
        buffer.ad('(').ad(fieldsSqlBuffer).ad(')');
        // VALUES (...)
        buffer.newline().ad("VALUES").ad('(').ad(valuesSqlBuffer).ad(')');
        return buffer.out();
    }

    public SqlBuffer buildUpdateSql(DbUpdate entity, DbWhere where) {
        CrudFragmentHelper sqlHelper = helper();
        String tableName = sqlHelper.getTableName();

        SqlBuilder buffer = new SqlBuilder();
        buffer.ad("UPDATE").ad(tableName);
        buffer.newline().ad(sqlHelper.buildUpdateSetSql(entity, true));

        if (VerifyTools.isNotBlank(where)) {
            buffer.newline().ad(sqlHelper.buildWhereSql(where, true));
        }
        return buffer.out();
    }

    public SqlBuffer buildDeleteSql(DbWhere where) {
        CrudFragmentHelper sqlHelper = helper();
        String tableName = sqlHelper.getTableName();

        SqlBuilder buffer = new SqlBuilder();
        buffer.ad("DELETE").ad("FROM").ad(tableName);
        if (VerifyTools.isNotBlank(where)) {
            buffer.newline().ad(sqlHelper.buildWhereSql(where, true));
        }
        return buffer.out();
    }
}
