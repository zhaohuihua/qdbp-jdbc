package com.gitee.qdbp.jdbc.plugins;

import com.gitee.qdbp.able.jdbc.base.WhereCondition;
import com.gitee.qdbp.jdbc.exception.UnsupportedFieldException;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.fragment.QueryFragmentHelper;

/**
 * Where SQL Builder
 *
 * @author zhaohuihua
 * @version 190601
 */
public interface WhereSqlBuilder<T extends WhereCondition> {

    /**
     * 获取支持的条件类型
     * 
     * @return 条件类型
     */
    Class<T> supported();

    /**
     * 生成Where SQL语句
     * 
     * @param condition 条件
     * @param sqlHelper SQL片段生成帮助类
     * @return SQL语句
     * @throws UnsupportedFieldException
     */
    SqlBuffer buildSql(T condition, QueryFragmentHelper sqlHelper) throws UnsupportedFieldException;

}
