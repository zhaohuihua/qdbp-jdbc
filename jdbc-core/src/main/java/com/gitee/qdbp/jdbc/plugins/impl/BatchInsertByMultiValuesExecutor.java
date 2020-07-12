package com.gitee.qdbp.jdbc.plugins.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.gitee.qdbp.able.jdbc.model.PkEntity;
import com.gitee.qdbp.jdbc.api.SqlBufferJdbcOperations;
import com.gitee.qdbp.jdbc.model.DbType;
import com.gitee.qdbp.jdbc.model.DbVersion;
import com.gitee.qdbp.jdbc.model.MainDbType;
import com.gitee.qdbp.jdbc.plugins.BatchInsertExecutor;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.build.CrudSqlBuilder;
import com.gitee.qdbp.jdbc.sql.fragment.CrudFragmentHelper;

/**
 * 一个INSERT对应多个VALUES的批量新增接口实现类<br>
 * INSERT INTO {tableName}({columnNames}) <br>
 * VALUES<br>
 * ({fieldValues})<br>
 * ...<br>
 * ({fieldValues})<br>
 * 
 * @author zhaohuihua
 * @version 20200707
 */
public class BatchInsertByMultiValuesExecutor implements BatchInsertExecutor {

    /**
     * 是否支持指定数据库<br>
     * 目前已知只有mysql支持, oracle/db2都不支持<br>
     * 如果有其他数据库支持, 可以继承此类, 覆盖supports方法
     */
    @Override
    public boolean supports(DbVersion version) {
        DbType dbType = version.getDbType();
        return dbType == MainDbType.MySQL || dbType == MainDbType.MariaDB;
    }

    @Override
    public List<String> inserts(List<PkEntity> entities, SqlBufferJdbcOperations jdbc, CrudSqlBuilder sqlBuilder) {
        CrudFragmentHelper sqlHelper = sqlBuilder.helper();
        String tableName = sqlHelper.getTableName();
        Set<String> fieldNames = mergeFields(entities);
        SqlBuffer fieldsSqlBuffer = sqlHelper.buildInsertFieldsSql(fieldNames);

        SqlBuffer buffer = new SqlBuffer();
        // INSERT INTO (...)
        buffer.append("INSERT INTO").append(' ', tableName).append(' ');
        buffer.append('(').append(fieldsSqlBuffer).append(')');
        // VALUES (...) (...)
        buffer.append('\n', "VALUES");
        List<String> ids = new ArrayList<>();
        boolean first = true;
        for (PkEntity item : entities) {
            String id = item.getPrimaryKey();
            ids.add(id);
            Map<String, Object> entity = item.getEntity();
            SqlBuffer valuesSqlBuffer = sqlHelper.buildInsertValuesSql(fieldNames, entity);
            if (first) {
                first = false;
            } else {
                buffer.append(',');
            }
            buffer.append('\n').append('(').append(valuesSqlBuffer).append(')');
        }

        // 执行批量数据库插入
        jdbc.batchInsert(buffer);
        return ids;
    }

    private Set<String> mergeFields(List<PkEntity> entities) {
        Set<String> fieldNames = new HashSet<>();
        for (PkEntity item : entities) {
            fieldNames.addAll(item.getEntity().keySet());
        }
        return fieldNames;
    }
}
