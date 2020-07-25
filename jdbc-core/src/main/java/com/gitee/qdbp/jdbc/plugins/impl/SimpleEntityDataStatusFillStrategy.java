package com.gitee.qdbp.jdbc.plugins.impl;

import java.util.Map;
import com.gitee.qdbp.able.jdbc.condition.DbUpdate;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.able.jdbc.utils.FieldTools;
import com.gitee.qdbp.jdbc.model.AllFieldColumn;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 简单的实体逻辑删除数据状态填充策略
 *
 * @author zhaohuihua
 * @version 20200725
 */
public class SimpleEntityDataStatusFillStrategy<DS> extends BaseEntityDataStatusFillStrategy<DS> {

    /** {@inheritDoc} **/
    @Override
    public void fillQueryWhereDataStatus(DbWhere where, String tableAlias, AllFieldColumn<?> allFields) {
        String fullFieldName = FieldTools.toTableFieldName(getLogicalDeleteField(), tableAlias);
        // 将数据状态设置为有效
        EntityFillTools.fillValueIfAbsent(where, fullFieldName, getDataEffectiveFlag(), allFields);
    }

    /** {@inheritDoc} **/
    @Override
    public void fillUpdateWhereDataStatus(DbWhere where, AllFieldColumn<?> allFields) {
        // 将数据状态设置为有效
        EntityFillTools.fillValueIfAbsent(where, getLogicalDeleteField(), getDataEffectiveFlag(), allFields);
    }

    /** {@inheritDoc} **/
    @Override
    public void fillDeleteWhereDataStatus(DbWhere where, AllFieldColumn<?> allFields) {
        // 将数据状态设置为有效
        EntityFillTools.fillValueIfAbsent(where, getLogicalDeleteField(), getDataEffectiveFlag(), allFields);
    }

    /** {@inheritDoc} **/
    @Override
    public void fillEntityCreateDataStatus(Map<String, Object> condition, AllFieldColumn<?> allFields) {
        // 将数据状态设置为有效
        EntityFillTools.fillValueIfAbsent(condition, getLogicalDeleteField(), getDataEffectiveFlag(), allFields);
    }

    /** {@inheritDoc} **/
    @Override
    public void fillLogicalDeleteDataStatus(Map<String, Object> entity, AllFieldColumn<?> allFields) {
        String fieldName = getLogicalDeleteField();
        DS fieldValue = getDataIneffectiveFlag();
        if (entity == null || VerifyTools.isBlank(fieldName) || VerifyTools.isBlank(fieldValue)) {
            return;
        }
        // 将数据状态设置为无效
        entity.put(fieldName, fieldValue);
    }

    /** {@inheritDoc} **/
    @Override
    public void fillLogicalDeleteDataStatus(DbUpdate ud, AllFieldColumn<?> allFields) {
        String fieldName = getLogicalDeleteField();
        DS fieldValue = getDataIneffectiveFlag();
        if (ud == null || VerifyTools.isBlank(fieldName) || VerifyTools.isBlank(fieldValue)) {
            return;
        }
        // 将数据状态设置为无效
        EntityFillTools.fillValueIfAbsent(ud, fieldName, fieldValue, allFields);
        this.putLogicalDeleteDataStatus(ud, fieldName, fieldValue);
    }
}
