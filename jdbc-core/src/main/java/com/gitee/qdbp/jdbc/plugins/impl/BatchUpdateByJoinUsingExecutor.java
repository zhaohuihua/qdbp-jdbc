package com.gitee.qdbp.jdbc.plugins.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.gitee.qdbp.able.jdbc.model.PkEntity;
import com.gitee.qdbp.jdbc.api.SqlBufferJdbcOperations;
import com.gitee.qdbp.jdbc.model.AllFieldColumn;
import com.gitee.qdbp.jdbc.model.DbType;
import com.gitee.qdbp.jdbc.model.DbVersion;
import com.gitee.qdbp.jdbc.model.MainDbType;
import com.gitee.qdbp.jdbc.model.PrimaryKeyFieldColumn;
import com.gitee.qdbp.jdbc.model.SimpleFieldColumn;
import com.gitee.qdbp.jdbc.plugins.BatchUpdateExecutor;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.build.CrudSqlBuilder;
import com.gitee.qdbp.jdbc.sql.fragment.CrudFragmentHelper;
import com.gitee.qdbp.tools.utils.ConvertTools;

/**
 * MySQL UPDATE JOIN USING 批量更新接口实现类<br>
 * UPDATE {tableName} A JOIN (<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;SELECT {id1} ID, {field11} FIELD11, {field12} FIELD12, ..., {field1n} FIELD1n<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;UNION<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;SELECT {id2} ID, {field21} FIELD21, {field22} FIELD22, ..., {field2n} FIELD2n<br>
 * ) B USING(ID)<br>
 * SET A.FIELD1=B.FIELD1, A.FIELD2=B.FIELD2, ..., A.FIELDn=B.FIELDn <br>
 * 
 * @author zhaohuihua
 * @version 20200712
 */
public class BatchUpdateByJoinUsingExecutor implements BatchUpdateExecutor {

    /**
     * 是否支持指定数据库<br>
     * 目前已知只有mysql支持<br>
     * 如果有其他数据库支持, 可以继承此类, 覆盖supports方法
     */
    @Override
    public boolean supports(DbVersion version) {
        DbType dbType = version.getDbType();
        return dbType == MainDbType.MySQL || dbType == MainDbType.MariaDB;
    }

    @Override
    public int updates(List<PkEntity> contents, SqlBufferJdbcOperations jdbc, CrudSqlBuilder sqlBuilder) {
        CrudFragmentHelper sqlHelper = sqlBuilder.helper();
        String tableName = sqlHelper.getTableName();
        PrimaryKeyFieldColumn pk = sqlHelper.getPrimaryKey();
        Set<String> fieldNames = mergeFields(contents);

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

        SqlBuffer buffer = new SqlBuffer();
        // UPDATE {tableName} A JOIN (
        buffer.append("UPDATE").append(' ', tableName).append(' ', 'A').append(' ', "JOIN").append(' ', '(');
        // 根据列顺序生成SQL
        boolean firstRow = true;
        for (PkEntity item : contents) {
            if (firstRow) {
                firstRow = false;
            } else {
                buffer.append('\n', '\t').append("UNION");
            }
            // SELECT {id1} ID, {field11} FIELD11, {field12} FIELD12, ..., {field1n} FIELD1n
            buffer.append('\n', '\t');
            buffer.append("SELECT", ' ').addVariable(item.getPrimaryKey()).append(' ', pk.getColumnName());
            Map<String, Object> entity = item.getEntity();
            for (SimpleFieldColumn column : columns.items()) {
                String fieldName = column.getFieldName();
                if (fieldMap.containsKey(fieldName) && !fieldName.equals(pk.getFieldName())) {
                    Object fieldValue = entity.get(fieldName);
                    // 不支持DbFieldName/DbFieldValue/DbRawValue
                    // 只支持fieldName=fieldValue
                    // fieldValue = sqlHelper.convertFieldValue(fieldValue);
                    buffer.append(',', ' ').addVariable(fieldValue).append(' ', column.getColumnName());
                }
            }
        }
        // ) B USING(ID)
        buffer.append('\n', ')', ' ', 'B').append(' ', "USING").append('(', pk.getColumnName(), ')');
        // SET
        buffer.append('\n', "SET", ' ');
        boolean firstColumn = true;
        // A.FIELD1=B.FIELD1, A.FIELD2=B.FIELD2, ..., A.FIELDn=B.FIELDn
        for (SimpleFieldColumn column : columns.items()) {
            String fieldName = column.getFieldName();
            if (fieldMap.containsKey(fieldName) && !fieldName.equals(pk.getFieldName())) {
                if (firstColumn) {
                    firstColumn = false;
                } else {
                    buffer.append(',', ' ');
                }
                buffer.append('A', '.').append(column.getColumnName());
                buffer.append('=');
                buffer.append('B', '.').append(column.getColumnName());
            }
        }

        // 执行批量数据库更新
        return jdbc.batchUpdate(buffer);
    }

    private Set<String> mergeFields(List<PkEntity> entities) {
        Set<String> fieldNames = new HashSet<>();
        for (PkEntity item : entities) {
            fieldNames.addAll(item.getEntity().keySet());
        }
        return fieldNames;
    }
}
