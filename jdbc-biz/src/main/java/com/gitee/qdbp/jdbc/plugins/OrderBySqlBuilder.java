package com.gitee.qdbp.jdbc.plugins;

import com.gitee.qdbp.able.model.db.OrderByCondition;
import com.gitee.qdbp.jdbc.exception.UnsupportedFieldExeption;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.SqlBuilder;

/**
 * OrderBy SQL Builder
 *
 * @author zhaohuihua
 * @version 190601
 */
public interface OrderBySqlBuilder<T extends OrderByCondition> {

    /**
     * 获取支持的条件类型
     * 
     * @return 条件类型
     */
    Class<T> supported();

    /**
     * 生成OrderBy SQL语句
     * 
     * @param condition 条件
     * @param sqlBuilder SQL生成参数
     * @return SQL语句
     * @throws UnsupportedFieldExeption
     */
    SqlBuffer buildSql(T condition, SqlBuilder sqlBuilder) throws UnsupportedFieldExeption;
}
