package com.gitee.qdbp.jdbc.sql.fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import com.gitee.qdbp.able.jdbc.base.DbCondition;
import com.gitee.qdbp.able.jdbc.base.UpdateCondition;
import com.gitee.qdbp.able.jdbc.condition.DbField;
import com.gitee.qdbp.able.jdbc.condition.DbUpdate;
import com.gitee.qdbp.jdbc.exception.UnsupportedFieldExeption;
import com.gitee.qdbp.jdbc.model.PrimaryKeyFieldColumn;
import com.gitee.qdbp.jdbc.model.SimpleFieldColumn;
import com.gitee.qdbp.jdbc.plugins.DbPluginContainer;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.plugins.UpdateSqlBuilder;
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
    public TableCrudFragmentHelper(Class<?> clazz, SqlDialect dialect) {
        super(DbTools.parseFieldColumns(clazz), dialect);
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
    public SqlBuffer buildUpdateSetSql(DbUpdate entity, boolean whole) throws UnsupportedFieldExeption {
        VerifyTools.requireNotBlank(entity, "entity");

        List<String> unsupported = new ArrayList<String>();
        SqlBuffer buffer = new SqlBuffer();
        for (DbCondition condition : entity.items()) {
            if (!buffer.isEmpty()) {
                buffer.append(',');
            }
            try {
                if (condition instanceof UpdateCondition) {
                    UpdateCondition subCondition = (UpdateCondition) condition;
                    SqlBuffer subSql = buildUpdateSql(subCondition, false);
                    if (!subSql.isEmpty()) {
                        buffer.append(subSql);
                    }
                } else if (condition instanceof DbField) {
                    DbField item = (DbField) condition;
                    SqlBuffer fieldSql = buildUpdateSql(item, false);
                    buffer.append(fieldSql);
                } else {
                    unsupported.add(condition.getClass().getSimpleName() + "#UnsupportedCondition");
                }
            } catch (UnsupportedFieldExeption e) {
                unsupported.addAll(e.getFields());
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

    public SqlBuffer buildUpdateSql(DbField field, boolean whole) throws UnsupportedFieldExeption {
        if (field == null) {
            return null;
        }
        SqlBuffer buffer = new SqlBuffer();
        String operateType = VerifyTools.nvl(field.getOperateType(), "Set");
        String fieldName = field.getFieldName();
        Object fieldValue = field.getFieldValue();
        if (VerifyTools.isBlank(fieldName)) {
            throw ufe("update sql", "fieldName$" + operateType + "#IsBlank");
        }
        String columnName = getColumnName(fieldName);
        if (VerifyTools.isBlank(columnName)) {
            throw ufe("update sql", fieldName);
        }

        if ("ToNull".equals(operateType)) {
            buffer.append(columnName).append('=').append("NULL");
        } else if ("Add".equals(operateType)) {
            if (VerifyTools.isBlank(fieldValue)) {
                throw ufe("update sql", fieldName + '$' + operateType + '(' + fieldValue + "#IsBlank" + ')');
            }
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
            if (VerifyTools.isBlank(fieldValue)) {
                throw ufe("update sql", fieldName + '$' + operateType + '(' + fieldValue + "#IsBlank" + ')');
            }
            buffer.append(columnName).append('=');
            buffer.addVariable(fieldName, fieldValue);
        } else {
            throw ufe("update sql", fieldName + '$' + operateType + '(' + "#UnsupportedOperate" + ')');
        }
        if (whole && !buffer.isEmpty()) {
            buffer.prepend("SET", ' ');
        }
        return buffer;
    }

    /** {@inheritDoc} **/
    @Override
    public <T extends UpdateCondition> SqlBuffer buildUpdateSql(T condition, boolean whole)
            throws UnsupportedFieldExeption {
        if (condition == null || condition.isEmpty()) {
            return null;
        }

        Class<? extends UpdateCondition> type = condition.getClass();
        // JDK8+不用强转
        @SuppressWarnings("unchecked")
        UpdateSqlBuilder<T> builder = (UpdateSqlBuilder<T>) DbPluginContainer.global.getUpdateSqlBuilder(type);
        if (builder == null) {
            throw ufe("update sql", condition.getClass().getSimpleName() + "#SqlBuilderNotFound");
        }
        SqlBuffer buffer = builder.buildSql(condition, this);
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
