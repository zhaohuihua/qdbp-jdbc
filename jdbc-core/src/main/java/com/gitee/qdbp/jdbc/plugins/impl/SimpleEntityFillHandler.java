package com.gitee.qdbp.jdbc.plugins.impl;

import java.util.Date;
import java.util.Map;
import com.gitee.qdbp.able.jdbc.condition.DbUpdate;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.jdbc.model.AllFieldColumn;
import com.gitee.qdbp.jdbc.plugins.EntityFillBizResolver;
import com.gitee.qdbp.tools.utils.RandomTools;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 默认实体数据填充业务处理类<br>
 * 自动填充逻辑删除/创建人/创建时间/修改人/修改时间字段
 *
 * @author zhaohuihua
 * @version 190602
 */
public class SimpleEntityFillHandler<DS> extends BaseEntityFillHandler {

    /** 逻辑删除字段名 **/
    private String logicalDeleteField;
    /** 逻辑删除时是否使用随机数标记数据状态: 0=不使用, 大于0表示随机数的位数 **/
    private int logicalDeleteRandoms;
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
    /** 与业务强相关的数据提供者 **/
    private EntityFillBizResolver entityFillBizResolver;

    /** 逻辑删除字段名 **/
    public String getLogicalDeleteField() {
        return logicalDeleteField;
    }

    /** 逻辑删除字段名 **/
    public void setLogicalDeleteField(String logicalDeleteField) {
        this.logicalDeleteField = logicalDeleteField;
    }

    /** 逻辑删除时是否使用随机数标记数据状态: 0=不使用, 大于0表示随机数的位数 **/
    public int getLogicalDeleteRandoms() {
        return logicalDeleteRandoms;
    }

    /** 逻辑删除时是否使用随机数标记数据状态: 0=不使用, 大于0表示随机数的位数 **/
    public void setLogicalDeleteRandoms(int logicalDeleteRandoms) {
        this.logicalDeleteRandoms = logicalDeleteRandoms;
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

    /** 与业务强相关的数据提供者 **/
    public EntityFillBizResolver getEntityFillBizResolver() {
        return entityFillBizResolver;
    }

    /** 与业务强相关的数据提供者 **/
    public void setEntityFillBizResolver(EntityFillBizResolver entityFillBizResolver) {
        this.entityFillBizResolver = entityFillBizResolver;
    }

    /** {@inheritDoc} **/
    @Override
    public String getLoginAccount() {
        if (entityFillBizResolver == null) {
            return null;
        } else {
            return entityFillBizResolver.getLoginAccount();
        }
    }

    /** {@inheritDoc} **/
    @Override
    public String generatePrimaryKeyCode(String tableName) {
        if (entityFillBizResolver == null) {
            return RandomTools.generateUuid();
        } else {
            return entityFillBizResolver.generatePrimaryKeyCode(tableName);
        }
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
    public void fillQueryWhereParams(DbWhere where, String tableAlias, AllFieldColumn<?> allFields) {
        // 无默认操作
    }

    /** {@inheritDoc} **/
    @Override
    public void fillUpdateWhereParams(DbWhere where, AllFieldColumn<?> allFields) {
        // 无默认操作
    }

    /** {@inheritDoc} **/
    @Override
    public void fillDeleteWhereParams(DbWhere where, AllFieldColumn<?> allFields) {
        // 无默认操作
    }

    /** {@inheritDoc} **/
    @Override
    public void fillQueryWhereDataStatus(DbWhere where, String tableAlias, AllFieldColumn<?> allFields) {
        fillValueIfAbsent(where, logicalDeleteField, tableAlias, dataEffectiveFlag, allFields);
    }

    /** {@inheritDoc} **/
    @Override
    public void fillUpdateWhereDataStatus(DbWhere where, AllFieldColumn<?> allFields) {
        fillValueIfAbsent(where, logicalDeleteField, dataEffectiveFlag, allFields);
    }

    /** {@inheritDoc} **/
    @Override
    public void fillDeleteWhereDataStatus(DbWhere where, AllFieldColumn<?> allFields) {
        fillValueIfAbsent(where, logicalDeleteField, dataEffectiveFlag, allFields);
    }

    /** {@inheritDoc} **/
    @Override
    public void fillEntityCreateDataStatus(Map<String, Object> condition, AllFieldColumn<?> allFields) {
        fillValueIfAbsent(condition, logicalDeleteField, dataEffectiveFlag, allFields);
    }

    /** {@inheritDoc} **/
    @Override
    public void fillEntityCreateParams(Map<String, Object> model, AllFieldColumn<?> allFields) {
        Date now = new Date();
        fillValueIfAbsent(model, createTimeField, now, allFields);
        fillValueIfAbsent(model, updateTimeField, now, allFields);

        String account = getLoginAccount();
        fillValueIfAbsent(model, createUserField, account, allFields);
        fillValueIfAbsent(model, updateUserField, account, allFields);
    }

    /** {@inheritDoc} **/
    @Override
    public void fillEntityUpdateParams(Map<String, Object> model, AllFieldColumn<?> allFields) {
        Date now = new Date();
        fillValueIfAbsent(model, updateTimeField, now, allFields);

        String account = getLoginAccount();
        fillValueIfAbsent(model, updateUserField, account, allFields);
    }

    /** {@inheritDoc} **/
    @Override
    public void fillEntityUpdateParams(DbUpdate ud, AllFieldColumn<?> allFields) {
        Date now = new Date();
        fillValueIfAbsent(ud, updateTimeField, now, allFields);

        Object account = getLoginAccount();
        fillValueIfAbsent(ud, updateUserField, account, allFields);
    }

    /** {@inheritDoc} **/
    @Override
    public void fillLogicalDeleteParams(Map<String, Object> model, AllFieldColumn<?> allFields) {
        this.fillEntityUpdateParams(model, allFields);
    }

    /** {@inheritDoc} **/
    @Override
    public void fillLogicalDeleteParams(DbUpdate ud, AllFieldColumn<?> allFields) {
        this.fillEntityUpdateParams(ud, allFields);
    }

    /** {@inheritDoc} **/
    @Override
    public void fillLogicalDeleteDataStatus(Map<String, Object> model, AllFieldColumn<?> allFields) {
        Object ineffectiveFlag = dataIneffectiveFlag;
        if (logicalDeleteRandoms > 0) {
            ineffectiveFlag = RandomTools.generateNumber(logicalDeleteRandoms);
        }
        // 将数据状态设置为无效
        fillValueIfAbsent(model, logicalDeleteField, ineffectiveFlag, allFields);
    }

    /** {@inheritDoc} **/
    @Override
    public void fillLogicalDeleteDataStatus(DbUpdate ud, AllFieldColumn<?> allFields) {
        Object ineffectiveFlag = dataIneffectiveFlag;
        if (logicalDeleteRandoms > 0) {
            ineffectiveFlag = RandomTools.generateNumber(logicalDeleteRandoms);
        }
        // 将数据状态设置为无效
        fillValueIfAbsent(ud, logicalDeleteField, ineffectiveFlag, allFields);
    }

}
