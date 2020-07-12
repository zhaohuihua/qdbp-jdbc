package com.gitee.qdbp.jdbc.plugins;

import java.util.List;
import com.gitee.qdbp.able.exception.ServiceException;
import com.gitee.qdbp.able.jdbc.model.PkEntity;
import com.gitee.qdbp.jdbc.api.SqlBufferJdbcOperations;
import com.gitee.qdbp.jdbc.model.DbVersion;
import com.gitee.qdbp.jdbc.sql.build.CrudSqlBuilder;

/**
 * 批量更新的处理类接口
 *
 * @author zhaohuihua
 * @version 20200705
 */
public interface BatchUpdateExecutor {

    /**
     * 是否支持指定数据库
     * 
     * @param version 数据库类型及版本信息
     * @return 是否支持
     */
    boolean supports(DbVersion version);

    /**
     * 根据主键编号批量更新实体对象
     * 
     * @param entities 待更新的内容
     * @param jdbcOperations SqlBuffer数据库操作类
     * @param sqlBuilder 单表增删改查SQL生成工具
     * @return 受影响行数(某些实现类可能无法获取到准确的受影响行数)
     * @throws ServiceException 操作失败
     */
    int updates(List<PkEntity> entities, SqlBufferJdbcOperations jdbcOperations,
            CrudSqlBuilder sqlBuilder) throws ServiceException;
}
