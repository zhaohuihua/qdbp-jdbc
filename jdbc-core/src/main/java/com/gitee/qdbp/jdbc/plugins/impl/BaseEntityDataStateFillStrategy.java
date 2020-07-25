package com.gitee.qdbp.jdbc.plugins.impl;

import java.util.List;
import com.gitee.qdbp.able.jdbc.base.DbCondition;
import com.gitee.qdbp.able.jdbc.condition.DbField;
import com.gitee.qdbp.able.jdbc.condition.DbUpdate;
import com.gitee.qdbp.jdbc.plugins.EntityDataStateFillStrategy;

/**
 * 实体的逻辑删除的数据状态填充策略基础类
 *
 * @author zhaohuihua
 * @version 20200725
 */
public abstract class BaseEntityDataStateFillStrategy<DS> implements EntityDataStateFillStrategy<DS> {

    /** 逻辑删除字段名 **/
    private String logicalDeleteField;
    /** 数据有效标记(正常状态) **/
    private DS dataEffectiveFlag;
    /** 数据无效标记(已删除状态) **/
    private DS dataIneffectiveFlag;

    /** 逻辑删除字段名 **/
    @Override
    public String getLogicalDeleteField() {
        return logicalDeleteField;
    }

    /** 逻辑删除字段名 **/
    public void setLogicalDeleteField(String logicalDeleteField) {
        this.logicalDeleteField = logicalDeleteField;
    }

    /** 数据有效标记(正常状态) **/
    @Override
    public DS getDataEffectiveFlag() {
        return dataEffectiveFlag;
    }

    /** 数据有效标记(正常状态) **/
    public void setDataEffectiveFlag(DS dataEffectiveFlag) {
        this.dataEffectiveFlag = dataEffectiveFlag;
    }

    /** 数据无效标记(已删除状态) **/
    @Override
    public DS getDataIneffectiveFlag() {
        return dataIneffectiveFlag;
    }

    /** 数据无效标记(已删除状态) **/
    public void setDataIneffectiveFlag(DS dataIneffectiveFlag) {
        this.dataIneffectiveFlag = dataIneffectiveFlag;
    }

    /** 处理逻辑删除时的数据状态 **/
    protected void putLogicalDeleteDataState(DbUpdate ud, String fieldName, Object dataIneffectiveFlag) {
        // 查找所有fieldName=logicalDeleteField的条件
        // 不管字段值和操作符是什么, 无条件替换为无效值
        List<DbCondition> conditions = ud.find(fieldName);
        if (conditions.isEmpty()) {
            ud.set(fieldName, dataIneffectiveFlag);
        } else {
            for (DbCondition item : conditions) {
                if (item instanceof DbField) {
                    DbField field = ((DbField) item);
                    field.setOperateType("=");
                    field.setFieldValue(dataIneffectiveFlag);
                } else {
                    // 不支持其他类型的条件
                }
            }
        }
    }
}
