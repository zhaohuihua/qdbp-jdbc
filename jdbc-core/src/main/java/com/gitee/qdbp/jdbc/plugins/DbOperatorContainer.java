package com.gitee.qdbp.jdbc.plugins;

import com.gitee.qdbp.jdbc.operator.DbBaseOperator;

public interface DbOperatorContainer {

    DbBaseOperator getWhereOperator(String operatorType);

    DbBaseOperator getUpdateOperator(String operatorType);
}
