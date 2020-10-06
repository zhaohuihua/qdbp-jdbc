package com.gitee.qdbp.jdbc.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.gitee.qdbp.jdbc.exception.UnsupportedFieldException;
import com.gitee.qdbp.jdbc.model.OmitStrategy;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.utils.DbTools;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 与数据库方言无关的SQL静态工具类
 *
 * @author zhaohuihua
 * @version 190601
 */
public abstract class SqlTools {

    /**
     * 生成IN语句<br>
     * 如果fieldValues只有一项, 输出COLUMN_NAME={fieldValue}<br>
     * 否则输出 COLUMN_NAME IN ( :fieldValue1, :fieldValue2, ... )<br>
     * 
     * @param columnName 列名
     * @param fieldValues 字段值
     * @param dialect 数据库方言
     */
    public static SqlBuffer buildInSql(String columnName, Collection<?> fieldValues, SqlDialect dialect) {
        return buildInSql(columnName, fieldValues, true, dialect);
    }

    /**
     * 生成NOT IN语句<br>
     * 如果fieldValues只有一项, 输出COLUMN_NAME!={fieldValue}<br>
     * 否则输出 COLUMN_NAME NOT IN ( :fieldValue1, :fieldValue2, ... )<br>
     * 
     * @param columnName 列名
     * @param fieldValues 字段值
     * @param dialect 数据库方言
     */
    public static SqlBuffer buildNotInSql(String columnName, Collection<?> fieldValues, SqlDialect dialect) {
        return buildInSql(columnName, fieldValues, false, dialect);
    }

    /**
     * 生成IN语句<br>
     * 如果fieldValues只有一项, 输出COLUMN_NAME={fieldValue}<br>
     * 否则输出 COLUMN_NAME IN ( :fieldValue1, :fieldValue2, ... )<br>
     * 
     * @param columnName 列名
     * @param fieldValues 字段值
     * @param matches true=in, false=not in
     */
    private static SqlBuffer buildInSql(String columnName, Collection<?> fieldValues, boolean matches,
            SqlDialect dialect) throws UnsupportedFieldException {
        SqlBuilder sql = new SqlBuilder();
        if (VerifyTools.isBlank(fieldValues)) {
            return sql.out();
        }

        if (fieldValues.size() == 1) {
            String operate = matches ? "=" : "!=";
            Object firstValue = null;
            for (Object value : fieldValues) {
                firstValue = value;
                break;
            }
            sql.ad(columnName).ad(operate).var(firstValue);
        } else {
            // 获取IN语句的省略策略配置项
            OmitStrategy omits = DbTools.getOmitSizeConfig("qdbc.in.sql.omit.strategy", "50:5");

            List<?> values = duplicateRemoval(fieldValues);
            String operate = matches ? "IN" : "NOT IN";
            int inItemLimit = dialect.getInItemLimit();
            if (inItemLimit <= 0 || values.size() <= inItemLimit) {
                sql.ad(columnName).ad(operate).ad('(');
                for (int i = 0, count = values.size(); i < count; i++) {
                    Object value = values.get(i);
                    if (i > 0) {
                        sql.ad(',');
                    }
                    if (omits.getMinSize() > 0 && count > omits.getMinSize()) {
                        sql.omit(i, count, omits.getKeepSize());
                    }
                    sql.var(value);
                }
                sql.ad(')');
            } else {
                List<List<?>> groups = splitList(values, inItemLimit);
                // oracle的IN语句最多只支持1000项
                // 如果matches=true,  生成 ( COLUMN_NAME IN (...) OR COLUMN_NAME IN (...) )
                // 如果matches=false, 生成 ( COLUMN_NAME NOT IN (...) AND COLUMN_NAME NOT IN (...) )
                String logic = matches ? "OR" : "AND";
                sql.ad('(');
                for (int g = 0; g < groups.size(); g++) {
                    if (g > 0) {
                        sql.ad(logic);
                    }
                    sql.ad(columnName).ad(operate).ad('(');
                    List<?> list = groups.get(g);
                    for (int i = 0, count = list.size(); i < count; i++) {
                        Object value = list.get(i);
                        if (i > 0) {
                            sql.ad(',');
                        }
                        if (omits.getMinSize() > 0 && count > omits.getMinSize()) {
                            sql.omit(i, count, omits.getKeepSize());
                        }
                        sql.var(value);
                    }
                    sql.ad(')');
                }
                sql.ad(')');
            }
        }
        return sql.out();
    }

    private static List<List<?>> splitList(List<?> values, int limit) {
        List<List<?>> groups = new ArrayList<>();
        List<Object> last = new ArrayList<>();
        groups.add(last);
        for (Object value : values) {
            if (last.size() >= limit) {
                last = new ArrayList<>();
                groups.add(last);
            }
            last.add(value);
        }
        return groups;
    }

    static List<?> duplicateRemoval(Collection<?> items) {
        Map<Object, ?> map = new HashMap<>();
        List<Object> list = new ArrayList<>();
        for (Object item : items) {
            if (!map.containsKey(item)) {
                map.put(item, null);
                list.add(item);
            }
        }
        return list;
    }
}
