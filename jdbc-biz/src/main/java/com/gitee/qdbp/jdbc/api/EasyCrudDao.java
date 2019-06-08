package com.gitee.qdbp.jdbc.api;

import java.util.List;
import java.util.Map;
import com.gitee.qdbp.able.exception.ServiceException;
import com.gitee.qdbp.able.model.ordering.OrderPaging;
import com.gitee.qdbp.able.model.ordering.Ordering;
import com.gitee.qdbp.able.model.paging.PageList;
import com.gitee.qdbp.jdbc.condition.DbUpdate;
import com.gitee.qdbp.jdbc.condition.DbWhere;

/**
 * 基础增删改查数据库操作
 *
 * @author 赵卉华
 * @version 190601
 */
public interface EasyCrudDao<T> {

    /**
     * 根据主键编号获取对象<br>
     * 注意: 默认查询条件由modelDataExecutor添加, 只查有效项<br>
     * SELECT {columnNames} FROM {tableName} WHERE ID={id} AND EFTFLAG='E'
     * 
     * @param id 主键编号
     * @return 实体对象
     */
    T findById(String id) throws ServiceException;

    /**
     * 根据查询条件获取对象<br>
     * 注意: 默认查询条件由modelDataExecutor添加, 只查有效项<br>
     * SELECT {columnNames} FROM {tableName} WHERE {whereConditions} AND EFTFLAG='E'
     * 
     * @param where 查询条件, 如果没有查询条件应传入DbWhere.NONE
     * @return 实体对象
     */
    T find(DbWhere where) throws ServiceException;

    /**
     * 查找所有的实体列表, 不分页<br>
     * 注意: 默认查询条件由modelDataExecutor添加, 只查有效项<br>
     * SELECT {columnNames} FROM {tableName} WHERE EFTFLAG='E'
     * 
     * @return 列表数据
     */
    List<T> listAll() throws ServiceException;

    /**
     * 查找所有的实体列表, 不分页<br>
     * 注意: 默认查询条件由modelDataExecutor添加, 只查有效项<br>
     * SELECT {columnNames} FROM {tableName} WHERE EFTFLAG='E' ORDER BY {orderByConditions}
     * 
     * @param orderings 排序字段
     * @return 列表数据
     */
    List<T> listAll(List<Ordering> orderings) throws ServiceException;

    /**
     * 根据条件分页查询实体列表<br>
     * 注意: 默认查询条件由modelDataExecutor添加, 只查有效项<br>
     * <br>
     * SELECT COUNT(*) FROM {tableName} WHERE {whereConditions} AND EFTFLAG='E'<br>
     * SELECT {columnNames} FROM {tableName}<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;WHERE {whereConditions} AND EFTFLAG='E' ORDER BY {orderByConditions}
     * 
     * @param where 查询条件, 如果没有查询条件应传入DbWhere.NONE
     * @param odpg 分页/排序条件, 不需要分页也不需要排序时应传入OrderPaging.NONE
     * @return 列表数据
     */
    PageList<T> list(DbWhere where, OrderPaging odpg) throws ServiceException;

    /**
     * 根据条件查询某个字段的值<br>
     * 注意: 默认查询条件由modelDataExecutor添加, 只查有效项<br>
     * <br>
     * SELECT {columnName} FROM {tableName}<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;WHERE {whereConditions} AND EFTFLAG='E'
     * 
     * @param fieldName 指定字段名
     * @param where 查询条件, 如果没有查询条件应传入DbWhere.NONE
     * @param valueClazz 字段值类型
     * @return 字段的值列表
     */
    <V> V findFieldValue(String fieldName, DbWhere where, Class<V> valueClazz) throws ServiceException;

    /**
     * 根据条件查询某个字段的值列表<br>
     * 注意: 默认查询条件由modelDataExecutor添加, 只查有效项<br>
     * <br>
     * SELECT {columnName} FROM {tableName}<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;WHERE {whereConditions} AND EFTFLAG='E' ORDER BY {orderByConditions}
     * 
     * @param fieldName 指定字段名
     * @param distinct 是否去重
     * @param where 查询条件, 如果没有查询条件应传入DbWhere.NONE
     * @param orderings 排序条件, 如果不排序应传入Orderings.NONE
     * @param valueClazz 字段值类型
     * @return 字段的值列表
     */
    <V> List<V> listFieldValues(String fieldName, boolean distinct, DbWhere where, List<Ordering> orderings,
            Class<V> valueClazz) throws ServiceException;

    /**
     * 递归查询所有子节点<br>
     * ORACLE： START WITH {codeField} IN( {startCode} ) CONNECT BY PRIOR {codeField} = {parentField}<br>
     * DB2/SqlServer: 使用WITH递归<br>
     * MYSQL 8.0+/PostgreSQL: 使用WITH RECURSIVE递归<br>
     * MYSQL 8.0-: 使用存储过程RECURSIVE_FIND_CHILDREN
     * 
     * @param startCode 起始编号
     * @param codeField 编号字段名
     * @param parentField 上级编号字段名
     * @param where 查询条件, 如果没有查询条件应传入DbWhere.NONE
     * @param orderings 排序条件, 如果不排序应传入Orderings.NONE
     * @return 子节点编号
     */
    List<T> listChildren(String startCode, String codeField, String parentField, DbWhere where,
            List<Ordering> orderings);

    /**
     * 递归查询所有子节点<br>
     * ORACLE： START WITH {codeField} IN( {startCode} ) CONNECT BY PRIOR {codeField} = {parentField}<br>
     * DB2/SqlServer: 使用WITH递归<br>
     * MYSQL 8.0+/PostgreSQL: 使用WITH RECURSIVE递归<br>
     * MYSQL 8.0-: 使用存储过程RECURSIVE_FIND_CHILDREN
     * 
     * @param startCodes 起始编号
     * @param codeField 编号字段名
     * @param parentField 上级编号字段名
     * @param where 查询条件, 如果没有查询条件应传入DbWhere.NONE
     * @return 子节点编号
     */
    List<T> listChildren(List<String> startCodes, String codeField, String parentField, DbWhere where,
            List<Ordering> orderings);

    /**
     * 递归查询所有子节点编号<br>
     * ORACLE： START WITH {codeField} IN( {startCode} ) CONNECT BY PRIOR {codeField} = {parentField}<br>
     * DB2/SqlServer: 使用WITH递归<br>
     * MYSQL 8.0+/PostgreSQL: 使用WITH RECURSIVE递归<br>
     * MYSQL 8.0-: 使用存储过程RECURSIVE_FIND_CHILDREN
     * 
     * @param startCode 起始编号
     * @param codeField 编号字段名
     * @param parentField 上级编号字段名
     * @param where 查询条件, 如果没有查询条件应传入DbWhere.NONE
     * @param orderings 排序条件, 如果不排序应传入Orderings.NONE
     * @return 子节点编号
     */
    List<String> listChildrenCodes(String startCode, String codeField, String parentField, DbWhere where,
            List<Ordering> orderings);

    /**
     * 递归查询所有子节点编号<br>
     * ORACLE： START WITH {codeField} IN( {startCode} ) CONNECT BY PRIOR {codeField} = {parentField}<br>
     * DB2/SqlServer: 使用WITH递归<br>
     * MYSQL 8.0+/PostgreSQL: 使用WITH RECURSIVE递归<br>
     * MYSQL 8.0-: 使用存储过程RECURSIVE_FIND_CHILDREN
     * 
     * @param startCodes 起始编号
     * @param codeField 编号字段名
     * @param parentField 上级编号字段名
     * @param where 查询条件, 如果没有查询条件应传入DbWhere.NONE
     * @param orderings 排序条件, 如果不排序应传入Orderings.NONE
     * @return 子节点编号
     */
    List<String> listChildrenCodes(List<String> startCodes, String codeField, String parentField, DbWhere where,
            List<Ordering> orderings);

    /**
     * 根据条件统计实体数量<br>
     * 注意: 默认查询条件由modelDataExecutor添加, 只查有效项<br>
     * SELECT COUNT(*) FROM {tableName} WHERE {whereConditions} AND EFTFLAG='E'
     * 
     * @param where 查询条件, 如果没有查询条件应传入DbWhere.NONE
     * @return 数据数量
     */
    int count(DbWhere where) throws ServiceException;

    /**
     * 根据条件分组统计实体数量<br>
     * 注意: 默认查询条件由modelDataExecutor添加, 只查有效项<br>
     * SELECT {groupByColumnName}, COUNT(*) FROM {tableName} <br>
     * &nbsp;&nbsp;&nbsp;&nbsp;WHERE {whereConditions} AND EFTFLAG='E' GROUP BY {groupByColumnName}
     * 
     * @param groupBy 分组条件
     * @param where 查询条件, 如果没有查询条件应传入DbWhere.NONE
     * @return 列表数据
     */
    Map<String, Integer> groupCount(String groupBy, DbWhere where) throws ServiceException;

    /**
     * 保存实体对象<br>
     * 注意: 如果主键编号为空将会自动生成<br>
     * 注意: 默认设置eftflag='E'<br>
     * INSERT INTO {tableName}({columnNames}) VALUES ({fieldValues})
     * 
     * @param entity 实体对象
     * @param fillCreateParams 是否自动填充创建参数
     * @return 返回主键编号
     * @throws ServiceException 操作失败
     */
    String insert(T entity, boolean fillCreateParams) throws ServiceException;

    /**
     * 保存实体对象<br>
     * 注意: 如果主键编号为空将会自动生成<br>
     * 注意: 默认设置eftflag='E'<br>
     * INSERT INTO {tableName}({columnNames}) VALUES ({fieldValues})
     * 
     * @param entity 实体对象
     * @param fillCreateParams 是否自动填充创建参数
     * @return 返回主键编号
     * @throws ServiceException 操作失败
     */
    String insert(Map<String, Object> entity, boolean fillCreateParams) throws ServiceException;

    /**
     * 根据主键编号更新实体对象<br>
     * 注意: 如果主键编号为空将会报错<br>
     * 注意: 默认查询条件由modelDataExecutor添加, 只处理有效项<br>
     * INSERT INTO {tableName}({columnNames}) VALUES ({fieldValues})
     * 
     * @param entity 实体对象
     * @param fillUpdateParams 是否自动填充更新参数
     * @param errorOnUnaffected 受影响行数为0时是否抛异常
     * @return 受影响行数
     * @throws ServiceException 操作失败
     */
    int update(T entity, boolean fillUpdateParams, boolean errorOnUnaffected) throws ServiceException;

    /**
     * 根据条件批量更新实体对象<br>
     * 注意: 默认查询条件由modelDataExecutor添加, 只处理有效项<br>
     * UPDATE {tableName} SET {columnName}={fieldValue}, ... WHERE {whereConditions} AND EFTFLAG='E'
     * 
     * @param entity 实体对象
     * @param where 匹配条件
     * @param fillUpdateParams 是否自动填充更新参数
     * @param errorOnUnaffected 受影响行数为0时是否抛异常
     * @return 受影响行数
     * @throws ServiceException 操作失败
     */
    int update(T entity, DbWhere where, boolean fillUpdateParams, boolean errorOnUnaffected) throws ServiceException;

    /**
     * 根据条件批量更新实体对象<br>
     * 注意: 默认查询条件由modelDataExecutor添加, 只处理有效项<br>
     * UPDATE {tableName} SET {columnName}={fieldValue}, ... WHERE {whereConditions} AND EFTFLAG='E'
     * 
     * @param entity 实体对象
     * @param where 匹配条件
     * @param fillUpdateParams 是否自动填充更新参数
     * @param errorOnUnaffected 受影响行数为0时是否抛异常
     * @return 受影响行数
     * @throws ServiceException 操作失败
     */
    int update(DbUpdate entity, DbWhere where, boolean fillUpdateParams, boolean errorOnUnaffected)
            throws ServiceException;

    /**
     * 根据主键编号删除实体对象(逻辑删除)<br>
     * 注意: 默认查询条件由modelDataExecutor添加, 只处理有效项<br>
     * UPDATE {tableName} SET EFTFLAG='D' WHERE ID IN ({ids}) EFTFLAG='E'
     *
     * @param ids 待删除的主键编号
     * @param fillUpdateParams 是否自动填充更新参数
     * @param errorOnUnaffected 受影响行数为0时是否抛异常
     * @return 删除行数
     * @throws ServiceException 删除失败
     */
    int logicalDeleteByIds(List<String> ids, boolean fillUpdateParams, boolean errorOnUnaffected)
            throws ServiceException;

    /**
     * 根据条件批量更新实体对象(逻辑删除)<br>
     * 注意: 默认查询条件由modelDataExecutor添加, 只处理有效项<br>
     * UPDATE {tableName} SET EFTFLAG='D' WHERE {whereConditions} AND EFTFLAG='E'
     * 
     * @param where 匹配条件
     * @param fillUpdateParams 是否自动填充更新参数
     * @param errorOnUnaffected 受影响行数为0时是否抛异常
     * @return 受影响行数
     * @throws ServiceException 操作失败
     */
    int logicalDelete(T where, boolean fillUpdateParams, boolean errorOnUnaffected) throws ServiceException;

    /**
     * 根据条件批量删除实体对象(逻辑删除)<br>
     * 注意: 默认查询条件由modelDataExecutor添加, 只处理有效项<br>
     * UPDATE {tableName} SET EFTFLAG='D' WHERE {whereConditions} AND EFTFLAG='E'
     * 
     * @param where 匹配条件
     * @param fillUpdateParams 是否自动填充更新参数
     * @param errorOnUnaffected 受影响行数为0时是否抛异常
     * @return 受影响行数
     * @throws ServiceException 操作失败
     */
    int logicalDelete(DbWhere where, boolean fillUpdateParams, boolean errorOnUnaffected) throws ServiceException;

    /**
     * 根据主键编号删除实体对象(物理删除)<br>
     * 注意: 默认查询条件由modelDataExecutor添加, 只处理有效项<br>
     * UPDATE {tableName} SET EFTFLAG='D' WHERE ID IN ({ids}) EFTFLAG='E'
     *
     * @param ids 待删除的主键编号
     * @param errorOnUnaffected 受影响行数为0时是否抛异常
     * @return 删除行数
     * @throws ServiceException 删除失败
     */
    int physicalDeleteByIds(List<String> ids, boolean errorOnUnaffected) throws ServiceException;

    /**
     * 根据条件批量更新实体对象(物理删除)<br>
     * 注意: 默认查询条件由modelDataExecutor添加, 只处理有效项<br>
     * UPDATE {tableName} SET EFTFLAG='D' WHERE {whereConditions} AND EFTFLAG='E'
     * 
     * @param where 匹配条件
     * @param fillUpdateParams 是否自动填充更新参数
     * @param errorOnUnaffected 受影响行数为0时是否抛异常
     * @return 受影响行数
     * @throws ServiceException 操作失败
     */
    int physicalDelete(T where, boolean errorOnUnaffected) throws ServiceException;

    /**
     * 根据条件批量删除实体对象(物理删除)<br>
     * 注意: 默认查询条件由modelDataExecutor添加, 只处理有效项<br>
     * UPDATE {tableName} SET EFTFLAG='D' WHERE {whereConditions} AND EFTFLAG='E'
     * 
     * @param where 匹配条件
     * @param errorOnUnaffected 受影响行数为0时是否抛异常
     * @param fillUpdateParams 是否自动填充更新参数
     * @return 受影响行数
     * @throws ServiceException 操作失败
     */
    int physicalDelete(DbWhere where, boolean errorOnUnaffected) throws ServiceException;
}
