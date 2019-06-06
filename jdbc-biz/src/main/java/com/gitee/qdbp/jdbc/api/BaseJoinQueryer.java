package com.gitee.qdbp.jdbc.api;

import java.util.List;
import java.util.Map;
import com.gitee.qdbp.able.exception.ServiceException;
import com.gitee.qdbp.able.model.ordering.OrderPaging;
import com.gitee.qdbp.able.model.ordering.Ordering;
import com.gitee.qdbp.able.model.paging.PageList;
import com.gitee.qdbp.jdbc.condition.DbWhere;

/**
 * 基础表连接查询操作
 *
 * @author 赵卉华
 * @version 190605
 */
public interface BaseJoinQueryer {

    /**
     * 主要功能: 根据查询条件获取对象<br>
     * 注意事项: 默认查询条件eftflag='E', 只查有效项<br>
     * SELECT {columnNames} FROM {tableNameA} A 
     * INNER JOIN {tableNameB} B ON A.DATA_ID=B.ID AND B.EFTFLAG='E'
     * WHERE {whereConditions} AND A.EFTFLAG='E'
     * 
     * @param where 查询条件, 如果没有查询条件应传入DbWhere.NONE
     * @param resultType 查询结果类型
     * @return 实体对象
     */
    <T> T find(DbWhere where, Class<T> resultType) throws ServiceException;

    /**
     * 主要功能: 查找所有的实体列表, 不分页<br>
     * 注意事项: 默认查询条件eftflag='E', 只查有效项<br>
     * SELECT {columnNames} FROM {tableName} WHERE EFTFLAG='E'
     * 
     * @param resultType 查询结果类型
     * @return 列表数据
     */
    <T> List<T> listAll(Class<T> resultType) throws ServiceException;

    /**
     * 主要功能: 查找所有的实体列表, 不分页<br>
     * 注意事项: 默认查询条件eftflag='E', 只查有效项<br>
     * SELECT {columnNames} FROM {tableName} WHERE EFTFLAG='E' ORDER BY {orderByConditions}
     * 
     * @param orderings 排序字段
     * @param resultType 查询结果类型
     * @return 列表数据
     */
    <T> List<T> listAll(List<Ordering> orderings, Class<T> resultType) throws ServiceException;

    /**
     * 主要功能: 根据条件分页查询实体列表<br>
     * 注意事项: 默认查询条件eftflag='E', 只查有效项<br>
     * <br>
     * SELECT COUNT(*) FROM {tableName} WHERE {whereConditions} AND EFTFLAG='E'<br>
     * SELECT {columnNames} FROM {tableName}<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;WHERE {whereConditions} AND EFTFLAG='E' ORDER BY {orderByConditions}
     * 
     * @param where 查询条件, 如果没有查询条件应传入DbWhere.NONE
     * @param odpg 分页/排序条件, 不需要分页也不需要排序时应传入OrderPaging.NONE
     * @param resultType 查询结果类型
     * @return 列表数据
     */
    <T> PageList<T> list(DbWhere where, OrderPaging odpg, Class<T> resultType) throws ServiceException;

    /**
     * 主要功能: 根据条件统计实体数量<br>
     * 注意事项: 默认查询条件eftflag='E', 只查有效项<br>
     * SELECT COUNT(*) FROM {tableName} WHERE {whereConditions} AND EFTFLAG='E'
     * 
     * @param where 查询条件, 如果没有查询条件应传入DbWhere.NONE
     * @return 数据数量
     */
    int count(DbWhere where) throws ServiceException;

    /**
     * 主要功能: 根据条件分组统计实体数量<br>
     * 注意事项: 默认查询条件eftflag='E', 只查有效项<br>
     * SELECT {groupByColumnName}, COUNT(*) FROM {tableName} <br>
     * &nbsp;&nbsp;&nbsp;&nbsp;WHERE {whereConditions} AND EFTFLAG='E' GROUP BY {groupByColumnName}
     * 
     * @param groupBy 分组条件
     * @param where 查询条件, 如果没有查询条件应传入DbWhere.NONE
     * @return 列表数据
     */
    Map<String, Integer> groupCount(String groupBy, DbWhere where) throws ServiceException;

}
