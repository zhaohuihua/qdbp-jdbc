package com.gitee.qdbp.jdbc.plugins.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import com.gitee.qdbp.able.jdbc.base.DbCondition;
import com.gitee.qdbp.able.jdbc.condition.DbField;
import com.gitee.qdbp.able.jdbc.condition.DbUpdate;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.able.jdbc.utils.FieldTools;
import com.gitee.qdbp.jdbc.model.AllFieldColumn;
import com.gitee.qdbp.jdbc.operator.DbBaseOperator;
import com.gitee.qdbp.jdbc.plugins.EntityFillBizResolver;
import com.gitee.qdbp.jdbc.utils.DbTools;
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
    // 如果使用随机数标记已删除记录, 那么如果除了正常和删除还有其他值, 一定要保证删除的码值是最大的
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
        if (logicalDeleteRandoms > 0 && logicalDeleteRandoms < 5) {
            // 如果使用随机数标记数据状态, 那么这个数值至少必须大于4
            // 因为这个数字太小的话, 产生的随机数容易冲突. 建议设置为8或10或更大的数值
            String msg = "If use the random number to mark the data state, this value must be greater than 4. "
                    + "Because the number is too small, the generated random numbers are prone to conflict. "
                    + "It is recommended to set it to 8 or 10 or greater.";
            throw new IllegalArgumentException(msg);
        }
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
        String fullFieldName = FieldTools.toTableFieldName(logicalDeleteField, tableAlias);
        handleWhereDataStatus(where, fullFieldName, allFields);
    }

    /** {@inheritDoc} **/
    @Override
    public void fillUpdateWhereDataStatus(DbWhere where, AllFieldColumn<?> allFields) {
        handleWhereDataStatus(where, logicalDeleteField, allFields);
    }

    /** {@inheritDoc} **/
    @Override
    public void fillDeleteWhereDataStatus(DbWhere where, AllFieldColumn<?> allFields) {
        handleWhereDataStatus(where, logicalDeleteField, allFields);
    }

    protected void handleWhereDataStatus(DbWhere where, String fullFieldName, AllFieldColumn<?> allFields) {
        if (!allFields.containsByFieldName(fullFieldName)) {
            return; // 不支持逻辑删除
        }
        // where中没有逻辑删除标记时, 默认设置为有效值, 即只查有效项
        if (!where.contains(fullFieldName)) {
            // 没有逻辑删除标记, 默认设置为有效值
            where.on(fullFieldName, "=", dataEffectiveFlag);
        } else { // 存在逻辑删除标记
            if (this.logicalDeleteRandoms > 0) {
                // 使用随机数标记数据状态
                // 将已删除条件(DataState.DELETED)替换为 dataState > maxUndeletedFlag
                replaceWhereDeletedDataStatus(where, fullFieldName);
            }
        }
    }

    /** 将已删除条件(DataState.DELETED)替换为 dataState > maxUndeletedFlag **/
    protected List<DbCondition> replaceWhereDeletedDataStatus(DbWhere where, String fullFieldName) {
        // 前提条件: 1.使用随机数标记数据状态; 2.已删除状态的码值(DataState.DELETED)是最大的
        DbBaseOperator equals = DbTools.getWhereOperator("="); // 只包含已删除的
        DbBaseOperator lessThen = DbTools.getWhereOperator("<"); // 不包含已删除的
        // 查不等于最大值的, 就是查小于最大值的
        DbBaseOperator notEquals = DbTools.getWhereOperator("!="); // 只包含已删除的(与lessThen相同)
        // 查最大的和小于最大的, 就是查所有的
        DbBaseOperator lessEquals = DbTools.getWhereOperator("<="); // 所有的
        // 查询比最大值还大的, 应该查不到记录
        DbBaseOperator greaterThen = DbTools.getWhereOperator(">"); // 查不到
        DbBaseOperator greaterEquals = DbTools.getWhereOperator(">="); // 只包含已删除的(与equals相同)
        // 没必要完全按logicalDeleteRandoms的最小值来计算, 只要大于等于5位数, 就是已删除
        // 也就是说可以用1~9999来表示正常和已删除之间的中间状态
        // long minDeletedFlag = (long) Math.pow(10, logicalDeleteRandoms - 1);
        long minDeletedFlag = 10000; // 最小的已删除码值
        long maxUndeletedFlag = minDeletedFlag - 1; // 最大的未删除码值
        // 查找所有fieldName=logicalDeleteField, 且fieldValue=dataIneffectiveFlag的条件
        List<DbCondition> conditions = where.find(fullFieldName, dataIneffectiveFlag);
        for (DbCondition item : conditions) {
            if (item instanceof DbField) {
                DbField field = ((DbField) item);
                String operator = field.getOperateType();
                if (equals.matchers(operator) || greaterEquals.matchers(operator)) {
                    // 等于DataState.DELETED, 即dataState > maxUndeletedFlag
                    field.setOperateType(">");
                    field.setFieldValue(maxUndeletedFlag);
                } else if (lessThen.matchers(operator) || notEquals.matchers(operator)) {
                    // 小于DataState.DELETED, 即dataState < minDeletedFlag
                    field.setOperateType("<");
                    field.setFieldValue(minDeletedFlag);
                } else if (lessEquals.matchers(operator)) {
                    // 小于等于DataState.DELETED, 查最大的和小于最大的, 就是查询所有记录
                    field.setOperateType(">=");
                    field.setFieldValue(0);
                } else if (greaterThen.matchers(operator)) {
                    // 大于DataState.DELETED, 查询比最大值还大的, 应该查不到记录
                    long maxDeletedFlag = (long) Math.pow(10, logicalDeleteRandoms) - 1;
                    field.setOperateType(">");
                    field.setFieldValue(maxDeletedFlag);
                } else {
                    // 不支持其他操作符, 如IN,LIKE
                }
            }
        }
        return conditions;
    }

    /** {@inheritDoc} **/
    @Override
    public void fillEntityCreateDataStatus(Map<String, Object> condition, AllFieldColumn<?> allFields) {
        fillValueIfAbsent(condition, logicalDeleteField, dataEffectiveFlag, allFields);
    }

    /** {@inheritDoc} **/
    @Override
    public void fillEntityCreateParams(Map<String, Object> entity, AllFieldColumn<?> allFields) {
        Date now = new Date();
        fillValueIfAbsent(entity, createTimeField, now, allFields);
        fillValueIfAbsent(entity, updateTimeField, now, allFields);

        String account = getLoginAccount();
        fillValueIfAbsent(entity, createUserField, account, allFields);
        fillValueIfAbsent(entity, updateUserField, account, allFields);
    }

    /** {@inheritDoc} **/
    @Override
    public void fillEntityUpdateParams(Map<String, Object> entity, AllFieldColumn<?> allFields) {
        Date now = new Date();
        fillValueIfAbsent(entity, updateTimeField, now, allFields);

        String account = getLoginAccount();
        fillValueIfAbsent(entity, updateUserField, account, allFields);
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
    public void fillLogicalDeleteParams(Map<String, Object> entity, AllFieldColumn<?> allFields) {
        this.fillEntityUpdateParams(entity, allFields);
    }

    /** {@inheritDoc} **/
    @Override
    public void fillLogicalDeleteParams(DbUpdate ud, AllFieldColumn<?> allFields) {
        this.fillEntityUpdateParams(ud, allFields);
    }

    /** {@inheritDoc} **/
    @Override
    public void fillLogicalDeleteDataStatus(Map<String, Object> entity, AllFieldColumn<?> allFields) {
        Object ineffectiveFlag = dataIneffectiveFlag;
        if (logicalDeleteRandoms > 0) {
            ineffectiveFlag = RandomTools.generateNumber(logicalDeleteRandoms);
        }
        // 将数据状态设置为无效
        fillValueIfAbsent(entity, logicalDeleteField, ineffectiveFlag, allFields);
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
