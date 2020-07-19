package com.gitee.qdbp.jdbc.plugins.impl;

import com.gitee.qdbp.jdbc.plugins.SqlFormatter;
import com.gitee.qdbp.tools.utils.IndentTools;

/**
 * 简单的SQL格式化处理工具
 *
 * @author zhaohuihua
 * @version 190601
 */
public class SimpleSqlFormatter implements SqlFormatter {

    public String format(String sql) {
        return format(sql, 0);
    }

    public String format(String sql, int indent) {
        if (indent <= 0) {
            return sql;
        } else {
            return IndentTools.indentAll(sql, indent);
        }
    }
}
