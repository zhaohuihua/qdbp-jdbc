package com.gitee.qdbp.jdbc.sql.fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.able.jdbc.condition.TableJoin;
import com.gitee.qdbp.able.jdbc.condition.TableJoin.JoinItem;
import com.gitee.qdbp.able.jdbc.condition.TableJoin.JoinType;
import com.gitee.qdbp.able.jdbc.condition.TableJoin.TableItem;
import com.gitee.qdbp.jdbc.exception.UnsupportedFieldExeption;
import com.gitee.qdbp.jdbc.model.SimpleFieldColumn;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.utils.DbTools;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 表关联SQL片段生成帮助类
 *
 * @author 赵卉华
 * @version 190601
 */
public class TableJoinFragmentHelper extends TableQueryFragmentHelper {

    private TableJoin tables;

    /** 构造函数 **/
    public TableJoinFragmentHelper(TableJoin tables, SqlDialect dialect) {
        super(DbTools.parseFieldColumns(tables), dialect);
        this.tables = tables;
    }

    protected List<SimpleFieldColumn> getFields(String fieldName) {
        List<SimpleFieldColumn> fields = new ArrayList<>();
        if (VerifyTools.isBlank(fieldName) || VerifyTools.isBlank(columns)) {
            return fields;
        }
        for (SimpleFieldColumn item : this.columns) {
            if (item.matchesByFieldName(fieldName)) {
                fields.add(item);
            }
        }
        return fields;
    }

    /** {@inheritDoc} **/
    @Override
    public boolean containsField(String fieldName) {
        if (VerifyTools.isBlank(fieldName) || VerifyTools.isBlank(columns)) {
            return false;
        }
        List<SimpleFieldColumn> fields = getFields(fieldName);
        return !fields.isEmpty();
    }

    /** {@inheritDoc} **/
    @Override
    public String getColumnName(String fieldName) throws UnsupportedFieldExeption {
        List<SimpleFieldColumn> fields = getFields(fieldName);
        if (fields.isEmpty()) {
            throw ufe("unsupported field", fieldName);
        } else if (fields.size() > 1) {
            throw ufe("unsupported field", "AmbiguousField:" + fieldName);
        } else {
            return fields.get(0).toTableColumnName();
        }
    }

    /** {@inheritDoc} **/
    @Override
    public String getColumnName(String fieldName, boolean throwOnUnsupportedField) throws UnsupportedFieldExeption {
        List<SimpleFieldColumn> fields = getFields(fieldName);
        if (fields.isEmpty()) {
            if (throwOnUnsupportedField) {
                throw ufe("unsupported field", fieldName);
            } else {
                return null;
            }
        } else if (fields.size() > 1) {
            if (throwOnUnsupportedField) {
                throw ufe("unsupported field", "AmbiguousField:" + fieldName);
            } else {
                return null;
            }
        } else {
            return fields.get(0).toTableColumnName();
        }
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer doBuildSpecialFieldsSql(Collection<String> fields, boolean columnAlias) throws UnsupportedFieldExeption {
        VerifyTools.requireNotBlank(fields, "fields");

        // 字段名映射
        Map<String, Void> fieldMap = new HashMap<String, Void>();
        List<String> unsupported = new ArrayList<String>();
        for (String fieldName : fields) {
            List<SimpleFieldColumn> matchesFields = getFields(fieldName);
            if (matchesFields.isEmpty()) {
                unsupported.add(fieldName);
            } else if (matchesFields.size() > 1) {
                unsupported.add("AmbiguousField:" + fieldName);
            } else {
                fieldMap.put(fieldName, null);
            }
        }
        if (!unsupported.isEmpty()) {
            // Column 'xxx' in field list is ambiguous
            throw ufe("build field sql unsupported fields", unsupported);
        }

        // 根据列顺序生成SQL
        SqlBuffer buffer = new SqlBuffer();
        for (SimpleFieldColumn item : columns) {
            if (fieldMap.containsKey(item.toTableFieldName()) || fieldMap.containsKey(item.getFieldName())) {
                if (!buffer.isEmpty()) {
                    buffer.append(',');
                }
                buffer.append(columnAlias ? item.toFullColumnName() : item.toTableColumnName());
            }
        }
        return buffer;
    }

    /** {@inheritDoc} **/
    public SqlBuffer buildFromSql(boolean whole) {
        SqlBuffer buffer = new SqlBuffer();
        // 主表
        TableItem major = tables.getMajor();
        buffer.append(DbTools.parseTableName(major.getTableType()));
        if (VerifyTools.isNotBlank(major.getTableAlias())) {
            buffer.append(' ').append(major.getTableAlias().toUpperCase());
        }
        List<JoinItem> joins = tables.getJoins();
        if (VerifyTools.isNotBlank(joins)) {
            for (JoinItem item : joins) { // 关联表
                JoinType joinType = VerifyTools.nvl(item.getJoinType(), JoinType.InnerJoin);
                buffer.append(' ').append(joinType.toSqlString()); // 关联类型
                buffer.append(' ').append(DbTools.parseTableName(item.getTableType()));
                if (VerifyTools.isNotBlank(item.getTableAlias())) { // 表别名
                    buffer.append(' ').append(item.getTableAlias().toUpperCase());
                }
                DbWhere where = item.getWhere();
                if (where != null && !where.isEmpty()) { // 关联条件
                    buffer.append(' ', "ON", ' ').append(buildWhereSql(where, false));
                }
            }
        }
        if (whole) {
            buffer.prepend("FROM", ' ');
        }
        return buffer;
    }

    protected UnsupportedFieldExeption ufe(String message, String field) {
        return new UnsupportedFieldExeption(toDescString(tables), message, Arrays.asList(field));
    }

    protected UnsupportedFieldExeption ufe(String message, List<String> fields) {
        return new UnsupportedFieldExeption(toDescString(tables), message, fields);
    }

    private String toDescString(TableJoin tables) {
        StringBuilder buffer = new StringBuilder();
        TableItem major = tables.getMajor();
        buffer.append(major.getTableType().getSimpleName());
        List<JoinItem> joins = tables.getJoins();
        if (VerifyTools.isNotBlank(joins)) {
            for (JoinItem item : joins) {
                buffer.append('+').append(item.getTableType().getSimpleName());
            }
        }
        return buffer.toString();
    }
}
