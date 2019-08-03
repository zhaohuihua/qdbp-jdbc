package com.gitee.qdbp.jdbc.plugins;

/**
 * 将变量转换为可插入数据库的字段值<br>
 * 返回值只能是Boolean/Character/Date/Number/String之一
 *
 * @author zhaohuihua
 * @version 190703
 */
public interface VariableHelper {

    /**
     * 在这里可以根据枚举类型决定转换为ordinal()还是name()<br>
     * 也就是说在同一个系统中同一个枚举类型的转换方式是相同的
     * 
     * @param variable 变量
     * @return 转换后的字段值, Boolean/Character/Date/Number/String之一
     */
    Object variableToDbValue(Enum<?> variable);

    /**
     * 在这里可以根据变量类型决定转换方式<br>
     * 也就是说在同一个系统中同一个变量类型的转换方式是相同的
     * 
     * @param variable 变量
     * @return 转换后的字段值, Boolean/Character/Date/Number/String之一
     */
    Object variableToDbValue(Object variable);
}
