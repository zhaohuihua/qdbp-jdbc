package com.gitee.qdbp.jdbc.plugins.impl;

import com.gitee.qdbp.jdbc.plugins.VariableHelper;

/**
 * VariableHelper简单实现类
 *
 * @author zhaohuihua
 * @version 190705
 */
public class SimpleVariableHelper implements VariableHelper {

    @Override
    public Object variableToDbValue(Enum<?> variable) {
        return variable.ordinal();
    }

    @Override
    public Object variableToDbValue(Object variable) {
        return variable.toString();
    }

}
