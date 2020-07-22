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
import com.gitee.qdbp.jdbc.sql.SqlBuilder;
import com.gitee.qdbp.jdbc.sql.build.CrudSqlBuilder;
import com.gitee.qdbp.jdbc.sql.fragment.CrudFragmentHelper;

/**
 * UNION ALL SELECT批量新增接口实现类(要求字段对齐)<pre>
    INSERT INTO {tableName}(FIELD1, FIELD2, FIELD3)
        SELECT field11, field12, ..., field1n FROM DUAL
        UNION ALL
        SELECT field21, field22, ..., field2n FROM DUAL
        UNION ALL
        SELECT fieldn1, fieldn2, ..., fieldnn FROM DUAL</pre>
 * 
 * @author zhaohuihua
 * @version 20200707
 */
public class BatchInsertByUnionAllFromDualExecutor implements BatchInsertExecutor {

    /**
     * 是否支持指定数据库<br>
     * 如果有其他数据库支持, 可以继承此类, 覆盖supports方法
     */
    @Override
    public boolean supports(DbVersion version) {
        DbType dbType = version.getDbType();
        return dbType == MainDbType.Oracle; // DUAL是oracle专用
    }

    @Override
    public List<String> inserts(List<PkEntity> entities, SqlBufferJdbcOperations jdbc, CrudSqlBuilder sqlBuilder) {
        CrudFragmentHelper sqlHelper = sqlBuilder.helper();
        String tableName = sqlHelper.getTableName();
        Set<String> fieldNames = mergeFields(entities);
        SqlBuffer fieldsSqlBuffer = sqlHelper.buildInsertFieldsSql(fieldNames);

        // INSERT INTO {tableName}(FIELD1, FIELD2, FIELD3) 
        //     SELECT field11, field12, ..., field1n FROM DUAL
        //     UNION ALL
        //     SELECT field21, field22, ..., field2n FROM DUAL
        //     UNION ALL
        //     SELECT fieldn1, fieldn2, ..., fieldnn FROM DUAL
        SqlBuilder sql = new SqlBuilder();
        // INSERT INTO (...)
        sql.ad("INSERT INTO").ad(tableName);
        sql.ad('(').ad(fieldsSqlBuffer).ad(')').newline().tab();
        List<String> ids = new ArrayList<>();
        int size = entities.size();
        for (int i = 0; i < size; i++) {
            PkEntity item = entities.get(i);
            String id = item.getPrimaryKey();
            ids.add(id);
            Map<String, Object> entity = item.getEntity();
            SqlBuffer valuesSqlBuffer = sqlHelper.buildInsertValuesSql(fieldNames, entity);
            if (i > 0) {
                sql.newline().ad("UNION ALL").newline();
            }
            sql.omit(i, size); // 插入省略标记
            sql.ad("SELECT").ad(valuesSqlBuffer).ad("FROM DUAL");
        }

        // 执行批量数据库插入
        jdbc.batchInsert(sql.out());
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
