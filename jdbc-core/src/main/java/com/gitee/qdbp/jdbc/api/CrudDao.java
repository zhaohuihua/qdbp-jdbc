package com.gitee.qdbp.jdbc.api;

import java.util.List;
import java.util.Map;
import com.gitee.qdbp.able.exception.ServiceException;
import com.gitee.qdbp.able.jdbc.condition.DbUpdate;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.able.jdbc.fields.Fields;
import com.gitee.qdbp.able.jdbc.ordering.OrderPaging;
import com.gitee.qdbp.able.jdbc.ordering.Orderings;
import com.gitee.qdbp.able.jdbc.paging.PageList;
import com.gitee.qdbp.jdbc.plugins.DbConditionConverter;
import com.gitee.qdbp.jdbc.sql.build.CrudSqlBuilder;

/**
 * 基础增删改查数据库操作
 *
 * @author 赵卉华
 * @version 190601
 */
public interface CrudDao<T> {

    /** SQL生成工具 **/
    CrudSqlBuilder getSqlBuilder();

    /**
     * 根据主键编号获取对象<br>
     * 注意: 默认查询条件由entityFillExecutor添加, 只查有效项<br>
     * SELECT {columnNames} FROM {tableName} WHERE ID={id} AND DATA_STATE=0
     * 
     * @param id 主键编号
     * @return 实体对象
     */
    T findById(String id) throws ServiceException;

    /**
     * 根据查询条件获取对象<br>
     * 注意: 默认查询条件由entityFillExecutor添加, 只查有效项<br>
     * SELECT {columnNames} FROM {tableName} WHERE {whereConditions} AND DATA_STATE=0
     * 
     * @param where 查询条件, 如果没有查询条件应传入DbWhere.NONE
     * @return 实体对象
     */
    T find(DbWhere where) throws ServiceException;

    /**
     * 根据查询条件获取对象, 只查询指定字段<br>
     * 注意: 默认查询条件由entityFillExecutor添加, 只查有效项<br>
     * SELECT {columnNames} FROM {tableName} WHERE {whereConditions} AND DATA_STATE=0
     * 
     * @param fields 查询的字段: 全部字段传入Fields.ALL, 指定字段传入IncludeFields对象, 排除字段传入ExcludeFields对象
     * @param where 查询条件, 如果没有查询条件应传入DbWhere.NONE
     * @return 实体对象
     */
    T find(Fields fields, DbWhere where) throws ServiceException;

    /**
     * 查找所有的实体列表, 不分页<br>
     * 注意: 默认查询条件由entityFillExecutor添加, 只查有效项<br>
     * SELECT {columnNames} FROM {tableName} WHERE DATA_STATE=0
     * 
     * @return 列表数据
     */
    List<T> listAll() throws ServiceException;

    /**
     * 查找所有的实体列表, 不分页, 只查询指定字段<br>
     * 注意: 默认查询条件由entityFillExecutor添加, 只查有效项<br>
     * SELECT {columnNames} FROM {tableName} WHERE DATA_STATE=0
     * 
     * @param fields 查询的字段: 全部字段传入Fields.ALL, 指定字段传入IncludeFields对象, 排除字段传入ExcludeFields对象
     * @return 列表数据
     */
    List<T> listAll(Fields fields) throws ServiceException;

    /**
     * 查找所有的实体列表, 不分页<br>
     * 注意: 默认查询条件由entityFillExecutor添加, 只查有效项<br>
     * SELECT {columnNames} FROM {tableName} WHERE DATA_STATE=0 ORDER BY {orderByConditions}
     * 
     * @param orderings 排序字段, 不需要排序时应传入Orderings.NONE
     * @return 列表数据
     */
    List<T> listAll(Orderings orderings) throws ServiceException;

    /**
     * 查找所有的实体列表, 不分页, 只查询指定字段<br>
     * 注意: 默认查询条件由entityFillExecutor添加, 只查有效项<br>
     * SELECT {columnNames} FROM {tableName} WHERE DATA_STATE=0 ORDER BY {orderByConditions}
     * 
     * @param fields 查询的字段: 全部字段传入Fields.ALL, 指定字段传入IncludeFields对象, 排除字段传入ExcludeFields对象
     * @param orderings 排序字段, 不需要排序时应传入Orderings.NONE
     * @return 列表数据
     */
    List<T> listAll(Fields fields, Orderings orderings) throws ServiceException;

    /**
     * 根据条件查询实体列表<br>
     * 注意: 默认查询条件由entityFillExecutor添加, 只查有效项<br>
     * <br>
     * SELECT COUNT(*) FROM {tableName} WHERE {whereConditions} AND DATA_STATE=0<br>
     * SELECT {columnNames} FROM {tableName}<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;WHERE {whereConditions} AND DATA_STATE=0<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ORDER BY {orderByConditions}
     * 
     * @param where 查询条件, 如果没有查询条件应传入DbWhere.NONE
     * @param orderings 排序条件, 不需要排序时应传入Orderings.NONE
     * @return 列表数据
     */
    List<T> list(DbWhere where, Orderings orderings) throws ServiceException;

    /**
     * 根据条件分页查询实体列表<br>
     * 注意: 默认查询条件由entityFillExecutor添加, 只查有效项<br>
     * <br>
     * SELECT COUNT(*) FROM {tableName} WHERE {whereConditions} AND DATA_STATE=0<br>
     * SELECT {columnNames} FROM {tableName}<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;WHERE {whereConditions} AND DATA_STATE=0<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ORDER BY {orderByConditions}
     * 
     * @param where 查询条件, 如果没有查询条件应传入DbWhere.NONE
     * @param odpg 分页/排序条件, 不需要分页也不需要排序时应传入OrderPaging.NONE
     * @return 列表数据
     */
    PageList<T> list(DbWhere where, OrderPaging odpg) throws ServiceException;

    /**
     * 主要功能: 按指定字段查找所有的实体列表, 不分页<br>
     * 注意: 默认查询条件由entityFillExecutor添加, 只查有效项<br>
     * <br>
     * SELECT {columnNames} FROM {tableName}<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;WHERE {whereConditions} AND DATA_STATE=0<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ORDER BY {orderByConditions}<br>
     * 
     * @param fields 查询的字段: 全部字段传入Fields.ALL, 指定字段传入IncludeFields对象, 排除字段传入ExcludeFields对象
     * @param where 查询条件, 如果没有查询条件应传入DbWhere.NONE
     * @param orderings 排序条件, 不需要排序时应传入Orderings.NONE
     * @return 列表数据
     */
    List<T> list(Fields fields, DbWhere where, Orderings orderings) throws ServiceException;

    /**
     * 主要功能: 根据条件分页按指定字段查询实体列表<br>
     * 注意: 默认查询条件由entityFillExecutor添加, 只查有效项<br>
     * <br>
     * SELECT COUNT(*) FROM {tableName} WHERE {whereConditions} AND DATA_STATE=0<br>
     * SELECT {columnNames} FROM {tableName}<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;WHERE {whereConditions} AND DATA_STATE=0<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ORDER BY {orderByConditions}<br>
     * 
     * @param fields 查询的字段: 全部字段传入Fields.ALL, 指定字段传入IncludeFields对象, 排除字段传入ExcludeFields对象
     * @param where 查询条件, 如果没有查询条件应传入DbWhere.NONE
     * @param odpg 分页/排序条件, 不需要分页也不需要排序时应传入OrderPaging.NONE
     * @return 列表数据
     */
    PageList<T> list(Fields fields, DbWhere where, OrderPaging odpg) throws ServiceException;

    /**
     * 根据条件查询某个字段的值<br>
     * 注意: 默认查询条件由entityFillExecutor添加, 只查有效项<br>
     * <br>
     * SELECT {columnName} FROM {tableName}<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;WHERE {whereConditions} AND DATA_STATE=0
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
     * SELECT {columnName} FROM {tableName}<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;WHERE {whereConditions} AND DATA_STATE=0<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ORDER BY {orderByConditions}
     * 
     * @param fieldName 指定字段名
     * @param distinct 是否去重
     * @param where 查询条件, 如果没有查询条件应传入DbWhere.NONE
     * @param orderings 排序条件, 不需要排序时应传入Orderings.NONE
     * @param valueClazz 字段值类型
     * @return 字段的值列表
     */
    <V> List<V> listFieldValues(String fieldName, boolean distinct, DbWhere where, Orderings orderings,
            Class<V> valueClazz) throws ServiceException;

    /**
     * 根据条件查询某个字段的值列表<br>
     * 注意: 默认查询条件由entityFillExecutor添加, 只查有效项<br>
     * <br>
     * SELECT {columnName} FROM {tableName}<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;WHERE {whereConditions} AND DATA_STATE=0<br>
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
    List<T> listChildren(String startCode, String codeField, String parentField, DbWhere where, Orderings orderings);

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
            Orderings orderings);

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
            Orderings orderings);

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
            Orderings orderings);

    /**
     * 根据条件统计实体数量<br>
     * 注意: 默认查询条件由entityFillExecutor添加, 只查有效项<br>
     * SELECT COUNT(*) FROM {tableName} WHERE {whereConditions} AND DATA_STATE=0
     * 
     * @param where 查询条件, 如果没有查询条件应传入DbWhere.NONE
     * @return 数据数量
     */
    int count(DbWhere where) throws ServiceException;

    /**
     * 根据条件分组统计实体数量<br>
     * 注意: 默认查询条件由entityFillExecutor添加, 只查有效项<br>
     * SELECT {groupByColumnName}, COUNT(*) FROM {tableName} <br>
     * &nbsp;&nbsp;&nbsp;&nbsp;WHERE {whereConditions} AND DATA_STATE=0 GROUP BY {groupByColumnName}
     * 
     * @param groupBy 分组条件
     * @param where 查询条件, 如果没有查询条件应传入DbWhere.NONE
     * @return 列表数据
     */
    Map<String, Integer> groupCount(String groupBy, DbWhere where) throws ServiceException;

    /**
     * 保存实体对象<br>
     * 注意: 如果主键编号为空将会自动生成<br>
     * 注意: 默认创建参数由entityFillExecutor添加, 如dataState=DataState.NORMAL<br>
     * INSERT INTO {tableName}({columnNames}) VALUES ({fieldValues})
     * 
     * @param entity 实体对象
     * @param fillCreateParams 是否自动填充更新参数(创建人/创建时间等)
     * @return 返回主键编号
     * @throws ServiceException 操作失败
     * @see DbConditionConverter#convertBeanToInsertMap(Object) 参数转换说明
     */
    String insert(T entity, boolean fillCreateParams) throws ServiceException;

    /**
     * 保存实体对象<br>
     * 注意: 如果主键编号为空将会自动生成<br>
     * 注意: 默认创建参数由entityFillExecutor添加, 如dataState=DataState.NORMAL<br>
     * INSERT INTO {tableName}({columnNames}) VALUES ({fieldValues})
     * 
     * @param entity 实体对象
     * @param fillCreateParams 是否自动填充更新参数(创建人/创建时间等)
     * @return 返回主键编号
     * @throws ServiceException 操作失败
     * @see DbConditionConverter#convertBeanToInsertMap(Object) 参数转换说明
     */
    String insert(Map<String, Object> entity, boolean fillCreateParams) throws ServiceException;

    /**
     * 根据主键编号更新实体对象<br>
     * 注意: 如果主键编号为空将会报错<br>
     * 注意: 默认查询条件由entityFillExecutor添加, 默认只处理有效项<br>
     * UPDATE {tableName} SET {columnName}={fieldValue}, ... WHERE ID={id} AND DATA_STATE=0
     * 
     * @param entity 实体对象
     * @param fillUpdateParams 是否自动填充更新参数(修改人/修改时间等)
     * @param errorOnUnaffected 受影响行数为0时是否抛异常
     * @return 受影响行数
     * @throws ServiceException 操作失败
     * @see DbConditionConverter#convertBeanToDbUpdate(Object) 参数转换说明
     */
    int update(T entity, boolean fillUpdateParams, boolean errorOnUnaffected) throws ServiceException;

    /**
     * 根据主键编号更新实体对象<br>
     * 注意: 如果主键编号为空将会报错<br>
     * 注意: 默认查询条件由entityFillExecutor添加, 默认只处理有效项<br>
     * UPDATE {tableName} SET {columnName}={fieldValue}, ... WHERE ID={id} AND DATA_STATE=0
     * 
     * @param entity 实体对象(<b>注意:</b> 如果存在
     * @param fillUpdateParams 是否自动填充更新参数(修改人/修改时间等)
     * @param errorOnUnaffected 受影响行数为0时是否抛异常
     * @return 受影响行数
     * @throws ServiceException 操作失败
     * @see DbConditionConverter#parseMapToDbUpdate(Map) 参数转换说明
     */
    int update(Map<String, Object> entity, boolean fillUpdateParams, boolean errorOnUnaffected) throws ServiceException;

    /**
     * 根据条件批量更新实体对象<br>
     * 注意: 默认查询条件由entityFillExecutor添加, 默认只处理有效项<br>
     * UPDATE {tableName} SET {columnName}={fieldValue}, ... WHERE {whereConditions} AND DATA_STATE=0
     * 
     * @param entity 实体对象
     * @param where 匹配条件
     * @param fillUpdateParams 是否自动填充更新参数(修改人/修改时间等)
     * @param errorOnUnaffected 受影响行数为0时是否抛异常
     * @return 受影响行数
     * @throws ServiceException 操作失败
     * @see DbConditionConverter#convertBeanToDbUpdate(Object) entity参数转换说明
     * @see DbWhere where条件参数转换说明
     */
    int update(T entity, DbWhere where, boolean fillUpdateParams, boolean errorOnUnaffected) throws ServiceException;

    /**
     * 根据条件批量更新实体对象<br>
     * 注意: 默认查询条件由entityFillExecutor添加, 默认只处理有效项<br>
     * UPDATE {tableName} SET {columnName}={fieldValue}, ... WHERE {whereConditions} AND DATA_STATE=0
     * 
     * @param entity 实体对象
     * @param where 匹配条件
     * @param fillUpdateParams 是否自动填充更新参数(修改人/修改时间等)
     * @param errorOnUnaffected 受影响行数为0时是否抛异常
     * @return 受影响行数
     * @throws ServiceException 操作失败
     */
    int update(DbUpdate entity, DbWhere where, boolean fillUpdateParams, boolean errorOnUnaffected)
            throws ServiceException;

    /**
     * 批量保存实体对象<br>
     * 注意: 如果主键编号为空将会自动生成<br>
     * 注意: 默认创建参数由entityFillExecutor添加, 如dataState=DataState.NORMAL<br>
     * 注意: 根据实现类的不同, 有以下注意事项, 请详查具体实现类的机制:<br>
     * -- 大部分的实现类要求实体列表字段对齐<br>
     * ---- 例如第1个实体有abcd四个字段,第2个实体只有abc三个字段, 则第2个实体的d字段将被设置为NULL<br>
     * ---- 这将会导致数据库设置的默认值不会生效<br>
     * 
     * @param entities 实体对象列表(只能是entity或map或IdEntity列表, 其他参数将会报错)
     * @param fillCreateParams 是否自动填充更新参数(创建人/创建时间等)
     * @return 返回主键编号
     * @throws ServiceException 操作失败
     * @see DbConditionConverter#convertBeanToInsertMap(Object) 参数转换说明
     */
    List<String> inserts(List<?> entities, boolean fillCreateParams) throws ServiceException;

    /**
     * 根据主键编号批量更新实体对象<br>
     * 注意: 如果主键编号为空将会报错<br>
     * 注意: 默认查询条件由entityFillExecutor添加, 默认只处理有效项<br>
     * 注意: 根据实现类的不同, 有以下注意事项, 请详查具体实现类的机制:<br>
     * -- 1.某些实现类可能无法获取到准确的受影响行数<br>
     * -- 2.大部分的实现类要求实体列表字段对齐<br>
     * ---- 例如第1个实体有abcd四个字段,第2个实体只有abc三个字段, 则第2个实体的d字段将被更新为NULL
     * 
     * @param entities 实体对象列表(只能是entity或map或IdUpdate列表, 其他参数将会报错)<br>
     *            如果实体对象是map, map下不能有where, 否则将会报错
     * @param commonWhere 除ID外的公共过滤条件, 如果没有公共过滤条件应传入DbWhere.NONE
     * @param fillUpdateParams 是否自动填充更新参数(修改人/修改时间等)
     * @return 受影响行数(某些实现类可能无法获取到准确的受影响行数)
     * @throws ServiceException 操作失败
     * @see DbConditionConverter#convertBeanToDbUpdate(Object) entity参数转换说明
     */
    int updates(List<?> entities, DbWhere commonWhere, boolean fillUpdateParams) throws ServiceException;

    /**
     * 根据主键编号删除实体对象(逻辑删除)<br>
     * 注意: 默认查询条件由entityFillExecutor添加, 默认只处理有效项<br>
     * UPDATE {tableName} SET DATA_STATE=1 WHERE ID IN ({ids}) DATA_STATE=0
     *
     * @param ids 待删除的主键编号
     * @param fillUpdateParams 是否自动填充更新参数(修改人/修改时间等)
     * @param errorOnUnaffected 受影响行数为0时是否抛异常
     * @return 删除行数
     * @throws ServiceException 删除失败
     */
    int logicalDeleteByIds(List<String> ids, boolean fillUpdateParams, boolean errorOnUnaffected)
            throws ServiceException;

    /**
     * 根据条件批量更新实体对象(逻辑删除)<br>
     * 注意: 默认查询条件由entityFillExecutor添加, 默认只处理有效项<br>
     * UPDATE {tableName} SET DATA_STATE=1 WHERE {whereConditions} AND DATA_STATE=0
     * 
     * @param where 匹配条件, 如果要删除全部记录应传入DbWhere.NONE
     * @param fillUpdateParams 是否自动填充更新参数(修改人/修改时间等)
     * @param errorOnUnaffected 受影响行数为0时是否抛异常
     * @return 受影响行数
     * @throws ServiceException 操作失败
     */
    int logicalDelete(T where, boolean fillUpdateParams, boolean errorOnUnaffected) throws ServiceException;

    /**
     * 根据条件批量删除实体对象(逻辑删除)<br>
     * 注意: 默认查询条件由entityFillExecutor添加, 默认只处理有效项<br>
     * UPDATE {tableName} SET DATA_STATE=1 WHERE {whereConditions} AND DATA_STATE=0
     * 
     * @param where 匹配条件, 如果要删除全部记录应传入DbWhere.NONE
     * @param fillUpdateParams 是否自动填充更新参数(修改人/修改时间等)
     * @param errorOnUnaffected 受影响行数为0时是否抛异常
     * @return 受影响行数
     * @throws ServiceException 操作失败
     */
    int logicalDelete(DbWhere where, boolean fillUpdateParams, boolean errorOnUnaffected) throws ServiceException;

    /**
     * 根据主键编号删除实体对象(物理删除)<br>
     * 注意: 默认查询条件由entityFillExecutor添加, 默认只处理有效项<br>
     * UPDATE {tableName} SET DATA_STATE=1 WHERE ID IN ({ids}) DATA_STATE=0
     *
     * @param ids 待删除的主键编号
     * @param errorOnUnaffected 受影响行数为0时是否抛异常
     * @return 删除行数
     * @throws ServiceException 删除失败
     */
    int physicalDeleteByIds(List<String> ids, boolean errorOnUnaffected) throws ServiceException;

    /**
     * 根据条件批量删除实体对象(物理删除)<br>
     * 注意: 默认查询条件由entityFillExecutor添加, 默认只处理有效项<br>
     * UPDATE {tableName} SET DATA_STATE=1 WHERE {whereConditions} AND DATA_STATE=0
     * 
     * @param where 匹配条件, 如果要删除全部记录应传入DbWhere.NONE
     * @param fillUpdateParams 是否自动填充更新参数(修改人/修改时间等)
     * @param errorOnUnaffected 受影响行数为0时是否抛异常
     * @return 受影响行数
     * @throws ServiceException 操作失败
     */
    int physicalDelete(T where, boolean errorOnUnaffected) throws ServiceException;

    /**
     * 根据条件批量删除实体对象(物理删除)<br>
     * 注意: 默认查询条件由entityFillExecutor添加, 默认只处理有效项<br>
     * UPDATE {tableName} SET DATA_STATE=1 WHERE {whereConditions} AND DATA_STATE=0
     * 
     * @param where 匹配条件, 如果要删除全部记录应传入DbWhere.NONE
     * @param errorOnUnaffected 受影响行数为0时是否抛异常
     * @param fillUpdateParams 是否自动填充更新参数(修改人/修改时间等)
     * @return 受影响行数
     * @throws ServiceException 操作失败
     */
    int physicalDelete(DbWhere where, boolean errorOnUnaffected) throws ServiceException;
}
	