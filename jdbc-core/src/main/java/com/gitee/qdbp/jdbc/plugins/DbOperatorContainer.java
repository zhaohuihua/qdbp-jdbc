package com.gitee.qdbp.jdbc.plugins;

import com.gitee.qdbp.jdbc.operator.DbBaseOperator;

/**
 * 运算符容器
 *
 * @author zhaohuihua
 * @version 200129
 */
public interface DbOperatorContainer {

    /** Where条件运算符 **/
    DbBaseOperator getWhereOperator(String operatorType);

    /** Update语句运算符 **/
    DbBaseOperator getUpdateOperator(String operatorType);
}
