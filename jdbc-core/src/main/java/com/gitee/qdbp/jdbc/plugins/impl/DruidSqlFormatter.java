package com.gitee.qdbp.jdbc.plugins.impl;

import java.util.regex.Pattern;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.util.JdbcConstants;
import com.gitee.qdbp.jdbc.plugins.SqlFormatter;
import com.gitee.qdbp.tools.utils.IndentTools;
import com.gitee.qdbp.tools.utils.StringTools;

/**
 * 使用DruidSqlFormat格式化SQL(有些注释会丢失,效果不理想)<br>
 * https://github.com/alibaba/druid/wiki/SQL_Format<br>
 * 需要引用com.alibaba.druid
 *
 * @author zhaohuihua
 * @version 20200719
 */
public class DruidSqlFormatter implements SqlFormatter {

    public String format(String sql) {
        return format(sql, 0);
    }

    public String format(String sql, int indent) {
        String formatted = formatSql(sql);
        String pretty = prettySql(formatted);
        if (indent <= 0) {
            return pretty;
        } else {
            return IndentTools.indentAll(pretty, indent);
        }
    }

    private static Pattern LEADING_COMMA = Pattern.compile("(\\r?\\n\\s*)(,)(\\s*)");

    protected String formatSql(String sql) {
        return SQLUtils.format(sql, JdbcConstants.ORACLE);
    }

    protected String prettySql(String sql) {
        // druid sql format的换行风格是换行后加前置逗号, 改为逗号后换行
        sql = LEADING_COMMA.matcher(sql).replaceAll("$2$1");
        // TAB替换为4个空格
        sql = StringTools.replace(sql, "\t", "    ");
        return sql;
    }

}
