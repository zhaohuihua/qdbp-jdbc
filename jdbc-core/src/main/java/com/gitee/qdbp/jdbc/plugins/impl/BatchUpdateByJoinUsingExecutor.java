package com.gitee.qdbp.jdbc.plugins.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.gitee.qdbp.able.jdbc.model.PkEntity;
import com.gitee.qdbp.jdbc.api.SqlBufferJdbcOperations;
import com.gitee.qdbp.jdbc.model.AllFieldColumn;
import com.gitee.qdbp.jdbc.model.DbVersion;
import com.gitee.qdbp.jdbc.model.PrimaryKeyFieldColumn;
import com.gitee.qdbp.jdbc.model.SimpleFieldColumn;
import com.gitee.qdbp.jdbc.plugins.BatchUpdateExecutor;
import com.gitee.qdbp.jdbc.sql.SqlBuilder;
import com.gitee.qdbp.jdbc.sql.build.CrudSqlBuilder;
import com.gitee.qdbp.jdbc.sql.fragment.CrudFragmentHelper;
import com.gitee.qdbp.jdbc.utils.DbTools;
import com.gitee.qdbp.tools.utils.ConvertTools;

/**
 * UPDATE JOIN USING 批量更新接口实现类(要求字段对齐)<br>
 * MySQL专有语法, 效率高于BatchUpdateByCaseWhen<pre>
    UPDATE {tableName} A JOIN (
        SELECT {id1} ID, {field11} FIELD11, {field12} FIELD12, ..., {field1n} FIELD1n
        UNION
        SELECT {id2} ID, {field21} FIELD21, {field22} FIELD22, ..., {field2n} FIELD2n
    ) B USING(ID)
    SET A.FIELD1=B.FIELD1, A.FIELD2=B.FIELD2, ..., A.FIELDn=B.FIELDn</pre>
 * 
 * @author zhaohuihua
 * @version 20200712
 */
public class BatchUpdateByJoinUsingExecutor implements BatchUpdateExecutor {

    /** 是否支持指定数据库 **/
    @Override
    public boolean supports(DbVersion version) {
        String key = "qdbc." + this.getClass().getSimpleName();
        String defvalue = "MySQL;MariaDB";
        String options = DbTools.getDbConfig().getStringUseDefValue(key, defvalue);
        return version.matchesWith(options);
    }

    @Override
    public int updates(List<PkEntity> entities, SqlBufferJdbcOperations jdbc, CrudSqlBuilder sqlBuilder) {
        CrudFragmentHelper sqlHelper = sqlBuilder.helper();
        String tableName = sqlHelper.getTableName();
        PrimaryKeyFieldColumn pk = sqlHelper.getPrimaryKey();
        Set<String> fieldNames = mergeFields(entities);

        // 检查字段名
        sqlHelper.checkSupportedFields(fieldNames, "build batch update sql");
        // 字段名映射表
        Map<String, ?> fieldMap = ConvertTools.toMap(fieldNames);

        AllFieldColumn<? extends SimpleFieldColumn> columns = sqlHelper.getAllFieldColumns();

        // UPDATE {tableName} A JOIN (
        //     SELECT {id1} ID, {field11} FIELD11, {field12} FIELD12, ..., {field1n} FIELD1n
        //     UNION
        //     SELECT {id2} ID, {field21} FIELD21, {field22} FIELD22, ..., {field2n} FIELD2n
        // ) B USING(ID)
        // SET A.FIELD1=B.FIELD1, A.FIELD2=B.FIELD2, ..., A.FIELDn=B.FIELDn

        SqlBuilder sql = new SqlBuilder();
        // UPDATE {tableName} A JOIN (
        sql.ad("UPDATE").ad(tableName).ad('A').ad("JOIN").ad('(').newline().tab();
        // 根据列顺序生成SQL
        int size = entities.size();
        for (int i = 0; i < size; i++) {
            PkEntity item = entities.get(i);
            if (i > 0) {
                sql.newline().ad("UNION").newline();
            }
            sql.omit(i, size); // 插入省略标记
            // SELECT {id1} ID, {field11} FIELD11, {field12} FIELD12, ..., {field1n} FIELD1n
            sql.ad("SELECT").var(item.getPrimaryKey()).ad(pk.getColumnName());
            Map<String, Object> entity = item.getEntity();
            for (SimpleFieldColumn column : columns.items()) {
                String fieldName = column.getFieldName();
                if (fieldMap.containsKey(fieldName) && !fieldName.equals(pk.getFieldName())) {
                    Object fieldValue = entity.get(fieldName);
                    // 不用调sqlHelper.convertFieldValue
                    // 因为不支持DbFieldName/DbFieldValue/DbRawValue, 只支持fieldName=fieldValue
                    // fieldValue = sqlHelper.convertFieldValue(fieldValue);
                    sql.ad(',', ' ').var(fieldValue).ad(column.getColumnName());
                }
            }
        }
        // ) B USING(ID)
        sql.newline().tab(-1).ad(')').ad('B').ad("USING").ad('(').ad(pk.getColumnName()).ad(')');
        // SET
        sql.newline().ad("SET");
        boolean firstColumn = true;
        // A.FIELD1=B.FIELD1, A.FIELD2=B.FIELD2, ..., A.FIELDn=B.FIELDn
        for (SimpleFieldColumn column : columns.items()) {
            String fieldName = column.getFieldName();
            if (fieldMap.containsKey(fieldName) && !fieldName.equals(pk.getFieldName())) {
                if (firstColumn) {
                    firstColumn = false;
                } else {
                    sql.ad(',', ' ');
                }
                sql.ad('A', '.').ad(column.getColumnName());
                sql.ad('=');
                sql.ad('B', '.').ad(column.getColumnName());
            }
        }

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
