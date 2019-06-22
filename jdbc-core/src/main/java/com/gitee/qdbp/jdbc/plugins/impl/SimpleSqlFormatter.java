package com.gitee.qdbp.jdbc.plugins.impl;

import java.util.regex.Pattern;
import com.gitee.qdbp.jdbc.plugins.SqlFormatter;

// 使用DruidSqlFormat格式化SQL
// https://github.com/alibaba/druid/wiki/SQL_Format
public class SimpleSqlFormatter implements SqlFormatter {

    public String format(String sql) {
        return format(sql, 0);
    }

    public String format(String sql, int indent) {
        String formatted = formatSql(sql);
        String pretty = prettySql(formatted);
        if (indent <= 0) {
            return pretty;
        } else {
            String tab = indent == 1 ? LEADING_SPACE : getIndentSpace(indent);
            return tab + NEWLINE.matcher(pretty).replaceAll("\n" + tab);
        }
    }

    protected String getIndentSpace(int indent) {
        StringBuilder buffer = new StringBuilder(indent);
        for (int i = 0; i < indent; i++) {
            buffer.append(LEADING_SPACE);
        }
        return buffer.toString();
    }

    private static Pattern LEADING_COMMA = Pattern.compile("(\\r?\\n\\s*)(,)(\\s*)");
    private static Pattern TAB = Pattern.compile("\\t");
    private static Pattern NEWLINE = Pattern.compile("\\n");
    private static String LEADING_SPACE = "    ";

    protected String formatSql(String sql) {
        return sql;
    }

    protected String prettySql(String sql) {
        // druid sql format的换行风格是换行后加前置逗号, 改为逗号后换行
        sql = LEADING_COMMA.matcher(sql).replaceAll("$2$1");
        // TAB替换为4个空格
        sql = TAB.matcher(sql).replaceAll(LEADING_SPACE);
        return sql;
    }

}
