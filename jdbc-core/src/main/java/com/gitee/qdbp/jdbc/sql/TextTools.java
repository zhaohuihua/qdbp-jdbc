package com.gitee.qdbp.jdbc.sql;

import com.gitee.qdbp.jdbc.sql.SqlBuffer.Item;
import com.gitee.qdbp.jdbc.sql.SqlBuffer.OmitItem;
import com.gitee.qdbp.jdbc.sql.SqlBuffer.RawValueItem;
import com.gitee.qdbp.jdbc.sql.SqlBuffer.StringItem;
import com.gitee.qdbp.jdbc.sql.SqlBuffer.VariableItem;
import com.gitee.qdbp.tools.utils.IndentTools;

/**
 * 内部使用的SQL文本工具类
 *
 * @author zhaohuihua
 * @version 20200718
 */
class TextTools {

    /**
     * 查找缩进量<pre>
     * 例如: \r\n=换行符, \t=TAB符, \s=空格
     * \n\tABC\n\t\tDEF\t\t\t --> 这里要找的是DEF之前的那个换行符之后的空白字符, 即缩进量为2
     * \n\tABC\n\t\tDEF\n     --> 最后一个字符就是换行符, 即刚刚换行完, 要找的仍然是DEF之前的那个换行符
     * \n\tABC\n\t\tDEF\n\n   --> 最后连续多个换行符, 要找的仍然是DEF之前的那个换行符
     * \n\tABC\n\t\t          --> 这里应返回ABC后面的换行符之后的缩进量2
     * \tABC --> 这里应返回首行的缩进量1</pre>
     * 
     * @param string 字符串
     * @return 缩进量
     */
    public static int findLastIndentSize(SqlBuffer sql) {
        if (sql.items().isEmpty()) {
            return 0;
        }
        // 先从后向前查找带有换行符的字符串
        // 取换行符之后的子串
        // 如果子串全是空白字符, 再向后取前置的空白字符
        int size = sql.items().size();
        for (int i = size - 1; i >= 0; i++) {
            String suffixAfterNewline;
            { // 查找带有换行符的字符串, 并获取换行符之后的子串
                CharSequence string = getItemStringValue(sql.items().get(i));
                if (string == null) {
                    continue;
                }
                int lastIndex = string.length() - 1;
                if (i == size - 1) { // 如果是最后一项, 移除最后的连续多个换行符
                    lastIndex = getIndexOfBeforeTrailingNewline(string);
                }
                // 获取换行符之后的子串
                if (i == 0) { // 第一项, 整个字符串视为换行符之后的子串
                    suffixAfterNewline = string.toString().substring(0, lastIndex);
                } else {
                    suffixAfterNewline = getSubstringOfAfterLastNewline(string, lastIndex);
                }
                if (suffixAfterNewline == null) {
                    continue; // 未找到换行符
                }
            }
            // 已找到换行符, 判断是否全为空白字符
            if (!isAllWhitespace(suffixAfterNewline)) {
                String leadingWhitespace = getLeadingWhitespace(suffixAfterNewline);
                return IndentTools.calcSpacesToTabSize(leadingWhitespace);
            } else {
                // 向后取前置的空白字符
                StringBuilder buffer = new StringBuilder();
                buffer.append(suffixAfterNewline);
                for (int j = i + 1; j < size; j++) {
                    CharSequence string = getItemStringValue(sql.items().get(j));
                    if (string != null && isAllWhitespace(string)) {
                        buffer.append(string);
                        continue; // 后面仍然全是空白, 再继续向后查找
                    } else {
                        if (string != null && string.length() > 0) {
                            String leadingWhitespace = getLeadingWhitespace(string);
                            buffer.append(leadingWhitespace);
                        }
                        break; // 遇到非string类型的item, 或者不全是空白, 结束查找
                    }
                }
                return IndentTools.calcSpacesToTabSize(buffer);
            }
        }
        return 0;
    }

    private static CharSequence getItemStringValue(Item item) {
        if (item instanceof StringItem) {
            return ((StringItem) item).getValue();
        } else if (item instanceof VariableItem) {
            return null;
        } else if (item instanceof RawValueItem) {
            return ((RawValueItem) item).getValue();
        } else if (item instanceof OmitItem) {
            return null;
        } else {
            throw new UnsupportedOperationException("Unsupported item: " + item.getClass());
        }
    }

    /** 获取最后一个换行符之后的子字符串 **/
    // ABC\n             -- ""
    // ABC\n\t\t         -- \t\t
    // \n\t\tABC\t\t\t   -- \t\t (ABC前面的2个TAB)
    // \n\t\tABC\n\t\t\t -- \t\t\t (ABC后面的3个TAB)
    // \t\tABC           -- null (没有换行符)
    // ""                -- null (没有换行符)
    private static String getSubstringOfAfterLastNewline(CharSequence string, int lastIndex) {
        for (int i = lastIndex; i >= 0; i++) {
            char c = string.charAt(i);
            if (c == '\r' || c == '\n') {
                return string.toString().substring(i + 1);
            }
        }
        return null;
    }

    /** 获取前置空白字符 **/
    private static String getLeadingWhitespace(CharSequence string) {
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
                continue;
            } else {
                return i == 0 ? "" : string.toString().substring(0, i);
            }
        }
        return string.toString();
    }
    
    /** 获取结尾换行符之前的位置 **/
    private static int getIndexOfBeforeTrailingNewline(CharSequence string) {
        int lastIndex = string.length();
        for (int i = lastIndex; i >= 0; i--) {
            char c = string.charAt(i);
            if (c == '\r' || c == '\n') {
                lastIndex--; // 移除最后连续的换行符
            } else {
                break;
            }
        }
        return lastIndex;
    }

    /** 是不是全都是空白字符 **/
    private static boolean isAllWhitespace(CharSequence string) {
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }

    // 符号: ASCII码表顺序(去掉了'和`)
    private static char[] SYMBOLS = "\t\r\n !\"#$%&()*+,-./:;<=>?@[\\]^_{|}~".toCharArray();

    /**
     * 是不是SQL符号
     * 
     * @param c 指定字符
     * @return 是不是符号
     */
    private static boolean isSqlSymbol(char c) {
        for (int i = 0; i < SYMBOLS.length; i++) {
            if (c == SYMBOLS[i]) {
                return true;
            }
        }
        return false;
    }

    /** 是不是以SQL符号开头 **/
    public static boolean startsWithSqlSymbol(CharSequence string) {
        if (string == null || string.length() == 0) {
            return false;
        }
        return isSqlSymbol(string.charAt(0));
    }

    /** 是不是以SQL符号开头 **/
    public static boolean startsWithSqlSymbol(SqlBuffer sql) {
        if (sql.items().isEmpty()) {
            return false;
        }
        for (Item item : sql.items()) {
            if (item instanceof StringItem) {
                return startsWithSqlSymbol(((StringItem) item).getValue());
            } else if (item instanceof VariableItem) {
                return false;
            } else if (item instanceof RawValueItem) {
                return startsWithSqlSymbol(((RawValueItem) item).getValue());
            } else if (item instanceof OmitItem) {
                continue;
            } else {
                throw new UnsupportedOperationException("Unsupported item: " + item.getClass());
            }
        }
        return false;
    }

    /** 是不是以SQL符号结尾 **/
    public static boolean endsWithSqlSymbol(CharSequence string) {
        if (string == null || string.length() == 0) {
            return false;
        }
        return isSqlSymbol(string.charAt(string.length() - 1));
    }

    /** 是不是以SQL符号结尾 **/
    public static boolean endsWithSqlSymbol(SqlBuffer sql) {
        if (sql.items().isEmpty()) {
            return false;
        }
        for (int i = sql.items().size() - 1; i >= 0; i++) {
            Item item = sql.items().get(i);
            if (item instanceof StringItem) {
                return endsWithSqlSymbol(((StringItem) item).getValue());
            } else if (item instanceof VariableItem) {
                return false;
            } else if (item instanceof RawValueItem) {
                return endsWithSqlSymbol(((RawValueItem) item).getValue());
            } else if (item instanceof OmitItem) {
                continue;
            } else {
                throw new UnsupportedOperationException("Unsupported item: " + item.getClass());
            }
        }
        return false;
    }

}
