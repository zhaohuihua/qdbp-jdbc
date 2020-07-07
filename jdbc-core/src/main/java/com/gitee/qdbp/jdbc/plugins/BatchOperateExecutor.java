package com.gitee.qdbp.jdbc.plugins;

import java.util.List;
import com.gitee.qdbp.able.exception.ServiceException;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.able.jdbc.model.PkEntity;
import com.gitee.qdbp.able.jdbc.model.PkUpdate;
import com.gitee.qdbp.jdbc.api.SqlBufferJdbcOperations;
import com.gitee.qdbp.jdbc.sql.build.CrudSqlBuilder;

/**
 * 批量操作的处理类接口
 *
 * @author zhaohuihua
 * @version 20200705
 */
public interface BatchOperateExecutor {

    /**
     * 批量保存实体对象
     * 
     * @param entities 实体对象列表
     * @param jdbcOperations SqlBuffer数据库操作类
     * @param sqlBuilder 单表增删改查SQL生成工具
     * @return 返回主键编号
     * @throws ServiceException 操作失败
     */
    List<String> inserts(List<PkEntity> entities, SqlBufferJdbcOperations jdbcOperations, CrudSqlBuilder sqlBuilder)
            throws ServiceException;

    /**
     * 根据主键编号批量更新实体对象
     * 
     * @param contents 待更新的内容
     * @param commonWhere 除ID外的公共过滤条件, 如果没有公共过滤条件应传入DbWhere.NONE
     * @param jdbcOperations SqlBuffer数据库操作类
     * @param sqlBuilder 单表增删改查SQL生成工具
     * @return 受影响行数(某些实现类可能无法获取到准确的受影响行数)
     * @throws ServiceException 操作失败
     */
    int updates(List<PkUpdate> contents, DbWhere commonWhere, SqlBufferJdbcOperations jdbcOperations,
            CrudSqlBuilder sqlBuilder) throws ServiceException;
}
