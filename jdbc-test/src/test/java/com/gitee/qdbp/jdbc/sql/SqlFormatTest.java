package com.gitee.qdbp.jdbc.sql;

import java.util.HashMap;
import java.util.Map;
import com.gitee.qdbp.able.jdbc.paging.Paging;
import com.gitee.qdbp.jdbc.utils.DbTools;
import com.gitee.qdbp.tools.utils.ConvertTools;

public class SqlFormatTest {

    public static void main(String[] args) {
        test11();
        System.out.println("------------------------------");
        test1();
        System.out.println("------------------------------");
        test2();
        System.out.println("------------------------------");
        test3();
    }

    private static void test1() {
        // @formatter:off
        String sql = "SELECT * FROM ( " + // 
                "    SELECT TT.*, ROWNUM RN  " + // 
                "    FROM (  " + // 
                "        SELECT ACCT_DEALPOSITION.*, ROWID T_RID  " + // 
                "        FROM ACCT_DEALPOSITION " + // 
                "        WHERE EFTFLAG IN ( :$1,:$2,:$3 ) " + // 
                "    ) TT  " + // 
                "    WHERE ROWNUM <= 100 " + // 
                ")  " + // 
                "WHERE RN > 0";
        // @formatter:on
        System.out.println(DbTools.formatSql(sql, 1));
    }

    private static void test11() {
        // @formatter:off
        String sqlTemplate = "SELECT ACCT_DEALPOSITION.*, ROWID T_RID  " + // 
                "    FROM ACCT_DEALPOSITION " + // 
                "    WHERE EFTFLAG IN ( #{eftflag} ) ";
        // @formatter:on
        Map<String, Object> params = new HashMap<>();
        params.put("eftflag", ConvertTools.toList('A', 'E'));
        SqlBuffer buffer = SqlBuffer.parse(sqlTemplate, params);
        DbTools.getSqlDialect().processPagingSql(buffer, new Paging(3, 10));
        System.out.println(DbTools.formatSql(buffer, 1));
    }

    private static void test2() {

        String sql = "SELECT * FROM (" + // 
                "    SELECT TT.*, ROWNUM RN " + // 
                "    FROM ( " + // 
                "        SELECT ID,NICK_NAME,REAL_NAME, " + // 
                "            ( SELECT DEPT_NAME FROM SYS_DEPARTMENT D" + // 
                "               WHERE D.DEPT_CODE = A.DEPT_CODE ) AS DEPT_NAME, " + // 
                "            DECODE(ACCOUNT_STATE,null,1,ACCOUNT_STATE) AS ACCOUNT_STATE, " + // 
                "            DECODE(SUBSTR(RATE*100,1,1),'.','0'||RATE*100,RATE*100)||'%' AS RATE, " + // 
                "            DECODE((B.AMOUNT - NVL(A.AMOUNT,0)),0,'ALL',AMOUNT,'NO','PART') AS AMOUNT_STATE " + // 
                "        FROM SYS_ACCOUNT A" + // 
                "        INNER JOIN ACCT_BALANCE B ON A.ID=B.ACCOUND_ID" + // 
                "        WHERE CREATE_TIME >= TO_DATE('2019-01-01','yyyy-mm-dd')" + // 
                "        AND PHONE LIKE ('%'||'139%'||'%')" + // 
                "        AND CITY_CODE IN (SELECT CITY_CODE FROM SYS_AREA WHERE AREA_TYPE='2')" + // 
                "        GROUP BY DEPT_CODE" + // 
                "    ) TT " + // 
                "    WHERE ROWNUM <= 100" + // 
                ") " + // 
                "WHERE RN > 0";
        System.out.println(DbTools.formatSql(sql, 1));
    }

    private static void test3() {
        // @formatter:off
        String sql = "SELECT " +
                "    IF ( " +
                "        LOCATE ( familyKey, link, 1 ) = 0, NULL, " +
                "        SUBSTRING ( " +
                "            link, LOCATE(familyKey, link, 1) + LENGTH(familyKey) + 1, " +
                "            IF ( " +
                "                LOCATE ( '&', link, LOCATE(familyKey, link, 1) ) = 0, LENGTH ( link ), " +
                "                LOCATE ( '&', link, LOCATE(familyKey, link, 1) ) " +
                "                - ( LOCATE(familyKey, link, 1) + LENGTH(familyKey) + 1 ) " +
                "            ) " +
                "        ) " +
                "    ) familyKey " +
                "FROM illustrations;";
        // @formatter:on
        System.out.println(DbTools.formatSql(sql, 1));
    }
}
