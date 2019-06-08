package com.gitee.qdbp.jdbc.sql.fragment;

import java.util.Arrays;
import java.util.List;
import com.gitee.qdbp.jdbc.condition.TableJoin;
import com.gitee.qdbp.jdbc.condition.TableJoin.JoinItem;
import com.gitee.qdbp.jdbc.condition.TableJoin.TableItem;
import com.gitee.qdbp.jdbc.exception.UnsupportedFieldExeption;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.utils.DbTools;
import com.gitee.qdbp.tools.utils.StringTools;
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
    public TableJoinFragmentHelper(TableJoin tables) {
        super(DbTools.parseFieldColumns(tables));
        this.tables = tables;
    }

    /** {@inheritDoc} **/
    public SqlBuffer buildFromSql(boolean whole) {
        SqlBuffer buffer = new SqlBuffer();
        buffer.append();
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
