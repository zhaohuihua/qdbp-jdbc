package com.gitee.qdbp.jdbc.plugins;

/**
 * 数据转换处理类, 转换为数据库可以存储的格式
 *
 * @author zhaohuihua
 * @version 190703
 */
public interface VariableToDbValueConverter {

    /**
     * 将变量转换为数据库可以存储的格式
     * 
     * @param variable 变量
     * @return 转换后的字段值, 一般是Boolean/Character/Date/Number/String之一
     */
    Object variableToDbValue(Object variable);
}
