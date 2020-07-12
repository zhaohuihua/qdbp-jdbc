package com.gitee.qdbp.jdbc.test.sql;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.LoggerFactory;
import com.gitee.qdbp.able.jdbc.paging.Paging;
import com.gitee.qdbp.jdbc.model.MainDbType;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.mapper.SqlParser;
import com.gitee.qdbp.jdbc.utils.DbTools;

public class SqlFormatTest {

    public static void main(String[] args) {
        LoggerFactory.getLogger(SqlFormatTest.class);
        System.out.println("------------------------------");
        test0();
        System.out.println("------------------------------");
        test1();
        System.out.println("------------------------------");
        test2();
        System.out.println("------------------------------");
        test3();
    }

    private static void test0() {
        SqlDialect dialect = DbTools.buildSqlDialect(MainDbType.Oracle);
        // @formatter:off
        String sqlTemplate = "SELECT * FROM ACCT_DEALPOSITION WHERE EFTFLAG IN ( #{eftflag} ) ";
        // @formatter:on
        Map<String, Object> params = new HashMap<>();
        params.put("eftflag", new SqlBuffer().addVariable('A').append(',').addVariable('E'));
        SqlBuffer buffer = new SqlParser(dialect).parse(sqlTemplate, params);
        SqlBuffer paging1 = dialect.buildPagingSql(buffer, new Paging(1, 10));
        SqlBuffer paging2 = dialect.buildPagingSql(buffer, new Paging(2, 10));
        System.out.println(buffer.getPreparedSqlString(dialect));
        System.out.println("------------------------------");
        System.out.println(DbTools.formatSql(paging1.getExecutableSqlString(dialect), 1));
        System.out.println("------------------------------");
        System.out.println(DbTools.formatSql(paging2.getExecutableSqlString(dialect), 1));
    }

    private static void test1() {
        // @formatter:off
        String sql = 
            "SELECT * FROM ( " +
            "    SELECT TT.*, ROWNUM RN " +
            "    FROM ( " +
            "        SELECT ACCT_DEALPOSITION.*, ROWID T_RID " +
            "        FROM ACCT_DEALPOSITION " +
            "        WHERE EFTFLAG IN ( :$1,:$2,:$3 ) " +
            "    ) TT " +
            "    WHERE ROWNUM <= 100 " +
            ") " +
            "WHERE RN > 0";
        // @formatter:on
        System.out.println(DbTools.formatSql(sql, 1));
    }

    private static void test2() {
        // @formatter:off
        String sql = 
            "SELECT * FROM ( " +
            "    SELECT TT.*, ROWNUM RN " +
            "    FROM ( " +
            "        SELECT ID,NICK_NAME,REAL_NAME, " +
            "            ( SELECT DEPT_NAME FROM SYS_DEPARTMENT D " +
            "               WHERE D.DEPT_CODE = A.DEPT_CODE ) AS DEPT_NAME, " +
            "            DECODE(ACCOUNT_STATE,null,1,ACCOUNT_STATE) AS ACCOUNT_STATE, " +
            "            DECODE(SUBSTR(RATE*100,1,1),'.','0'||RATE*100,RATE*100)||'%' AS RATE, " +
            "            DECODE((B.AMOUNT - NVL(A.AMOUNT,0)),0,'ALL',AMOUNT,'NO','PART') AS AMOUNT_STATE " +
            "        FROM SYS_ACCOUNT A " +
            "        INNER JOIN ACCT_BALANCE B ON A.ID=B.ACCOUND_ID " +
            "        WHERE CREATE_TIME >= TO_DATE('2019-01-01','yyyy-mm-dd') " +
            "        AND PHONE LIKE ('%'||'139'||'%') " +
            "        AND CITY_CODE IN (SELECT CITY_CODE FROM SYS_AREA WHERE AREA_TYPE='2') " +
            "        GROUP BY DEPT_CODE " +
            "    ) TT " +
            "    WHERE ROWNUM <= 100 " +
            ") " +
            "WHERE RN > 0";
        // @formatter:on
        System.out.println(DbTools.formatSql(sql, 1));
    }

    private static void test3() {
        // @formatter:off
        String sql = 
            "SELECT " +
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
