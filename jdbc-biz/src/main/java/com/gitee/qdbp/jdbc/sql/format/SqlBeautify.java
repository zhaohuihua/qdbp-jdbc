package com.gitee.qdbp.jdbc.sql.format;

/**
 * <pre>
单行语句: 不算缩进和换行符的字符个数, 加上父级缩进及前缀, 不超过一行宽度

CASE WHEN单行语句写法:
    CASE WHEN SMS_SEND_RESULT IN (0, 3) THEN 1 ELSE 0 END
CASE WHEN多行语句写法:
    CASE WHEN (
        SELECT COUNT(DISTINCT prod_state) FROM table_prod_publish WHERE category IN ( 2, 3, 5 )
    ) > 1
    THEN sale_price
    ELSE prod_price
    END AS prod_price

子语句处理逻辑: 遇到开始的括号就作为子语句处理
1. 子语句是单个字符: 看作一个整体, 无需空格, 如 COUNT(*)
2. 子语句是单词,或多个单词: 括号前空格, 内容前后空格, 如 SUM ( field_name ), COUNT( DISTINCT prod_state )
3. 子语句是单行语句: 括号前空格, 内容前后空格, 如 AND id IN ( SELECT id FROM table_name WHERE dept_code = '1001' )
4. 子语句是复杂语句: 从子语句的前缀开始换行, 内容换行缩进, 如下语句从AND id IN开始换行
    WHERE active_state = 1
        AND id IN (
            SELECT id FROM table_name WHERE dept_code = '1001' AND active_state = 1
        )

并列条件: 函数参数, FROM字段, WHERE条件, ORDER BY条件, UPDATE SET条件, 等等
这些并列条件, 尽量写在一行
如果一行写不下, 就换行加缩进, 如activation_user_state
紧跟在复杂子语句后面的并列条件需要换行, 如star_level不能跟在arrive_prod_cnt后面
并列条件需要整体换行, 如arrive_user_cnt在star_level后面写不下, 需要整体换行,
SELECT channel_name, sxsp.sms_product_price prod_price, sxtd.pro_name province_name, 
    activation_user_state, renew_user_state,
    SUM (
        CASE WHEN sxsp.sms_prod_result IN (200, 201, 996, 997, 998, 999) THEN 1 ELSE 0 END
    ) arrive_prod_cnt,
    sxrsq.rights_star star_level, 
    SUM ( CASE WHEN sxsp.sms_send_result IN (0, 3) THEN 1 ELSE 0 END ) arrive_user_cnt,
    activation_user_cnt, renew_user_cnt
FROM source_xinyuan_sms_presend sxsp

 * </pre>
 * @author zhaohuihua
 * @version 190601
 */
public class SqlBeautify {
//
//    /**
//     * End of input.<br>
//     * 0x0A LF 表示换行<br>
//     * 0x0D CR 表示回车<br>
//     * 0x1A SUB 在文本文件中表示文件结束
//     */
//    public static final char LF = '\n';
//    public static final char CR = '\r';
//    public static final char EOI = 0x1A;
//
//    public static enum WordCase {
//        NONE, UPPERCASE, LOWERCASE
//    }
//
//    public static class Options {
//
//        private WordCase keywordCase;
//        private WordCase functionCase;
//        private WordCase identifierCase;
//    }
//
//    public class Token {
//
//        private final TokenType type;
//        private final String literals;
//        private final int endPosition;
//
//        public Token(TokenType type, String literals, int endPosition) {
//            this.type = type;
//            this.literals = literals;
//            this.endPosition = endPosition;
//        }
//
//    }
//
//    private static enum PartType {
//        TABLE, SELECT_FIELD, JOIN_CONDITION, WHERE_CONDITION, ORDER_BY_FIELD
//    }
//
//    private static enum TokenType {
//        // 空格, 换行, 括号, 成对符号, 注释, 关键字, 终止符(多个SQL), 文档结束符, 其他字符
//        WHITESPACE, NEWLINE, BRACKET_START, BRACKET_END, RANGE_START, RANGE_END, COMMENT_START, COMMENT_END, KEYWORD,
//        ENDED, EOI, OTHER
//    }
//
//    public static void main(String[] args) {
//        String sql = "SELECT * FROM (" + // 
//                "    SELECT TT.*, ROWNUM RN " + // 
//                "    FROM ( " + // 
//                "        SELECT ID,NICK_NAME,REAL_NAME, " + // 
//                "            ( SELECT DEPT_NAME FROM SYS_DEPARTMENT D" + // 
//                "               WHERE D.DEPT_CODE = A.DEPT_CODE ) AS DEPT_NAME, " + // 
//                "            DECODE(ACCOUNT_STATE,null,1,ACCOUNT_STATE) AS ACCOUNT_STATE, " + // 
//                "            DECODE(SUBSTR(RATE*100,1,1),'.','0'||RATE*100,RATE*100)||'%' AS RATE, " + // 
//                "            DECODE((B.AMOUNT - NVL(A.AMOUNT,0)),0,'ALL',AMOUNT,'NO','PART') AS AMOUNT_STATE " + // 
//                "        FROM SYS_ACCOUNT A" + // 
//                "        INNER JOIN ACCT_BALANCE B ON A.ID=B.ACCOUND_ID" + // 
//                "        WHERE CREATE_TIME >= TO_DATE('2019-01-01','yyyy-mm-dd')" + // 
//                "        AND PHONE LIKE ('%'||'139%'||'%')" + // 
//                "        AND CITY_CODE IN (SELECT CITY_CODE FROM SYS_AREA WHERE AREA_TYPE='2')" + // 
//                "        GROUP BY DEPT_CODE" + // 
//                "    ) TT " + // 
//                "    WHERE ROWNUM <= 100" + // 
//                ") " + // 
//                "WHERE RN > 0";
//
//        System.out.println(sql);
//
//        // 空格, 换行, 括号, 成对符号, 注释, 关键字, 终止符, 其他字符
//        StringBuilder temp = new StringBuilder();
//        boolean isEmpty = true;
//        StringBuilder lastPart = new StringBuilder();
//        TokenType lastType = null;
//        String expectType = null;
//        int indent = 0;
//        for (int i = 0, len = sql.length(); i < len;) {
//            lastPart.setLength(0);
//            for (int j = i; j < len; j++) {
//                if (lastType == null) {
//
//                }
//            }
//            if (isEmpty) {
//            }
//        }
//    }
//
//    private final String input;
//    private int offset;
//
//    public SqlBeautify(String sql) {
//        this.input = sql;
//    }
//
//    private Token scanWhitespace() {
//        int length = 0;
//        while (isWhitespaceChar(charAt(offset + length))) {
//            length++;
//        }
//        return new Token(TokenType.WHITESPACE, " ", offset + length);
//    }
//
//    private Token scanNewline() {
//        int length = 0;
//        while (isNewlineChar(charAt(offset + length))) {
//            length++;
//        }
//        return new Token(TokenType.NEWLINE, "\n", offset + length);
//    }
//
//    private boolean isEnd() {
//        return offset >= input.length();
//    }
//
//    private static final boolean isNewlineChar(final char c) {
//        return c == LF || c == CR;
//    }
//
//    private static final boolean isWhitespaceChar(final char c) {
//        return c <= 0x20 && c != EOI && !isNewlineChar(c) || c >= 0x7F && c <= 0xA0;
//    }
//
//    private final char charAt(int offset) {
//        return this.offset + offset >= input.length() ? (char) EOI : input.charAt(this.offset + offset);
//    }
}