package com.gitee.qdbp.jdbc.plugins.impl;

import com.gitee.qdbp.tools.utils.NamingTools;

/**
 * 名称转换处理器
 *
 * @author zhaohuihua
 * @version 190601
 */
public class SimpleNameConverter extends BaseNameConverter {

    @Override
    public String beanNameToTableName(String beanName) {
        return toUnderlineString(beanName);
    }

    @Override
    public String tableNameToBeanName(String tableName) {
        return NamingTools.toCamelString(tableName, true);
    }

    @Override
    public String fieldNameToColumnName(String fieldName) {
        return toUnderlineString(fieldName);
    }

    @Override
    public String columnNameToFieldName(String columnName) {
        return NamingTools.toCamelString(columnName, false);
    }

}
