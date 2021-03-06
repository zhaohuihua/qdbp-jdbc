package com.gitee.qdbp.jdbc.plugins;

import java.util.Date;
import com.gitee.qdbp.able.jdbc.paging.Paging;
import com.gitee.qdbp.jdbc.model.DbVersion;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;

/**
 * 数据库方言处理接口
 *
 * @author zhaohuihua
 * @version 190606
 */
public interface SqlDialect {

    /** 获取数据库版本信息 **/
    DbVersion getDbVersion();

    /**
     * IN语句最多支持多少项<br>
     * oracle的IN语句最多只支持1000项, 其他数据库无限制
     * 
     * @return 数量限制, 0表示无限制
     */
    int getInItemLimit();

    /** 生成新的分页SQL对象, 原对象不受影响 **/
    SqlBuffer buildPagingSql(SqlBuffer buffer, Paging paging);

    /** 处理分页, 在原SQL前后追加分页语句 **/
    void processPagingSql(SqlBuffer buffer, Paging paging);

    /** 转换为按拼音排序的表达式 **/
    String toPinyinOrderByExpression(String columnName);

    /** 当前时间的数据库原生写法 **/
    String rawCurrentTimestamp();

    /**
     * Boolean类型的变量转换为字符串(用于拼接SQL)
     * 
     * @param variable 变量
     * @return 转换后的值, true=1, false=0
     */
    String variableToString(Boolean variable);

    /**
     * 字符串类型的变量转换为字符串(用于拼接SQL)<br>
     * 需要替换特殊字符, 并在两侧加单引号
     * 
     * @param variable 变量, 如: Let's go.
     * @return 转换后的值, 如: 'Let''s go.'
     */
    String variableToString(String variable);

    /**
     * 日期类型的变量转换为字符串(用于拼接SQL)<br>
     * 一般需要生成TO_TIMESTAMP SQL语句(MYSQL例外,支持字符串)<br>
     * 
     * @param date 指定日期
     * @return TO_TIMESTAMP SQL语句<br>
     *         ORACLE: TO_TIMESTAMP('2019-06-01 12:34:56.789', 'YYYY-MM-DD HH24:MI:SS.FF')<br>
     *         MYSQL: '2019-06-01 12:34:56.789'<br>
     */
    String variableToString(Date variable);

    /**
     * 生成LIKE SQL语句
     * 
     * @param fieldValue 字段值
     * @return LIKE SQL语句<br>
     *         ORACLE: LIKE ('%'|| ? ||'%')<br>
     *         MYSQL: LIKE CONCAT('%', ?, '%')<br>
     */
    SqlBuffer buildLikeSql(Object fieldValue);

    /**
     * 生成LIKE SQL语句
     * 
     * @param fieldValue 字段值
     * @return LIKE SQL语句<br>
     *         ORACLE: LIKE ( ? ||'%')<br>
     *         MYSQL: LIKE CONCAT( ?, '%')<br>
     */
    SqlBuffer buildStartsWithSql(Object fieldValue);

    /**
     * 生成LIKE SQL语句
     * 
     * @param fieldValue 字段值
     * @return LIKE SQL语句<br>
     *         ORACLE: LIKE ('%'|| ? )<br>
     *         MYSQL: LIKE CONCAT('%', ? )<br>
     */
    SqlBuffer buildEndsWithSql(Object fieldValue);

    /**
     * SqlDialect创建接口
     *
     * @author zhaohuihua
     * @version 20201018
     */
    interface Creator {

        /**
         * 根据数据库版本创建SQL方言处理对象
         * 
         * @param version 数据库版本
         * @return SQL方言处理对象
         */
        SqlDialect create(DbVersion version);
    }
}
