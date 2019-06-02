package com.gitee.qdbp.jdbc.plugins;

/**
 * SQL格式化接口
 *
 * @author zhaohuihua
 * @version 190601
 */
public interface SqlFormatter {

    /**
     * 格式化SQL语句
     * 
     * @param sql 待格式化的SQL语句
     * @return 已格式化的SQL语句
     */
    String format(String sql);

    /**
     * 格式化SQL语句
     * 
     * @param sql 待格式化的SQL语句
     * @param indent 缩进层数
     * @return 已格式化的SQL语句
     */
    String format(String sql, int indent);
}
