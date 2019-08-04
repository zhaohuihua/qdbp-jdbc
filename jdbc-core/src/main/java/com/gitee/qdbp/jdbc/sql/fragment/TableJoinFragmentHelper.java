package com.gitee.qdbp.jdbc.sql.fragment;

import java.util.Arrays;
import java.util.List;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.able.jdbc.condition.TableJoin;
import com.gitee.qdbp.able.jdbc.condition.TableJoin.JoinItem;
import com.gitee.qdbp.able.jdbc.condition.TableJoin.JoinType;
import com.gitee.qdbp.able.jdbc.condition.TableJoin.TableItem;
import com.gitee.qdbp.jdbc.exception.UnsupportedFieldExeption;
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
                    buffer.append(' ').append("ON").append(buildWhereSql(where, false));
                }
            }
        }
        if (whole) {
            buffer.prepend("FROM", ' ');
        }
        return buffer;
    }

    protected UnsupportedFieldExeption ufe(String subject, String field) {
        String message = subject + " unsupported fields";
        return new UnsupportedFieldExeption(toDescString(tables), message, Arrays.asList(field));
    }

    protected UnsupportedFieldExeption ufe(String subject, List<String> fields) {
        String message = subject + " unsupported fields";
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
