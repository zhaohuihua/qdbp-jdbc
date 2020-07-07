package com.gitee.qdbp.jdbc.plugins.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.gitee.qdbp.able.jdbc.condition.DbUpdate;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.able.jdbc.model.PkEntity;
import com.gitee.qdbp.able.jdbc.model.PkUpdate;
import com.gitee.qdbp.jdbc.api.SqlBufferJdbcOperations;
import com.gitee.qdbp.jdbc.model.PrimaryKeyFieldColumn;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.build.CrudSqlBuilder;

/**
 * 生成多条SQL语句一起执行的批量处理接口实现类<br>
 * <b>注意</b>: 只有mysql支持, oracle/db2都不支持<br>
 * <b>注意</b>: 如果是druid需要配置multiStatementAllow; 如果是mysql需要配置allowMultiQueries参数!
 *
 * @author zhaohuihua
 * @version 20200707
 */
public class BatchOperateByMultiSqlExecutor extends BaseBatchOperateExecutor {

    /**
     * 批量保存实体对象<br>
     * 生成多条SQL语句一起执行:<br>
     * INSERT INTO {tableName}({columnNames}) VALUES ({fieldValues});<br>
     * INSERT INTO {tableName}({columnNames}) VALUES ({fieldValues});<br>
     * ...<br>
     * INSERT INTO {tableName}({columnNames}) VALUES ({fieldValues});<br>
     */
    @Override
    public List<String> inserts(List<PkEntity> entities, SqlBufferJdbcOperations jdbc, CrudSqlBuilder sqlBuilder) {
        SqlBuffer buffer = new SqlBuffer();
        List<String> ids = new ArrayList<>();
        for (PkEntity item : entities) {
            String id = item.getPrimaryKey();
            Map<String, Object> entity = item.getEntity();
            ids.add(id);
            // 拼接SQL
            SqlBuffer temp = sqlBuilder.buildInsertSql(entity);
            buffer.append(temp).append(';', '\n');
        }

        // 执行批量数据库插入
        jdbc.batchInsert(buffer);
        return ids;
    }

    /**
     * 根据主键编号批量更新实体对象<br>
     * 生成多条SQL语句一起执行:<br>
     * UPDATE {tableName} SET {columnName}={fieldValue}, ... WHERE {whereConditions} AND DATA_STATE=0;<br>
     * UPDATE {tableName} SET {columnName}={fieldValue}, ... WHERE {whereConditions} AND DATA_STATE=0;<br>
     * ...<br>
     * UPDATE {tableName} SET {columnName}={fieldValue}, ... WHERE {whereConditions} AND DATA_STATE=0;<br>
     */
    @Override
    public int updates(List<PkUpdate> contents, DbWhere commonWhere, SqlBufferJdbcOperations jdbc,
            CrudSqlBuilder sqlBuilder) {
        // 查找主键(批量更新必须要有主键)
        PrimaryKeyFieldColumn pk = sqlBuilder.helper().getPrimaryKey();
        SqlBuffer buffer = new SqlBuffer();
        for (PkUpdate item : contents) {
            String pkValue = item.getPrimaryKey();
            DbUpdate entity = item.getUpdate();
            // 从公共条件中复制过滤条件
            DbWhere where = commonWhere.copy();
            // 将主键加入到过滤条件中
            where.on(pk.getFieldName(), "=", pkValue);
            // 拼接SQL
            SqlBuffer temp = sqlBuilder.buildUpdateSql(entity, where);
            if (!buffer.isEmpty()) {
                buffer.append(';', '\n');
            }
            buffer.append(temp);
        }

        // 执行批量数据库插入
        return jdbc.batchUpdate(buffer);
    }

}
