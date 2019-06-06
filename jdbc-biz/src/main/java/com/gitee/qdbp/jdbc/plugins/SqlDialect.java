package com.gitee.qdbp.jdbc.plugins;

import java.util.Collection;
import java.util.List;
import com.gitee.qdbp.able.model.ordering.Ordering;
import com.gitee.qdbp.able.model.paging.Paging;
import com.gitee.qdbp.jdbc.condition.DbWhere;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.fragment.QueryFragmentBuilder;

/**
 * 数据库方言处理接口
 *
 * @author zhaohuihua
 * @version 190606
 */
public interface SqlDialect {

    /** 处理分页 **/
    void processPagingSql(SqlBuffer buffer, Paging paging);

    String toPinyinOrderByExpression(String columnName);

    /**
     * 生成LIKE SQL语句
     * 
     * @param fieldValue 字段值
     * @return LIKE SQL语句(<b>不带</b>字段名前缀)<br>
     *         ORACLE: LIKE ('%'|| ? ||'%')<br>
     *         MYSQL: LIKE CONCAT('%', ?, '%')<br>
     */
    SqlBuffer buildLikeSql(Object fieldValue);

    /**
     * 生成LIKE SQL语句
     * 
     * @param fieldName 字段名, 这个字段名只是用来标记变量的, 并不一定是数据库的列名, 生成的SQL也不带字段名前缀
     * @param fieldValue 字段值
     * @return LIKE SQL语句(<b>不带</b>字段名前缀)<br>
     *         ORACLE: LIKE ('%'|| ? ||'%')<br>
     *         MYSQL: LIKE CONCAT('%', ?, '%')<br>
     */
    SqlBuffer buildLikeSql(String fieldName, Object fieldValue);

    /**
     * 生成LIKE SQL语句
     * 
     * @param fieldValue 字段值
     * @return LIKE SQL语句(<b>不带</b>字段名前缀)<br>
     *         ORACLE: LIKE ( ? ||'%')<br>
     *         MYSQL: LIKE CONCAT( ?, '%')<br>
     */
    SqlBuffer buildStartsWithSql(Object fieldValue);

    /**
     * 生成LIKE SQL语句
     * 
     * @param fieldName 字段名, 这个字段名只是用来标记变量的, 并不一定是数据库的列名, 生成的SQL也不带字段名前缀
     * @param fieldValue 字段值
     * @return LIKE SQL语句(<b>不带</b>字段名前缀)<br>
     *         ORACLE: LIKE ( ? ||'%')<br>
     *         MYSQL: LIKE CONCAT( ?, '%')<br>
     */
    SqlBuffer buildStartsWithSql(String fieldName, Object fieldValue);

    /**
     * 生成LIKE SQL语句
     * 
     * @param fieldValue 字段值
     * @return LIKE SQL语句(<b>不带</b>字段名前缀)<br>
     *         ORACLE: LIKE ('%'|| ? )<br>
     *         MYSQL: LIKE CONCAT('%', ? )<br>
     */
    SqlBuffer buildEndsWithSql(Object fieldValue);

    /**
     * 生成LIKE SQL语句
     * 
     * @param fieldName 字段名, 这个字段名只是用来标记变量的, 并不一定是数据库的列名, 生成的SQL也不带字段名前缀
     * @param fieldValue 字段值
     * @return LIKE SQL语句(<b>不带</b>字段名前缀)<br>
     *         ORACLE: LIKE ('%'|| ? )<br>
     *         MYSQL: LIKE CONCAT('%', ? )<br>
     */
    SqlBuffer buildEndsWithSql(String fieldName, Object fieldValue);

    /**
     * 递归查询所有子节点<br>
     * ORACLE： START WITH {codeField} IN( {startCode} ) CONNECT BY PRIOR {codeField} = {parentField}<br>
     * DB2/SqlServer: 使用WITH递归<br>
     * MYSQL 8.0+/PostgreSQL: 使用WITH RECURSIVE递归<br>
     * MYSQL 8.0-: 使用存储过程RECURSIVE_FIND_CHILDREN
     * 
     * @param startCodes 起始编号
     * @param codeField 编号字段
     * @param parentField 上级编号字段
     * @param selectFields 查询字段列表
     * @param where 查询条件
     * @param orderings 排序条件
     * @param builder 生成SQL的帮助类
     * @return SQL语句
     */
    SqlBuffer buildFindChildrenSql(List<String> startCodes, String codeField, String parentField,
            Collection<String> selectFields, DbWhere where, List<Ordering> orderings, QueryFragmentBuilder builder);

}
