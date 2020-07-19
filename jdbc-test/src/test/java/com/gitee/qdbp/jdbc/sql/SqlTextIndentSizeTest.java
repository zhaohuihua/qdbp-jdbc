package com.gitee.qdbp.jdbc.sql;

import java.util.ArrayList;
import java.util.List;
import com.gitee.qdbp.jdbc.model.MainDbType;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer.StringItem;
import com.gitee.qdbp.jdbc.utils.DbTools;

/**
 * 缩进量测试类
 *
 * @author zhaohuihua
 * @version 20200719
 */
public class SqlTextIndentSizeTest {

    public static void main(String[] args) {
        test(null);
        test(false);
        test(true);
        System.out.println("test completed!");
    }

    private static void test(Boolean inserBlank) {
        // 这里要找的是DEF之前的那个换行符之后的空白字符, 即缩进量为3
        testFindLastIndentSize("\n\tABC\n\t\t\tDEF\t\t", inserBlank, 3);
        testFindLastIndentSize("\n\tABC\n\t\t    DEF\t\t", inserBlank, 3);
        testFindLastIndentSize("\n\tABC\n    \t\tDEF\t\t", inserBlank, 3);
        testFindLastIndentSize("\n\tABC\n\t    \tDEF\t\t", inserBlank, 3);

        // 最后一个字符就是换行符, 即刚刚换行完, 要找的仍然是DEF之前的那个换行符
        testFindLastIndentSize("\n\tABC\n\t\t\tDEF\n", inserBlank, 3);
        testFindLastIndentSize("\n\tABC\n\t\t    DEF\n", inserBlank, 3);
        testFindLastIndentSize("\n\tABC\n    \t\tDEF\n", inserBlank, 3);
        testFindLastIndentSize("\n\tABC\n\t    \tDEF\n", inserBlank, 3);
        // 最后连续多个换行符
        testFindLastIndentSize("\n\tABC\n\t\t\tDEF\n\n\n", inserBlank, 3);
        testFindLastIndentSize("\n\tABC\n\t\t    DEF\n\n\n", inserBlank, 3);
        testFindLastIndentSize("\n\tABC\n    \t\tDEF\n\n\n", inserBlank, 3);
        testFindLastIndentSize("\n\tABC\n\t    \tDEF\n\n\n", inserBlank, 3);

        // 这里应返回ABC后面的换行符之后的缩进量3
        testFindLastIndentSize("\n\tABC\n\t\t\t", inserBlank, 3);
        testFindLastIndentSize("\n\tABC\n\t    \t", inserBlank, 3);
        testFindLastIndentSize("\n\tABC\n    \t\t", inserBlank, 3);
        testFindLastIndentSize("\n\tABC\n\t\t    ", inserBlank, 3);

        // 这里应返回首行的缩进量1
        testFindLastIndentSize("\tABC", inserBlank, 1);
        testFindLastIndentSize("\tABC\n", inserBlank, 1);
        testFindLastIndentSize("\tABC\n\n", inserBlank, 1);
        testFindLastIndentSize("\t\t\tABC", inserBlank, 3);
        testFindLastIndentSize("\t    \tABC\n", inserBlank, 3);
        testFindLastIndentSize("\t\t    ABC\n\n", inserBlank, 3);
        testFindLastIndentSize("    \t\tABC\n\n", inserBlank, 3);

        // 没有缩进
        testFindLastIndentSize("ABC", inserBlank, 0);
        testFindLastIndentSize("ABC\t", inserBlank, 0);
        testFindLastIndentSize("ABC\n", inserBlank, 0);
        testFindLastIndentSize("ABC\t\n", inserBlank, 0);
        testFindLastIndentSize("ABC\n\n", inserBlank, 0);
        testFindLastIndentSize("ABC\t\n\n", inserBlank, 0);
    }

    private static SqlBuffer buildTestSql(String string, Boolean inserBlank) {
        if (inserBlank == null) {
            return new SqlBuffer(string);
        }
        // 按是否转义字符切换, 生成Item列表
        // 空格不属于转义字符
        // 例如 \n\tABC\n\t\tDEF\t\t\t 生成 空白+字母+空白+字母+空白 5个Item
        List<StringItem> items = new ArrayList<>();
        items.add(new StringItem());
        boolean lastIsEscape = true;
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            boolean isEscape = c == '\t' || c == '\r' || c == '\n';
            if (lastIsEscape != isEscape) {
                if (inserBlank) { // 插入空的Item
                    items.add(new StringItem());
                }
                items.add(new StringItem());
            }
            items.get(items.size() - 1).getValue().append(c);
            lastIsEscape = isEscape;
        }
        SqlBuffer sql = new SqlBuffer();
        sql.items().addAll(items);
        return sql;
    }

    private static void testFindLastIndentSize(String input, Boolean inserBlank, int expectedIndent) {
        SqlBuffer sql = buildTestSql(input, inserBlank);
        SqlDialect dialect = DbTools.buildSqlDialect(MainDbType.MySQL);
        int actualIndent = SqlTextTools.findLastIndentSize(sql);
        if (actualIndent != expectedIndent) {
            String sqlString = getSqlString(sql, dialect);
            System.out.printf("Error: expected=%s, expected=%s, sql=%s%n", expectedIndent, actualIndent, sqlString);
        }
    }

    private static String getSqlString(SqlBuffer sql, SqlDialect dialect) {
        String string = sql.getLoggingSqlString(dialect);
        return string.replace(" ", "\\s").replace("\t", "\\t").replace("\r", "\\r").replace("\n", "\\n");
    }
}
