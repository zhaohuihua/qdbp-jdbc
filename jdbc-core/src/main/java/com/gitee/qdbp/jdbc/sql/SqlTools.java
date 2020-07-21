package com.gitee.qdbp.jdbc.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.gitee.qdbp.jdbc.exception.UnsupportedFieldException;
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
     * 如果fieldValues只有一项, 输出={fieldValue}<br>
     * 否则输出 IN ( :fieldValue1, :fieldValue2, ... )<br>
     * 
     * @param fieldValues 字段值
     */
    public static SqlBuffer buildInSql(Collection<?> fieldValues) {
        return buildInSql(fieldValues, true);
    }

    /**
     * 生成NOT IN语句<br>
     * 如果fieldValues只有一项, 输出!={fieldValue}<br>
     * 否则输出 NOT IN ( :fieldValue1, :fieldValue2, ... )<br>
     * 
     * @param fieldValues 字段值
     */
    public static SqlBuffer buildNotInSql(Collection<?> fieldValues) {
        return buildInSql(fieldValues, false);
    }

    /**
     * 生成IN语句<br>
     * 如果fieldValues只有一项, 输出={fieldValue}<br>
     * 否则输出 IN ( :fieldValue1, :fieldValue2, ... )<br>
     * 
     * @param fieldValues 字段值
     * @param matches true=in, false=not in
     */
    private static SqlBuffer buildInSql(Collection<?> fieldValues, boolean matches) throws UnsupportedFieldException {
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
            sql.ad(operate).var(firstValue);
        } else {
            List<?> values = duplicateRemoval(fieldValues);
            String operate = matches ? "IN" : "NOT IN";
            sql.ad(operate).ad('(');
            for (int i = 0, count = values.size(); i < count; i++) {
                Object value = values.get(i);
                if (i > 0) {
                    sql.ad(',');
                }
                if (count > 40) {
                    sql.omit(i, count, 10);
                }
                sql.var(value);
            }
            sql.ad(')');
        }
        return sql.out();
    }

    private static List<?> duplicateRemoval(Collection<?> items) {
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
