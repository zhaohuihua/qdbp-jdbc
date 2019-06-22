package com.gitee.qdbp.jdbc.plugins;

/**
 * 名称转换处理接口<br>
 * 每个项目的处理方式不一样, 抽象成接口由各项目提供实现类
 *
 * @author zhaohuihua
 * @version 190601
 */
public interface NameConverter {

    /** BeanName转换为表名 **/
    String beanNameToTableName(String beanName);

    /** 表名转换为BeanName **/
    String tableNameToBeanName(String tableName);

    /** 字段名转换为列名 **/
    String fieldNameToColumnName(String fieldName);

    /** 列名转换为字段名 **/
    String columnNameToFieldName(String columnName);

}
