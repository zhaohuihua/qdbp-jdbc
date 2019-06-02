package com.gitee.qdbp.jdbc.sql;

import java.util.Collection;
import com.gitee.qdbp.jdbc.exception.UnsupportedFieldExeption;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * SQL工具类
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
        return buildInSql(null, fieldValues, true);
    }

    /**
     * 生成IN语句<br>
     * 如果fieldValues只有一项, 输出={fieldValue}<br>
     * 否则输出 IN ( :fieldValue1, :fieldValue2, ... )<br>
     * 
     * @param fieldName 字段名称
     * @param fieldValues 字段值
     */
    public static SqlBuffer buildInSql(String fieldName, Collection<?> fieldValues) {
        return buildInSql(fieldName, fieldValues, true);
    }

    /**
     * 生成NOT IN语句<br>
     * 如果fieldValues只有一项, 输出={fieldValue}<br>
     * 否则输出 IN ( :fieldValue1, :fieldValue2, ... )<br>
     * 
     * @param fieldValues 字段值
     */
    public static SqlBuffer buildNotInSql(Collection<?> fieldValues) {
        return buildInSql(null, fieldValues, false);
    }

    /**
     * 生成NOT IN语句<br>
     * 如果fieldValues只有一项, 输出={fieldValue}<br>
     * 否则输出 IN ( :fieldValue1, :fieldValue2, ... )<br>
     * 
     * @param fieldName 字段名称
     * @param fieldValues 字段值
     */
    public static SqlBuffer buildNotInSql(String fieldName, Collection<?> fieldValues) {
        return buildInSql(fieldName, fieldValues, false);
    }

    /**
     * 生成IN语句<br>
     * 如果fieldValues只有一项, 输出={fieldValue}<br>
     * 否则输出 IN ( :fieldValue1, :fieldValue2, ... )<br>
     * 
     * @param fieldName 字段名称
     * @param fieldValues 字段值
     * @param matches true=in, false=not in
     */
    private static SqlBuffer buildInSql(String fieldName, Collection<?> fieldValues, boolean matches)
            throws UnsupportedFieldExeption {
        SqlBuffer buffer = new SqlBuffer();
        if (VerifyTools.isBlank(fieldValues)) {
            return buffer;
        }

        if (fieldValues.size() == 1) {
            String operate = matches ? "=" : " != ";
            Object firstValue = null;
            for (Object value : fieldValues) {
                firstValue = value;
                break;
            }
            buffer.append(operate).addVariable(fieldName, firstValue);
        } else {
            String operate = matches ? "IN" : "NOT IN";
            buffer.append(' ', operate, ' ').append('(');
            boolean first = true;
            for (Object value : fieldValues) {
                if (!first) {
                    buffer.append(',');
                }
                first = false;
                buffer.addVariable(fieldName, value);
            }
            buffer.append(')');
        }
        return buffer;
    }
}
