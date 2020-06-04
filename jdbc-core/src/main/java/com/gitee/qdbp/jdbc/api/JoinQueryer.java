package com.gitee.qdbp.jdbc.api;

import java.util.List;
import java.util.Map;
import com.gitee.qdbp.able.exception.ServiceException;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.able.jdbc.ordering.OrderPaging;
import com.gitee.qdbp.able.jdbc.ordering.Orderings;
import com.gitee.qdbp.able.jdbc.paging.PageList;
import com.gitee.qdbp.jdbc.sql.build.QuerySqlBuilder;

/**
 * 基础表连接查询操作<br>
 * 这里的所有字段(DbWhere/Ordering/groupBy/fieldName), 如果存在重名则需要带表别名, 如u.userName
 * 
 * @param <T> 查询结果类型
 * @author zhaohuihua
 * @version 190608
 */
public interface JoinQueryer<T> {

    /** SQL生成工具 **/
    QuerySqlBuilder getSqlBuilder();

    /**
     * 根据查询条件获取对象<br>
     * 注意: 默认查询条件由entityFillExecutor添加, 只查有效项<br>
     * SELECT {columnNames} FROM {tableNameA} A<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;INNER JOIN {tableNameB} B<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ON A.DATA_ID=B.ID AND B.DATA_STATE=0<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;WHERE {whereConditions} AND A.DATA_STATE=0
     * 
     * @param where 查询条件, 如果没有查询条件应传入DbWhere.NONE
     * @param resultType 查询结果类型
     * @return 实体对象
     */
    T find(DbWhere where) throws ServiceException;

    /**
     * 查找所有的实体列表, 不分页<br>
     * 注意: 默认查询条件由entityFillExecutor添加, 只查有效项<br>
     * SELECT {columnNames} FROM {tableNameA} A<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;INNER JOIN {tableNameB} B<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ON A.DATA_ID=B.ID AND B.DATA_STATE=0<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;WHERE A.DATA_STATE=0
     * 
     * @param resultType 查询结果类型
     * @return 列表数据
     */
    List<T> listAll() throws ServiceException;

    /**
     * 查找所有的实体列表, 不分页<br>
     * 注意: 默认查询条件由entityFillExecutor添加, 只查有效项<br>
     * SELECT {columnNames} FROM {tableNameA} A<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;INNER JOIN {tableNameB} B<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ON A.DATA_ID=B.ID AND B.DATA_STATE=0<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;WHERE A.DATA_STATE=0<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ORDER BY {orderByConditions}
     * 
     * @param orderings 排序字段
     * @return 列表数据
     */
    List<T> listAll(Orderings orderings) throws ServiceException;

    /**
     * 根据条件分页查询实体列表<br>
     * 注意: 默认查询条件由entityFillExecutor添加, 只查有效项<br>
     * <br>
     * SELECT COUNT(*) FROM {tableNameA} A<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;INNER JOIN {tableNameB} B<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ON A.DATA_ID=B.ID AND B.DATA_STATE=0<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;WHERE {whereConditions} AND A.DATA_STATE=0<br>
     * SELECT {columnNames} FROM {tableNameA} A<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;INNER JOIN {tableNameB} B<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ON A.DATA_ID=B.ID AND B.DATA_STATE=0<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;WHERE {whereConditions} AND A.DATA_STATE=0<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ORDER BY {orderByConditions}
     * 
     * @param where 查询条件, 如果没有查询条件应传入DbWhere.NONE
     * @param odpg 分页/排序条件, 不需要分页也不需要排序时应传入OrderPaging.NONE
     * @return 列表数据
     */
    PageList<T> list(DbWhere where, OrderPaging odpg) throws ServiceException;

    /**
     * 根据条件统计实体数量<br>
     * 注意: 默认查询条件由entityFillExecutor添加, 只查有效项<br>
     * SELECT COUNT(*) FROM {tableNameA} A<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;INNER JOIN {tableNameB} B<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ON A.DATA_ID=B.ID AND B.DATA_STATE=0<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;WHERE {whereConditions} AND A.DATA_STATE=0
     * 
     * @param where 查询条件, 如果没有查询条件应传入DbWhere.NONE
     * @return 数据数量
     */
    int count(DbWhere where) throws ServiceException;

    /**
     * 根据条件分组统计实体数量<br>
     * 注意: 默认查询条件由entityFillExecutor添加, 只查有效项<br>
     * SELECT {groupByColumnName}, COUNT(*) FROM {tableNameA} A<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;INNER JOIN {tableNameB} B<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ON A.DATA_ID=B.ID AND B.DATA_STATE=0<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;WHERE {whereConditions} AND A.DATA_STATE=0<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;GROUP BY {groupByColumnName}
     * 
     * @param groupBy 分组条件
     * @param where 查询条件, 如果没有查询条件应传入DbWhere.NONE
     * @return 列表数据
     */
    Map<String, Integer> groupCount(String groupBy, DbWhere where) throws ServiceException;

    /**
     * 根据条件查询某个字段的值<br>
     * 注意: 默认查询条件由entityFillExecutor添加, 只查有效项<br>
     * <br>
     * SELECT {columnName} FROM {tableNameA} A<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;INNER JOIN {tableNameB} B<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ON A.DATA_ID=B.ID AND B.DATA_STATE=0<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;WHERE {whereConditions} AND A.DATA_STATE=0
     * 
     * @param fieldName 指定字段名
     * @param where 查询条件, 如果没有查询条件应传入DbWhere.NONE
     * @param valueClazz 字段值类型
     * @return 字段的值列表
     */
    <V> V findFieldValue(String fieldName, DbWhere where, Class<V> valueClazz) throws ServiceException;

    /**
     * 根据条件查询某个字段的值列表<br>
     * 注意: 默认查询条件由entityFillExecutor添加, 只查有效项<br>
     * <br>
     * SELECT {columnName} FROM {tableNameA} A<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;INNER JOIN {tableNameB} B<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ON A.DATA_ID=B.ID AND B.DATA_STATE=0<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;WHERE {whereConditions} AND A.DATA_STATE=0<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ORDER BY {orderByConditions}
     * 
     * @param fieldName 指定字段名
     * @param distinct 是否去重
     * @param where 查询条件, 如果没有查询条件应传入DbWhere.NONE
     * @param odpg 分页/排序条件, 不需要分页也不需要排序时应传入OrderPaging.NONE
     * @param valueClazz 字段值类型
     * @return 字段的值列表
     */
    <V> PageList<V> listFieldValues(String fieldName, boolean distinct, DbWhere where, OrderPaging odpg,
            Class<V> valueClazz) throws ServiceException;
}
