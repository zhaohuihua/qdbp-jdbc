package com.gitee.qdbp.jdbc.plugins.impl;

import java.util.Date;
import java.util.Map;
import com.gitee.qdbp.jdbc.condition.DbUpdate;
import com.gitee.qdbp.jdbc.condition.DbWhere;
import com.gitee.qdbp.jdbc.model.AllFieldColumn;
import com.gitee.qdbp.tools.utils.RandomTools;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 默认实体类业务处理类
 *
 * @author zhaohuihua
 * @version 190602
 */
public class SimpleModelDataHandler<DS> extends BaseModelDataHandler {

    /** 逻辑删除字段名 **/
    private String logicalDeleteField;
    /** 数据有效标记 **/
    private DS dataEffectiveFlag;
    /** 数据无效标记 **/
    private DS dataIneffectiveFlag;
    /** 创建时间字段名 **/
    private String createTimeField;
    /** 修改时间字段名 **/
    private String updateTimeField;
    /** 创建用户字段名 **/
    private String createUserField;
    /** 修改用户字段名 **/
    private String updateUserField;

    /** 逻辑删除字段名 **/
    public String getLogicalDeleteField() {
        return logicalDeleteField;
    }

    /** 逻辑删除字段名 **/
    public void setLogicalDeleteField(String logicalDeleteField) {
        this.logicalDeleteField = logicalDeleteField;
    }

    /** 数据有效标记 **/
    public DS getDataEffectiveFlag() {
        return dataEffectiveFlag;
    }

    /** 数据有效标记 **/
    public void setDataEffectiveFlag(DS dataEffectiveFlag) {
        this.dataEffectiveFlag = dataEffectiveFlag;
    }

    /** 数据无效标记 **/
    public DS getDataIneffectiveFlag() {
        return dataIneffectiveFlag;
    }

    /** 数据无效标记 **/
    public void setDataIneffectiveFlag(DS dataIneffectiveFlag) {
        this.dataIneffectiveFlag = dataIneffectiveFlag;
    }

    /** 创建时间字段名 **/
    public String getCreateTimeField() {
        return createTimeField;
    }

    /** 创建时间字段名 **/
    public void setCreateTimeField(String createTimeField) {
        this.createTimeField = createTimeField;
    }

    /** 修改时间字段名 **/
    public String getUpdateTimeField() {
        return updateTimeField;
    }

    /** 修改时间字段名 **/
    public void setUpdateTimeField(String updateTimeField) {
        this.updateTimeField = updateTimeField;
    }

    /** 创建用户字段名 **/
    public String getCreateUserField() {
        return createUserField;
    }

    /** 创建用户字段名 **/
    public void setCreateUserField(String createUserField) {
        this.createUserField = createUserField;
    }

    /** 修改用户字段名 **/
    public String getUpdateUserField() {
        return updateUserField;
    }

    /** 修改用户字段名 **/
    public void setUpdateUserField(String updateUserField) {
        this.updateUserField = updateUserField;
    }

    /** 获取当前登录账号, 一般是UserId **/
    protected Object getLoginAccount() {
        return null;
    }

    /** {@inheritDoc} **/
    @Override
    public String generatePrimaryKeyCode(String tableName) {
        return RandomTools.generateUuid();
    }

    /** {@inheritDoc} **/
    @Override
    public boolean supportedTableLogicalDelete(AllFieldColumn<?> allFields) {
        if (VerifyTools.isBlank(logicalDeleteField)) {
            return false;
        } else {
            return allFields.containsByFieldName(logicalDeleteField);
        }
    }

    /** {@inheritDoc} **/
    @Override
    public void fillQueryWhereDataStatus(DbWhere where, String tableAlias, AllFieldColumn<?> allFields) {
        fillValueIfAbsent(where, logicalDeleteField, tableAlias, dataEffectiveFlag, allFields);
    }

    /** {@inheritDoc} **/
    @Override
    public void fillTableWhereDataStatus(DbWhere where, AllFieldColumn<?> allFields) {
        fillValueIfAbsent(where, logicalDeleteField, dataEffectiveFlag, allFields);
    }

    /** {@inheritDoc} **/
    @Override
    public void fillTableCreateDataStatus(Map<String, Object> condition, AllFieldColumn<?> allFields) {
        fillValueIfAbsent(condition, logicalDeleteField, dataEffectiveFlag, allFields);
    }

    /** {@inheritDoc} **/
    @Override
    public void fillTableUpdateDataStatus(DbUpdate ud, AllFieldColumn<?> allFields) {
        // 修改时不涉及数据状态的处理
    }

    /** {@inheritDoc} **/
    @Override
    public void fillTableLogicalDeleteDataStatus(DbUpdate ud, AllFieldColumn<?> allFields) {
        // 将数据状态设置为无效
        fillValueIfAbsent(ud, logicalDeleteField, dataIneffectiveFlag, allFields);
    }

    /** {@inheritDoc} **/
    @Override
    public void fillTableCreteParams(Map<String, Object> model, AllFieldColumn<?> allFields) {
        Date now = new Date();
        fillValueIfAbsent(model, createTimeField, now, allFields);
        fillValueIfAbsent(model, updateTimeField, now, allFields);

        Object account = getLoginAccount();
        fillValueIfAbsent(model, createUserField, account, allFields);
        fillValueIfAbsent(model, updateUserField, account, allFields);
    }

    /** {@inheritDoc} **/
    @Override
    public void fillTableUpdateParams(Map<String, Object> model, AllFieldColumn<?> allFields) {
        Date now = new Date();
        fillValueIfAbsent(model, updateTimeField, now, allFields);

        Object account = getLoginAccount();
        fillValueIfAbsent(model, updateUserField, account, allFields);
    }

    /** {@inheritDoc} **/
    @Override
    public void fillTableUpdateParams(DbUpdate ud, AllFieldColumn<?> allFields) {
        Date now = new Date();
        fillValueIfAbsent(ud, updateTimeField, now, allFields);

        Object account = getLoginAccount();
        fillValueIfAbsent(ud, updateUserField, account, allFields);
    }

}
