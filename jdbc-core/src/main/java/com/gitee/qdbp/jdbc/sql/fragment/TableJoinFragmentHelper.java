package com.gitee.qdbp.jdbc.sql.fragment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.able.jdbc.condition.TableJoin;
import com.gitee.qdbp.able.jdbc.condition.TableJoin.JoinItem;
import com.gitee.qdbp.able.jdbc.condition.TableJoin.JoinType;
import com.gitee.qdbp.able.jdbc.condition.TableJoin.TableItem;
import com.gitee.qdbp.jdbc.exception.UnsupportedFieldException;
import com.gitee.qdbp.jdbc.model.FieldColumns;
import com.gitee.qdbp.jdbc.model.FieldScene;
import com.gitee.qdbp.jdbc.model.SimpleFieldColumn;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.SqlBuilder;
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
        super(DbTools.parseAllFieldColumns(tables), dialect);
        this.tables = tables;
    }


    /** {@inheritDoc} **/
    @Override
    public boolean containsField(FieldScene scene, String fieldName) {
        if (VerifyTools.isBlank(fieldName)) {
            return false;
        }
        List<? extends SimpleFieldColumn> fields = this.columns.filter(scene).findAllByFieldName(fieldName);
        return !fields.isEmpty();
    }

    /** {@inheritDoc} **/
    @Override
    public String getColumnName(FieldScene scene, String fieldName) throws UnsupportedFieldException {
        List<? extends SimpleFieldColumn> fields = this.columns.filter(scene).findAllByFieldName(fieldName);
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
    public String getColumnName(FieldScene scene, String fieldName, boolean throwOnUnsupportedField) throws UnsupportedFieldException {
        List<? extends SimpleFieldColumn> fields = this.columns.filter(scene).findAllByFieldName(fieldName);
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
    protected SqlBuffer doBuildSpecialFieldsSql(FieldScene scene, Collection<String> fields, boolean isWhitelist, boolean columnAlias)
            throws UnsupportedFieldException {
        VerifyTools.requireNotBlank(fields, "fields");

        FieldColumns<? extends SimpleFieldColumn> fieldColumns = this.columns.filter(scene);
        // 字段名映射
        Map<String, Void> fieldMap = new HashMap<String, Void>();
        List<String> unsupported = new ArrayList<String>();
        for (String fieldName : fields) {
            List<? extends SimpleFieldColumn> matchesFields = fieldColumns.findAllByFieldName(fieldName);
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
        SqlBuilder buffer = new SqlBuilder();
        for (SimpleFieldColumn item : fieldColumns) {
            boolean exists = fieldMap.containsKey(item.toTableFieldName()) || fieldMap.containsKey(item.getFieldName());
            if (exists == isWhitelist) {
                if (!buffer.isEmpty()) {
                    buffer.ad(',');
                }
                buffer.ad(columnAlias ? item.toFullColumnName() : item.toTableColumnName());
            }
        }
        return buffer.out();
    }

    /** {@inheritDoc} **/
    public SqlBuffer buildFromSql(boolean whole) {
        SqlBuilder buffer = new SqlBuilder();
        // 主表
        TableItem major = tables.getMajor();
        buffer.ad(DbTools.parseTableName(major.getTableType()));
        if (VerifyTools.isNotBlank(major.getTableAlias())) {
            buffer.ad(major.getTableAlias().toUpperCase());
        }
        List<JoinItem> joins = tables.getJoins();
        if (VerifyTools.isNotBlank(joins)) {
            for (JoinItem item : joins) { // 关联表
                buffer.newline();
                JoinType joinType = VerifyTools.nvl(item.getJoinType(), JoinType.InnerJoin);
                buffer.ad(joinType.toSqlString()); // 关联类型
                buffer.ad(DbTools.parseTableName(item.getTableType()));
                if (VerifyTools.isNotBlank(item.getTableAlias())) { // 表别名
                    buffer.ad(item.getTableAlias().toUpperCase());
                }
                DbWhere where = item.getWhere();
                if (where != null && !where.isEmpty()) { // 关联条件
                    buffer.ad("ON").ad(buildWhereSql(where, false));
                }
            }
        }
        if (whole) {
            buffer.pd("FROM");
        }
        return buffer.out();
    }

    private String tablesDescString;

    @Override
    protected String getOwnerDescString() {
        if (tablesDescString == null) {
            tablesDescString = getTablesDescString(tables);
        }
        return tablesDescString;
    }

    private String getTablesDescString(TableJoin tables) {
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
