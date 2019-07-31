package com.gitee.qdbp.jdbc.sql.build;

import java.util.Map;
import java.util.Set;
import com.gitee.qdbp.able.jdbc.condition.DbUpdate;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
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
        return (CrudFragmentHelper) sqlBuilder;
    }

    public SqlBuffer buildInsertSql(Map<String, Object> entity) {

        CrudFragmentHelper sqlHelper = helper();
        String tableName = sqlHelper.getTableName();
        Set<String> fieldNames = entity.keySet();
        SqlBuffer valuesSqlBuffer = sqlHelper.buildInsertValuesSql(entity);
        SqlBuffer fieldsSqlBuffer = sqlHelper.buildFieldsSql(fieldNames);

        SqlBuffer buffer = new SqlBuffer();
        // INSERT INTO (...)
        buffer.append("INSERT INTO").append(' ', tableName).append(' ');
        buffer.append('(');
        buffer.append(fieldsSqlBuffer);
        buffer.append(')');
        // VALUES (...)
        buffer.append('\n', "VALUES", ' ').append('(');
        buffer.append(valuesSqlBuffer);
        buffer.append(')');
        return buffer;
    }

    public SqlBuffer buildUpdateSql(DbUpdate entity, DbWhere where) {
        CrudFragmentHelper sqlHelper = helper();
        String tableName = sqlHelper.getTableName();

        SqlBuffer buffer = new SqlBuffer();
        buffer.append("UPDATE").append(' ', tableName);
        buffer.append('\n', sqlHelper.buildUpdateSetSql(entity, true));

        if (VerifyTools.isNotBlank(where)) {
            buffer.append('\n', sqlBuilder.buildWhereSql(where, true));
        }
        return buffer;
    }

    public SqlBuffer buildDeleteSql(DbWhere where) {
        CrudFragmentHelper sqlHelper = helper();
        String tableName = sqlHelper.getTableName();

        SqlBuffer buffer = new SqlBuffer();
        buffer.append("DELETE").append(' ', "FROM").append(' ', tableName);
        if (VerifyTools.isNotBlank(where)) {
            buffer.append('\n', sqlBuilder.buildWhereSql(where, true));
        }
        return buffer;
    }
}
