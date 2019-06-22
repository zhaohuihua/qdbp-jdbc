package com.gitee.qdbp.jdbc.plugins;

import java.util.List;
import com.gitee.qdbp.jdbc.model.SimpleFieldColumn;
import com.gitee.qdbp.jdbc.model.PrimaryKeyFieldColumn;

/**
 * 扫描数据表和列信息<br>
 * 每个项目的处理方式不一样, 抽象成接口由各项目提供实现类
 *
 * @author zhaohuihua
 * @version 190601
 */
public interface TableInfoScans {

    /**
     * 扫描表名信息
     * 
     * @param clazz 类名
     * @return 表名
     */
    String scanTableName(Class<?> clazz);

    /**
     * 扫描主键信息
     * 
     * @param clazz 类名
     * @return 主键
     */
    PrimaryKeyFieldColumn scanPrimaryKey(Class<?> clazz);

    /**
     * 扫描字段名和数据库列名的映射表
     * 
     * @param clazz 类型
     * @return ColumnInfo: fieldName - columnName
     */
    List<SimpleFieldColumn> scanColumnList(Class<?> clazz);

}
