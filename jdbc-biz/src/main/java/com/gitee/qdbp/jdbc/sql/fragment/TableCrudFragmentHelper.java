package com.gitee.qdbp.jdbc.sql.fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import com.gitee.qdbp.able.jdbc.condition.DbField;
import com.gitee.qdbp.able.jdbc.condition.DbUpdate;
import com.gitee.qdbp.jdbc.exception.UnsupportedFieldExeption;
import com.gitee.qdbp.jdbc.model.SimpleFieldColumn;
import com.gitee.qdbp.jdbc.model.PrimaryKeyFieldColumn;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.utils.DbTools;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 单表增删改查SQL片段生成帮助类
 *
 * @author 赵卉华
 * @version 190601
 */
public class TableCrudFragmentHelper extends TableQueryFragmentHelper implements CrudFragmentHelper {

    private final Class<?> clazz;
    private final String tableName;
    private final PrimaryKeyFieldColumn primaryKey;

    /** 构造函数 **/
    public TableCrudFragmentHelper(Class<?> clazz) {
        super(DbTools.parseFieldColumns(clazz));
        this.clazz = clazz;
        this.tableName = DbTools.parseTableName(clazz);
        this.primaryKey = DbTools.parsePrimaryKey(clazz);
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildInsertValuesSql(Map<String, Object> entity) throws UnsupportedFieldExeption {
        VerifyTools.requireNotBlank(entity, "entity");

        List<String> unsupported = new ArrayList<String>();
        for (String fieldName : entity.keySet()) {
            if (!containsField(fieldName)) {
                unsupported.add(fieldName);
            }
        }
        if (!unsupported.isEmpty()) {
            throw ufe("insert values sql", unsupported);
        }

        // 根据列顺序生成SQL
        SqlBuffer buffer = new SqlBuffer();
        for (SimpleFieldColumn item : columns) {
            if (!entity.containsKey(item.getFieldName())) {
                continue;
            }
            if (!buffer.isEmpty()) {
                buffer.append(',');
            }
            buffer.addVariable(item.getFieldName(), entity.get(item.getFieldName()));
        }
        return buffer;
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildUpdateSetSql(DbUpdate entity) throws UnsupportedFieldExeption {
        return buildUpdateSetSql(entity, true);
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildUpdateSetSql(DbUpdate entity, boolean whole) throws UnsupportedFieldExeption {
        VerifyTools.requireNotBlank(entity, "entity");

        List<String> unsupported = new ArrayList<String>();
        SqlBuffer buffer = new SqlBuffer();
        for (DbField item : entity.fields()) {
            String operateType = VerifyTools.nvl(item.getOperateType(), "Set");
            String fieldName = item.getFieldName();
            Object fieldValue = item.getFieldValue();
            if (VerifyTools.isAnyBlank(fieldName, fieldValue)) {
                continue;
            }
            String columnName = getColumnName(fieldName);
            if (VerifyTools.isBlank(columnName)) {
                unsupported.add(fieldName);
                continue;
            }

            if (!buffer.isEmpty()) {
                buffer.append(',');
            }
            if ("ToNull".equals(operateType)) {
                buffer.append(columnName).append('=').append("NULL");
            } else if ("Add".equals(operateType)) {
                if (fieldValue instanceof Number && ((Number) fieldValue).doubleValue() < 0) {
                    buffer.append(columnName).append('=');
                    buffer.append(columnName).append('-');
                    buffer.addVariable(fieldName, fieldValue);
                } else {
                    buffer.append(columnName).append('=');
                    buffer.append(columnName).append('+');
                    buffer.addVariable(fieldName, fieldValue);
                }
            } else if ("Set".equals(operateType)) {
                buffer.append(columnName).append('=');
                buffer.addVariable(fieldName, fieldValue);
            } else {
                unsupported.add(fieldName + '(' + operateType + ')');
            }
        }
        if (!unsupported.isEmpty()) {
            throw ufe("update values sql", unsupported);
        }
        if (whole && !buffer.isEmpty()) {
            buffer.prepend("SET", ' ');
        }
        return buffer;
    }

    /** {@inheritDoc} **/
    public SqlBuffer buildFromSql(boolean whole) {
        SqlBuffer buffer = new SqlBuffer(tableName);
        if (whole) {
            buffer.prepend("FROM", ' ');
        }
        return buffer;
    }

    /** {@inheritDoc} **/
    @Override
    public String getTableName() {
        return tableName;
    }

    /** {@inheritDoc} **/
    @Override
    public PrimaryKeyFieldColumn getPrimaryKey() {
        return primaryKey;
    }

    protected UnsupportedFieldExeption ufe(String subject, String field) {
        String message = subject + " unsupported fields";
        return new UnsupportedFieldExeption(clazz.getSimpleName(), message, Arrays.asList(field));
    }

    protected UnsupportedFieldExeption ufe(String subject, List<String> fields) {
        String message = subject + " unsupported fields";
        return new UnsupportedFieldExeption(clazz.getSimpleName(), message, fields);
    }
}
