package com.gitee.qdbp.jdbc.sql.mapper;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.gitee.qdbp.jdbc.model.DbVersion;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.utils.DbTools;
import com.gitee.qdbp.tools.utils.ReflectTools;

/**
 * SQL解析, 从SQL模板解析获取SqlBuffer对象
 *
 * @author zhaohuihua
 * @version 190804
 */
public class SqlParser {

    private static final Pattern PLACEHOLDER = Pattern.compile("([\\$#])\\{([\\.\\w\\[\\]\\$]+)\\}");

    private SqlDialect dialect;

    public SqlParser(DbVersion version) {
        this.dialect = DbTools.buildSqlDialect(version);
    }

    public SqlParser(SqlDialect dialect) {
        this.dialect = dialect;
    }

    /**
     * 从SQL模板解析获取SqlBuffer对象, 替换占位符<br>
     * #{fieldName}为预编译参数, ${fieldName}为拼写式参数<br>
     * 占位符变量可以是另一个SqlBuffer片段<br>
     * 
     * @param sqlTemplate SQL模板
     * @param params 占位符变量
     * @return SqlBuffer对象
     */
    public SqlBuffer parse(String sqlTemplate, Map<String, Object> params) {
        SqlBuffer buffer = new SqlBuffer();
        Matcher matcher = PLACEHOLDER.matcher(sqlTemplate);
        int index = 0;
        while (matcher.find()) {
            if (index < matcher.start()) {
                buffer.append(sqlTemplate.substring(index, matcher.start()));
            }
            String prefix = matcher.group(1);
            String placeholder = matcher.group(2);
            Object value = ReflectTools.getDepthValue(params, placeholder);
            if ("$".equals(prefix)) { // 拼写式参数
                if (value instanceof SqlBuffer) {
                    buffer.append(((SqlBuffer) value).getExecutableSqlString(dialect));
                } else {
                    buffer.append(DbTools.variableToString(value, dialect));
                }
            } else { // 预编译参数
                if (value instanceof SqlBuffer) {
                    buffer.append((SqlBuffer) value);
                } else {
                    buffer.addVariable(value);
                }
            }
            index = matcher.end();
        }
        if (index < sqlTemplate.length()) {
            buffer.append(sqlTemplate.substring(index));
        }
        return buffer;
    }
}
