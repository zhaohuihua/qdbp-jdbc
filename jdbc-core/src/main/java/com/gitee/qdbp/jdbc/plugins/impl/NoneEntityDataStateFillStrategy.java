package com.gitee.qdbp.jdbc.plugins.impl;

import java.util.Map;
import com.gitee.qdbp.able.jdbc.condition.DbUpdate;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.jdbc.model.AllFieldColumn;
import com.gitee.qdbp.jdbc.plugins.EntityDataStateFillStrategy;

/**
 * 空的逻辑删除的数据状态填充策略, 什么也不干
 *
 * @author zhaohuihua
 * @version 20200809
 */
public class NoneEntityDataStateFillStrategy implements EntityDataStateFillStrategy<Object> {

    @Override
    public String getLogicalDeleteField() {
        return null;
    }

    @Override
    public Object getDataEffectiveFlag() {
        return null;
    }

    @Override
    public Object getDataIneffectiveFlag() {
        return null;
    }

    @Override
    public void fillQueryWhereDataState(DbWhere where, String tableAlias, AllFieldColumn<?> allFields) {
    }

    @Override
    public void fillUpdateWhereDataState(DbWhere where, AllFieldColumn<?> allFields) {
    }

    @Override
    public void fillDeleteWhereDataState(DbWhere where, AllFieldColumn<?> allFields) {
    }

    @Override
    public void fillEntityCreateDataState(Map<String, Object> entity, AllFieldColumn<?> allFields) {
    }

    @Override
    public void fillLogicalDeleteDataState(Map<String, Object> entity, AllFieldColumn<?> allFields) {
    }

    @Override
    public void fillLogicalDeleteDataState(DbUpdate ud, AllFieldColumn<?> allFields) {
    }

}
