package com.gitee.qdbp.jdbc.sql.fragment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.gitee.qdbp.able.jdbc.base.DbCondition;
import com.gitee.qdbp.able.jdbc.base.UpdateCondition;
import com.gitee.qdbp.able.jdbc.condition.DbField;
import com.gitee.qdbp.able.jdbc.condition.DbUpdate;
import com.gitee.qdbp.jdbc.exception.UnsupportedFieldException;
import com.gitee.qdbp.jdbc.model.PrimaryKeyFieldColumn;
import com.gitee.qdbp.jdbc.model.SimpleFieldColumn;
import com.gitee.qdbp.jdbc.operator.DbBaseOperator;
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
        super(DbTools.parseToAllFieldColumn(clazz), dialect);
        this.clazz = clazz;
        this.tableName = DbTools.parseTableName(clazz);
        this.primaryKey = DbTools.parsePrimaryKey(clazz);
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildInsertValuesSql(Map<String, Object> entity) throws UnsupportedFieldException {
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
        for (SimpleFieldColumn item : this.columns.items()) {
            if (!entity.containsKey(item.getFieldName())) {
                continue;
            }
            if (!buffer.isEmpty()) {
                buffer.append(',');
            }
            buffer.addVariable(entity.get(item.getFieldName()));
        }
        return buffer;
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildUpdateSetSql(DbUpdate entity, boolean whole) throws UnsupportedFieldException {
        VerifyTools.requireNotBlank(entity, "entity");

        List<String> unsupported = new ArrayList<String>();
        SqlBuffer buffer = new SqlBuffer();
        Iterator<DbCondition> iterator = entity.iterator();
        while (iterator.hasNext()) {
            DbCondition condition = iterator.next();
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
            } catch (UnsupportedFieldException e) {
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

    public SqlBuffer buildUpdateSql(DbField field, boolean whole) throws UnsupportedFieldException {
        if (field == null) {
            return null;
        }
        String operateType = VerifyTools.nvl(field.getOperateType(), "Set");
        String fieldName = field.getFieldName();
        Object fieldValue = field.getFieldValue();
        if (VerifyTools.isBlank(fieldName)) {
            throw ufe("build update sql", "fieldName:MustNotBe" + (fieldName == null ? "Null" : "Empty"));
        }
        String columnName = getColumnName(fieldName);

        // 查找Update运算符处理类
        DbBaseOperator operator = DbTools.getUpdateOperator(operateType);
        if (operator == null) {
            throw ufe("build where sql", "UnsupportedOperator:(" + fieldName + ' ' + operateType + " ...)");
        }
        // 由运算符处理类生成子SQL
        String desc = "build update sql unsupported field";
        SqlBuffer buffer = buildOperatorSql(fieldName, columnName, operator, fieldValue, desc);

        if (whole && !buffer.isEmpty()) {
            buffer.prepend("SET", ' ');
        }
        return buffer;
    }

    /** {@inheritDoc} **/
    @Override
    public <T extends UpdateCondition> SqlBuffer buildUpdateSql(T condition, boolean whole)
            throws UnsupportedFieldException {
        if (condition == null || condition.isEmpty()) {
            return null;
        }

        Class<? extends UpdateCondition> type = condition.getClass();
        // JDK8+不用强转
        @SuppressWarnings("unchecked")
        UpdateSqlBuilder<T> builder = (UpdateSqlBuilder<T>) DbPluginContainer.defaults().getUpdateSqlBuilder(type);
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

    @Override
    protected String getOwnerDescString() {
        return clazz.getSimpleName();
    }

}
