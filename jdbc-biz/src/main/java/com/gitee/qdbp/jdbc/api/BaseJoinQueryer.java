package com.gitee.qdbp.jdbc.api;

import java.util.List;
import java.util.Map;
import com.gitee.qdbp.able.exception.ServiceException;
import com.gitee.qdbp.able.model.ordering.OrderPaging;
import com.gitee.qdbp.able.model.ordering.Ordering;
import com.gitee.qdbp.able.model.paging.PartList;
import com.gitee.qdbp.jdbc.condition.DbWhere;

/**
 * 基础表连接查询操作
 *
 * @author 赵卉华
 * @version 190605
 */
public interface BaseJoinQueryer<T> {

    /**
     * 主要功能: 根据查询条件获取对象<br>
     * 注意事项: 默认查询条件eftflag='E', 只查有效项<br>
     * SELECT {columnNames} FROM {tableNameA} A 
     * INNER JOIN {tableNameB} B ON A.DATA_ID=B.ID AND B.EFTFLAG='E'
     * WHERE {whereConditions} AND A.EFTFLAG='E'
     * 
     * @param where 查询条件, 如果没有查询条件应传入DbWhere.NONE
     * @return 实体对象
     */
    T find(DbWhere where) throws ServiceException;

    /**
     * 主要功能: 查找所有的实体列表, 不分页<br>
     * 注意事项: 默认查询条件eftflag='E', 只查有效项<br>
     * SELECT {columnNames} FROM {tableName} WHERE EFTFLAG='E'
     * 
     * @return 列表数据
     */
    List<T> listAll() throws ServiceException;

    /**
     * 主要功能: 查找所有的实体列表, 不分页<br>
     * 注意事项: 默认查询条件eftflag='E', 只查有效项<br>
     * SELECT {columnNames} FROM {tableName} WHERE EFTFLAG='E' ORDER BY {orderByConditions}
     * 
     * @param orderings 排序字段
     * @return 列表数据
     */
    List<T> listAll(List<Ordering> orderings) throws ServiceException;

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
     * @return 列表数据
     */
    PartList<T> list(DbWhere where, OrderPaging odpg) throws ServiceException;

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
