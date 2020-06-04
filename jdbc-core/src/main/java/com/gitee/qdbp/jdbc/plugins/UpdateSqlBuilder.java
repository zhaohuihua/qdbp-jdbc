package com.gitee.qdbp.jdbc.plugins;

import com.gitee.qdbp.able.jdbc.base.UpdateCondition;
import com.gitee.qdbp.jdbc.exception.UnsupportedFieldException;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.fragment.QueryFragmentHelper;

/**
 * Update SQL Builder
 *
 * @author zhaohuihua
 * @version 190601
 */
public interface UpdateSqlBuilder<T extends UpdateCondition> {

    /**
     * 获取支持的条件类型
     * 
     * @return 条件类型
     */
    Class<T> supported();

    /**
     * 生成Update SQL语句
     * 
     * @param condition 条件
     * @param sqlBuilder SQL生成参数
     * @return SQL语句
     * @throws UnsupportedFieldException
     */
    SqlBuffer buildSql(T condition, QueryFragmentHelper sqlBuilder) throws UnsupportedFieldException;

}
