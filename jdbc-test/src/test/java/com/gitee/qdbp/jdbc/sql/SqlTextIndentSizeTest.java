package com.gitee.qdbp.jdbc.sql;

import java.util.ArrayList;
import java.util.List;
import com.gitee.qdbp.jdbc.model.MainDbType;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer.StringItem;
import com.gitee.qdbp.jdbc.utils.DbTools;
import com.gitee.qdbp.tools.utils.StringTools;

/**
 * 缩进量测试类
 *
 * @author zhaohuihua
 * @version 20200719
 */
public class SqlTextIndentSizeTest {

    public static void main(String[] args) {
        test(false);
        test(true);
        System.out.println("test passed!");
    }

    private static void test(boolean inserBlank) {
        // 这里要找的是DEF之前的那个换行符之后的空白字符, 即缩进量为2
        testFindLastIndentSize(buildTestSql("\n\tABC\n\t\tDEF\t\t\t", inserBlank), 2);

        // 最后一个字符就是换行符, 即刚刚换行完, 要找的仍然是DEF之前的那个换行符
        testFindLastIndentSize(buildTestSql("\n\tABC\n\t\tDEF\n", inserBlank), 2);
        // 最后连续多个换行符, 要找的仍然是DEF之前的那个换行符
        testFindLastIndentSize(buildTestSql("\n\tABC\n\t\tDEF\n\n", inserBlank), 2);

        // 这里应返回ABC后面的换行符之后的缩进量2
        testFindLastIndentSize(buildTestSql("\n\tABC\n\t\t", inserBlank), 2);

        // 这里应返回首行的缩进量1
        testFindLastIndentSize(buildTestSql("\tABC", inserBlank), 1);
        testFindLastIndentSize(buildTestSql("\tABC\n", inserBlank), 1);
        testFindLastIndentSize(buildTestSql("\tABC\n\n", inserBlank), 1);

        // 没有缩进
        testFindLastIndentSize(buildTestSql("ABC", inserBlank), 0);
        testFindLastIndentSize(buildTestSql("ABC\n", inserBlank), 0);
        testFindLastIndentSize(buildTestSql("ABC\n\n", inserBlank), 0);
    }

    private static SqlBuffer buildTestSql(String string, boolean inserBlank) {
        // 按是否空白字符切换, 生成Item列表
        // 例如 \n\tABC\n\t\tDEF\t\t\t 生成 空白+字母+空白+字母+空白 5个Item
        List<StringItem> items = new ArrayList<>();
        items.add(new StringItem());
        boolean lastIsBlank = true;
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            boolean isBlank = StringTools.isAsciiWhitespace(c);
            if (lastIsBlank != isBlank) {
                if (inserBlank) { // 插入空的Item
                    items.add(new StringItem());
                }
                items.add(new StringItem());
            }
            items.get(items.size() - 1).getValue().append(c);
            lastIsBlank = isBlank;
        }
        SqlBuffer sql = new SqlBuffer();
        sql.items().addAll(items);
        return sql;
    }

    private static void testFindLastIndentSize(SqlBuffer sql, int expectedIndent) {
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
