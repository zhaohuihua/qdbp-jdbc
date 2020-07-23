package com.gitee.qdbp.jdbc.plugins.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.gitee.qdbp.jdbc.operator.DbBaseOperator;
import com.gitee.qdbp.jdbc.operator.update.DbBinaryAddOperator;
import com.gitee.qdbp.jdbc.operator.update.DbBinarySetOperator;
import com.gitee.qdbp.jdbc.operator.update.DbUnaryToNullOperator;
import com.gitee.qdbp.jdbc.operator.where.DbBinaryEndsWithOperator;
import com.gitee.qdbp.jdbc.operator.where.DbBinaryEqualsOperator;
import com.gitee.qdbp.jdbc.operator.where.DbBinaryGreaterEqualsThenOperator;
import com.gitee.qdbp.jdbc.operator.where.DbBinaryGreaterThenOperator;
import com.gitee.qdbp.jdbc.operator.where.DbBinaryLessEqualsThenOperator;
import com.gitee.qdbp.jdbc.operator.where.DbBinaryLessThenOperator;
import com.gitee.qdbp.jdbc.operator.where.DbBinaryLikeOperator;
import com.gitee.qdbp.jdbc.operator.where.DbBinaryNotEqualsOperator;
import com.gitee.qdbp.jdbc.operator.where.DbBinaryNotLikeOperator;
import com.gitee.qdbp.jdbc.operator.where.DbBinaryStartsWithOperator;
import com.gitee.qdbp.jdbc.operator.where.DbMultivariateInOperator;
import com.gitee.qdbp.jdbc.operator.where.DbMultivariateNotInOperator;
import com.gitee.qdbp.jdbc.operator.where.DbTernaryBetweenOperator;
import com.gitee.qdbp.jdbc.operator.where.DbTernaryNotBetweenOperator;
import com.gitee.qdbp.jdbc.operator.where.DbUnaryIsNotNullOperator;
import com.gitee.qdbp.jdbc.operator.where.DbUnaryIsNullOperator;
import com.gitee.qdbp.jdbc.plugins.DbOperatorContainer;

/**
 * 运算符容器
 *
 * @author zhaohuihua
 * @version 20200126
 */
public class SimpleDbOperatorContainer implements DbOperatorContainer {

    private Map<String, DbBaseOperator> whereOperators = new HashMap<>();
    private Map<String, DbBaseOperator> updateOperators = new HashMap<>();

    public SimpleDbOperatorContainer() {
        // 注册Where条件的运算符
        registerWhereOperator(new DbUnaryIsNullOperator());
        registerWhereOperator(new DbUnaryIsNotNullOperator());
        registerWhereOperator(new DbBinaryEqualsOperator());
        registerWhereOperator(new DbBinaryNotEqualsOperator());
        registerWhereOperator(new DbBinaryGreaterThenOperator());
        registerWhereOperator(new DbBinaryGreaterEqualsThenOperator());
        registerWhereOperator(new DbBinaryGreaterThenOperator());
        registerWhereOperator(new DbBinaryLessEqualsThenOperator());
        registerWhereOperator(new DbBinaryLessThenOperator());
        registerWhereOperator(new DbBinaryLikeOperator());
        registerWhereOperator(new DbBinaryNotLikeOperator());
        registerWhereOperator(new DbBinaryStartsWithOperator());
        registerWhereOperator(new DbBinaryEndsWithOperator());
        registerWhereOperator(new DbTernaryBetweenOperator());
        registerWhereOperator(new DbTernaryNotBetweenOperator());
        registerWhereOperator(new DbMultivariateInOperator());
        registerWhereOperator(new DbMultivariateNotInOperator());
        // 注册Update条件的运算符
        registerUpdateOperator(new DbUnaryToNullOperator());
        registerUpdateOperator(new DbBinarySetOperator());
        registerUpdateOperator(new DbBinaryAddOperator());
    }

    /** 注册Where操作符 **/
    public void registerWhereOperator(DbBaseOperator operator) {
        registerOperator(whereOperators, operator);
    }

    /** 注册Update操作符 **/
    public void registerUpdateOperator(DbBaseOperator operator) {
        registerOperator(updateOperators, operator);
    }

    /** 获取Where操作符 **/
    @Override
    public DbBaseOperator getWhereOperator(String operatorType) {
        return whereOperators.get(convertKey(operatorType));
    }

    /** 获取Update操作符 **/
    @Override
    public DbBaseOperator getUpdateOperator(String operatorType) {
        return updateOperators.get(convertKey(operatorType));
    }

    private static void registerOperator(Map<String, DbBaseOperator> container, DbBaseOperator operator) {
        container.put(convertKey(operator.getType()), operator);
        container.put(convertKey(operator.getName()), operator);
        List<String> aliases = operator.getAliases();
        if (aliases != null) {
            for (String alias : aliases) {
                container.put(convertKey(alias), operator);
            }
        }
    }

    private static String convertKey(String operatorType) {
        return operatorType == null ? null : operatorType.toUpperCase();
    }
}
