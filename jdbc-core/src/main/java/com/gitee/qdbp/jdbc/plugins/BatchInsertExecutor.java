package com.gitee.qdbp.jdbc.plugins;

import java.util.List;
import com.gitee.qdbp.able.exception.ServiceException;
import com.gitee.qdbp.able.jdbc.model.PkEntity;
import com.gitee.qdbp.jdbc.api.SqlBufferJdbcOperations;
import com.gitee.qdbp.jdbc.model.DbVersion;
import com.gitee.qdbp.jdbc.sql.build.CrudSqlBuilder;

/**
 * 批量新增的处理类接口
 *
 * @author zhaohuihua
 * @version 20200705
 */
public interface BatchInsertExecutor {

    /**
     * 是否支持指定数据库
     * 
     * @param version 数据库类型及版本信息
     * @return 是否支持
     */
    boolean supports(DbVersion version);

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

}
