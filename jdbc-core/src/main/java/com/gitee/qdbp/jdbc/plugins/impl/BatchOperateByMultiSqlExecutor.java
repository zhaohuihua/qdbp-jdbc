package com.gitee.qdbp.jdbc.plugins.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.gitee.qdbp.able.jdbc.condition.DbUpdate;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.able.jdbc.model.PkEntity;
import com.gitee.qdbp.jdbc.api.SqlBufferJdbcOperations;
import com.gitee.qdbp.jdbc.model.DbType;
import com.gitee.qdbp.jdbc.model.DbVersion;
import com.gitee.qdbp.jdbc.model.MainDbType;
import com.gitee.qdbp.jdbc.model.PrimaryKeyFieldColumn;
import com.gitee.qdbp.jdbc.plugins.BatchInsertExecutor;
import com.gitee.qdbp.jdbc.plugins.BatchUpdateExecutor;
import com.gitee.qdbp.jdbc.plugins.DbConditionConverter;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.SqlBuilder;
import com.gitee.qdbp.jdbc.sql.build.CrudSqlBuilder;
import com.gitee.qdbp.jdbc.utils.DbTools;

/**
 * 生成多条SQL语句一起执行的批量处理接口实现类<br>
 * <b>注意</b>: 只有mysql支持, oracle/db2都不支持<br>
 * <b>注意</b>: 如果是druid需要配置multiStatementAllow; 如果是mysql需要配置allowMultiQueries参数!<br>
 * <pre>
    &lt;bean id="sysDataSource" ...&gt;
        ...
        &lt;!-- 配置filters, proxyFilters一定要放在filters前面, 否则filters初始化时就会生成默认的 --&gt;
        &lt;property name="proxyFilters"&gt;
            &lt;list&gt;
                &lt;bean class="com.alibaba.druid.filter.stat.StatFilter"&gt;
                    &lt;property name="slowSqlMillis" value="30000" /&gt;
                    &lt;property name="logSlowSql" value="true" /&gt;
                    &lt;property name="mergeSql" value="true" /&gt;
                &lt;/bean&gt;
                &lt;bean class="com.alibaba.druid.wall.WallFilter"&gt;
                    &lt;property name="config"&gt;
                        &lt;bean id="wall-config" class="com.alibaba.druid.wall.WallConfig"&gt;
                            &lt;property name="multiStatementAllow" value="true" /&gt;
                        &lt;/bean&gt;
                    &lt;/property&gt;
                &lt;/bean&gt;
            &lt;/list&gt;
        &lt;/property&gt;
        &lt;property name="filters" value="config,stat,wall" /&gt;
    &lt;/bean&gt;
    </pre>
 * 
 * @author zhaohuihua
 * @version 20200707
 */
public class BatchOperateByMultiSqlExecutor implements BatchInsertExecutor, BatchUpdateExecutor {

    /**
     * 是否支持指定数据库<br>
     * 目前实测只有mysql支持, oracle/db2都不支持<br>
     * 如果有其他数据库支持, 可以继承此类, 覆盖supports方法
     */
    @Override
    public boolean supports(DbVersion version) {
        DbType dbType = version.getDbType();
        return dbType == MainDbType.MySQL || dbType == MainDbType.MariaDB;
    }

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
        SqlBuilder sql = new SqlBuilder();
        List<String> ids = new ArrayList<>();
        int size = entities.size();
        for (int i = 0; i < size; i++) {
            PkEntity item = entities.get(i);
            String id = item.getPrimaryKey();
            Map<String, Object> entity = item.getEntity();
            ids.add(id);
            if (i > 0) {
                sql.ad('\n');
                sql.omit(i, size); // 插入省略标记
            }
            // 拼接SQL
            SqlBuffer temp = sqlBuilder.buildInsertSql(entity);
            sql.ad(temp).ad(';');
        }

        // 执行批量数据库插入
        jdbc.batchInsert(sql.out());
        return ids;
    }

    /**
     * 根据主键编号批量更新实体对象<br>
     * 生成多条SQL语句一起执行:<br>
     * UPDATE {tableName} SET {columnName}={fieldValue}, ... WHERE ID={id};<br>
     * UPDATE {tableName} SET {columnName}={fieldValue}, ... WHERE ID={id};<br>
     * ...<br>
     * UPDATE {tableName} SET {columnName}={fieldValue}, ... WHERE ID={id};<br>
     */
    @Override
    public int updates(List<PkEntity> entities, SqlBufferJdbcOperations jdbc, CrudSqlBuilder sqlBuilder) {
        // 查找主键(批量更新必须要有主键)
        PrimaryKeyFieldColumn pk = sqlBuilder.helper().getPrimaryKey();
        DbConditionConverter converter = DbTools.getDbConditionConverter();
        SqlBuilder sql = new SqlBuilder();
        int size = entities.size();
        for (int i = 0; i < size; i++) {
            PkEntity item = entities.get(i);
            String pkValue = item.getPrimaryKey();
            Map<String, Object> entity = item.getEntity();
            // 生成主键过滤条件
            DbWhere where = new DbWhere();
            where.on(pk.getFieldName(), "=", pkValue);
            // entity转换为DbUpdate
            DbUpdate ud = converter.parseMapToDbUpdate(entity);
            if (i > 0) {
                sql.ad('\n');
                sql.omit(i, size); // 插入省略标记
            }
            // 拼接SQL
            SqlBuffer temp = sqlBuilder.buildUpdateSql(ud, where);
            sql.ad(temp).ad(';');
        }

        // 执行批量数据库插入
        return jdbc.batchUpdate(sql.out());
    }

}
