package com.gitee.qdbp.jdbc.plugins.impl;

import java.util.List;
import java.util.Map;
import com.gitee.qdbp.able.jdbc.base.DbCondition;
import com.gitee.qdbp.able.jdbc.condition.DbField;
import com.gitee.qdbp.able.jdbc.condition.DbUpdate;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.able.jdbc.utils.FieldTools;
import com.gitee.qdbp.jdbc.model.AllFieldColumn;
import com.gitee.qdbp.jdbc.operator.DbBaseOperator;
import com.gitee.qdbp.jdbc.utils.DbTools;
import com.gitee.qdbp.tools.utils.RandomTools;

/**
 * 使用随机数标记已删除状态的实体逻辑删除数据状态填充策略
 *
 * @author zhaohuihua
 * @version 20200725
 */
public class RandomNumberEntityDataStateFillStrategy<DS> extends BaseEntityDataStateFillStrategy<DS> {

    /** 是否使用随机数标记已删除状态: 0=不使用, 大于0表示随机数的位数 **/
    private int logicalDeleteRandoms;

    /** 是否使用随机数标记已删除状态: 0=不使用, 大于0表示随机数的位数 **/
    public int getLogicalDeleteRandoms() {
        return logicalDeleteRandoms;
    }

    /** 是否使用随机数标记已删除状态 **/
    public boolean useLogicalDeleteRandoms() {
        return logicalDeleteRandoms > 0;
    }

    /** 是否使用随机数标记已删除状态: 0=不使用, 大于0表示随机数的位数 **/
    public void setLogicalDeleteRandoms(int logicalDeleteRandoms) {
        if (logicalDeleteRandoms > 0 && logicalDeleteRandoms < 5) {
            // 如果使用随机数标记已删除状态, 那么这个位数至少必须大于4
            // 因为这个数字太小的话, 产生的随机数容易冲突. 建议设置为10位或更大的数值
            String msg = "If use the random number to mark the data state, this value must be greater than 4. "
                    + "Because the number is too small, the generated random numbers are prone to conflict. "
                    + "It is recommended to set it to 10 or greater.";
            throw new IllegalArgumentException(msg);
        }
        this.logicalDeleteRandoms = logicalDeleteRandoms;
    }

    protected String generateRandomNumber() {
        return RandomTools.generateNumber(logicalDeleteRandoms);
    }

    /** 处理逻辑删除时的数据状态 **/
    protected void handleLogicalDeleteDataState(Map<String, Object> entity, String fieldName,
            AllFieldColumn<?> allFields) {
        if (!allFields.containsByFieldName(fieldName)) {
            return; // 不支持逻辑删除
        }
        // 无条件替换为随机数
        String randomNumber = generateRandomNumber();
        entity.put(fieldName, randomNumber);
    }

    /** 处理逻辑删除时的数据状态 **/
    protected void handleLogicalDeleteDataState(DbUpdate ud, String fieldName, AllFieldColumn<?> allFields) {
        if (!allFields.containsByFieldName(fieldName)) {
            return; // 不支持逻辑删除
        }
        // 查找所有fieldName=logicalDeleteField的条件
        // 不管字段值和操作符是什么, 无条件替换为随机数
        String randomNumber = generateRandomNumber();
        this.putLogicalDeleteDataState(ud, fieldName, randomNumber);
    }

    /** 处理实体创建时的数据状态 **/
    protected void handleEntityCreateDataState(Map<String, Object> entity, String fieldName,
            AllFieldColumn<?> allFields) {
        if (!allFields.containsByFieldName(fieldName)) {
            return; // 不支持逻辑删除
        }
        // where中没有逻辑删除标记时, 默认设置为有效值, 即只查有效项
        if (!entity.containsKey(fieldName)) {
            // 没有逻辑删除标记, 默认设置为有效值
            entity.put(fieldName, getDataEffectiveFlag());
        } else { // 存在逻辑删除标记
            if (this.useLogicalDeleteRandoms()) {
                Object value = entity.get(fieldName);
                // 数据状态=无效标记, 替换为随机数
                if (value != null && value.equals(getDataIneffectiveFlag())) {
                    entity.put(fieldName, generateRandomNumber());
                }
            }
        }
    }

    /*** 处理Where条件中的数据状态 **/
    protected void handleWhereDataState(DbWhere where, String fullFieldName, AllFieldColumn<?> allFields) {
        if (!allFields.containsByFieldName(fullFieldName)) {
            return; // 不支持逻辑删除
        }
        // where中没有逻辑删除标记时, 默认设置为有效值, 即只查有效项
        if (!where.contains(fullFieldName)) {
            // 没有逻辑删除标记, 默认设置为有效值
            where.on(fullFieldName, "=", getDataEffectiveFlag());
        } else { // 存在逻辑删除标记
            if (this.useLogicalDeleteRandoms()) {
                // 使用随机数标记已删除状态
                // 将已删除条件(DataState.DELETED)替换为 dataState > maxUndeletedFlag
                replaceWhereDeletedDataState(where, fullFieldName);
            }
        }
    }

    /** 将已删除条件(DataState.DELETED)替换为 dataState > maxUndeletedFlag **/
    protected List<DbCondition> replaceWhereDeletedDataState(DbWhere where, String fullFieldName) {
        // 前提条件: 1.使用随机数标记已删除状态; 2.已删除状态的码值(DataState.DELETED)是最大的
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
        List<DbCondition> conditions = where.find(fullFieldName, getDataIneffectiveFlag());
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
            } else {
                // 不支持其他类型的条件
            }
        }
        return conditions;
    }

    /** {@inheritDoc} **/
    @Override
    public void fillQueryWhereDataState(DbWhere where, String tableAlias, AllFieldColumn<?> allFields) {
        String fullFieldName = FieldTools.toTableFieldName(getLogicalDeleteField(), tableAlias);
        handleWhereDataState(where, fullFieldName, allFields);
    }

    /** {@inheritDoc} **/
    @Override
    public void fillUpdateWhereDataState(DbWhere where, AllFieldColumn<?> allFields) {
        handleWhereDataState(where, getLogicalDeleteField(), allFields);
    }

    /** {@inheritDoc} **/
    @Override
    public void fillDeleteWhereDataState(DbWhere where, AllFieldColumn<?> allFields) {
        handleWhereDataState(where, getLogicalDeleteField(), allFields);
    }

    /** {@inheritDoc} **/
    @Override
    public void fillEntityCreateDataState(Map<String, Object> entity, AllFieldColumn<?> allFields) {
        handleEntityCreateDataState(entity, getLogicalDeleteField(), allFields);
    }

    /** {@inheritDoc} **/
    @Override
    public void fillLogicalDeleteDataState(Map<String, Object> entity, AllFieldColumn<?> allFields) {
        handleLogicalDeleteDataState(entity, getLogicalDeleteField(), allFields);
    }

    /** {@inheritDoc} **/
    @Override
    public void fillLogicalDeleteDataState(DbUpdate ud, AllFieldColumn<?> allFields) {
        Object ineffectiveFlag = getDataIneffectiveFlag();
        if (logicalDeleteRandoms > 0) {
            ineffectiveFlag = RandomTools.generateNumber(logicalDeleteRandoms);
        }
        // 将数据状态设置为无效
        EntityFillTools.fillValueIfAbsent(ud, getLogicalDeleteField(), ineffectiveFlag, allFields);
    }
}
