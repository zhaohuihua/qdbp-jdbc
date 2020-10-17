package com.gitee.qdbp.jdbc.plugins.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.gitee.qdbp.able.jdbc.model.PkEntity;
import com.gitee.qdbp.jdbc.api.SqlBufferJdbcOperations;
import com.gitee.qdbp.jdbc.model.AllFieldColumn;
import com.gitee.qdbp.jdbc.model.DbVersion;
import com.gitee.qdbp.jdbc.model.FieldColumns;
import com.gitee.qdbp.jdbc.model.FieldScene;
import com.gitee.qdbp.jdbc.model.OmitStrategy;
import com.gitee.qdbp.jdbc.model.SimpleFieldColumn;
import com.gitee.qdbp.jdbc.plugins.BatchUpdateExecutor;
import com.gitee.qdbp.jdbc.sql.SqlBuilder;
import com.gitee.qdbp.jdbc.sql.build.CrudSqlBuilder;
import com.gitee.qdbp.jdbc.sql.fragment.CrudFragmentHelper;
import com.gitee.qdbp.jdbc.utils.DbTools;
import com.gitee.qdbp.tools.utils.ConvertTools;

/**
 * UPDATE CASE WHEN 批量更新接口实现类(要求字段对齐)<pre>
    UPDATE {tableName} SET
        FIELD1=(CASE ID
          WHEN {id1} THEN {field11}
          WHEN {id2} THEN {field12}
          WHEN {idn} THEN {field1n}
        ELSE FIELD1 END),
        FIELD2=(CASE ID
          WHEN {id1} THEN {field21}
          WHEN {id2} THEN {field22}
          WHEN {idn} THEN {field2n}
        ELSE FIELD2 END)
    WHERE ID IN (id1,id2,idn)</pre>
 * 
 * @author zhaohuihua
 * @version 20200722
 */
public class BatchUpdateByCaseWhenExecutor implements BatchUpdateExecutor {

    /** 是否支持指定数据库 **/
    @Override
    public boolean supports(DbVersion version) {
        String key = "qdbc.supports." + this.getClass().getSimpleName();
        String defvalue = "mysql,mariadb,oracle,db2";
        String options = DbTools.getDbConfig().getStringUseDefValue(key, defvalue);
        return version.matchesWith(options);
    }

    @Override
    public int updates(List<PkEntity> entities, SqlBufferJdbcOperations jdbc, CrudSqlBuilder sqlBuilder) {
        CrudFragmentHelper sqlHelper = sqlBuilder.helper();
        String tableName = sqlHelper.getTableName();
        SimpleFieldColumn pk = sqlHelper.getPrimaryKey();
        Set<String> fieldNames = mergeFields(entities);
        // 获取批量操作语句的省略策略配置项
        OmitStrategy omits = DbTools.getOmitSizeConfig("qdbc.batch.sql.omitStrategy", "8:3");

        // 检查字段名
        sqlHelper.checkSupportedFields(FieldScene.UPDATE, fieldNames, "build batch update sql");
        // 字段名映射表
        Map<String, ?> fieldMap = ConvertTools.toMap(fieldNames);

        AllFieldColumn<? extends SimpleFieldColumn> all = sqlHelper.getAllFieldColumns();
        FieldColumns<? extends SimpleFieldColumn> fieldColumns = all.filter(FieldScene.UPDATE);
        // UPDATE {tableName} SET
        //     FIELD1=(CASE ID
        //       WHEN {id1} THEN {field11}
        //       WHEN {id2} THEN {field12}
        //       WHEN {idn} THEN {field1n}
        //     ELSE FIELD1 END),
        //     FIELD2=(CASE ID
        //       WHEN {id1} THEN {field21}
        //       WHEN {id2} THEN {field22}
        //       WHEN {idn} THEN {field2n}
        //     ELSE FIELD2 END)
        // WHERE ID IN (id1,id2,idn)

        SqlBuilder sql = new SqlBuilder();
        // UPDATE {tableName} SET
        sql.ad("UPDATE").ad(tableName).ad("SET").newline().tab();
        // 根据列顺序生成SQL
        int size = entities.size();
        boolean first = true;
        for (SimpleFieldColumn item : fieldColumns) {
            String fieldName = item.getFieldName();
            if (!fieldMap.containsKey(fieldName) || fieldName.equals(pk.getFieldName())) {
                continue;
            }
            if (first) {
                first = false;
            } else {
                sql.ad(',').newline();
            }
            // FIELD1=(CASE ID
            sql.ad(item.getColumnName()).ad('=', '(').ad("CASE").ad(pk.getColumnName()).newline().tab();
            for (int i = 0; i < size; i++) {
                if (i > 0) {
                    sql.newline();
                }
                PkEntity pkEntity = entities.get(i);
                if (omits.getMinSize() > 0 && size > omits.getMinSize()) {
                    sql.omit(i, size, omits.getKeepSize()); // 插入省略标记
                }
                Map<String, Object> entity = pkEntity.getEntity();
                Object fieldValue = entity.get(fieldName);
                // 不用调sqlHelper.convertFieldValue
                // 因为不支持DbFieldName/DbFieldValue/DbRawValue, 只支持fieldName=fieldValue
                // fieldValue = sqlHelper.convertFieldValue(fieldValue);
                // WHEN {id1} THEN {field11}
                sql.ad("WHEN").var(pkEntity.getPrimaryKey()).ad("THEN").var(fieldValue);
            }
            // ELSE FIELD1 END)
            sql.newline().tab(-1).ad("ELSE").ad(item.getColumnName()).ad("END").ad(')');
        }
        // WHERE ID IN (
        sql.newline().ad("WHERE").ad(pk.getColumnName()).ad("IN").ad('(').newline().tab();
        // id1,id2,idn
        for (int i = 0; i < size; i++) {
            PkEntity item = entities.get(i);
            if (i > 0) {
                sql.ad(',').newline();
            }
            if (omits.getMinSize() > 0 && size > omits.getMinSize()) {
                sql.omit(i, size, omits.getKeepSize()); // 插入省略标记
            }
            // 获取主键值
            sql.var(item.getPrimaryKey());
        }
        // )
        sql.newline().tab(-1).ad(')');

        // 执行批量数据库更新
        return jdbc.batchUpdate(sql.out());
    }

    private Set<String> mergeFields(List<PkEntity> entities) {
        Set<String> fieldNames = new HashSet<>();
        for (PkEntity item : entities) {
            fieldNames.addAll(item.getEntity().keySet());
        }
        return fieldNames;
    }
}
