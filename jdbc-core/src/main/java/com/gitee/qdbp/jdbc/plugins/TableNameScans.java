package com.gitee.qdbp.jdbc.plugins;

/**
 * 表名扫描类
 *
 * @author zhaohuihua
 * @version 190727
 */
public interface TableNameScans {

    /**
     * 扫描表名信息
     * 
     * @param clazz 类名
     * @return 表名
     */
    String scanTableName(Class<?> clazz);
}
