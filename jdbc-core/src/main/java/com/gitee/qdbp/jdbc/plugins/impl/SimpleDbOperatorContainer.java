package com.gitee.qdbp.jdbc.plugins.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.gitee.qdbp.jdbc.operator.DbBaseOperator;
import com.gitee.qdbp.jdbc.operator.impl.DbBinaryAddOperator;
import com.gitee.qdbp.jdbc.operator.impl.DbBinaryEndsWithOperator;
import com.gitee.qdbp.jdbc.operator.impl.DbBinaryEqualsOperator;
import com.gitee.qdbp.jdbc.operator.impl.DbBinaryGreaterEqualsThenOperator;
import com.gitee.qdbp.jdbc.operator.impl.DbBinaryGreaterThenOperator;
import com.gitee.qdbp.jdbc.operator.impl.DbBinaryLessEqualsThenOperator;
import com.gitee.qdbp.jdbc.operator.impl.DbBinaryLessThenOperator;
import com.gitee.qdbp.jdbc.operator.impl.DbBinaryLikeOperator;
import com.gitee.qdbp.jdbc.operator.impl.DbBinaryNotEqualsOperator;
import com.gitee.qdbp.jdbc.operator.impl.DbBinaryNotLikeOperator;
import com.gitee.qdbp.jdbc.operator.impl.DbBinarySetOperator;
import com.gitee.qdbp.jdbc.operator.impl.DbBinaryStartsWithOperator;
import com.gitee.qdbp.jdbc.operator.impl.DbMultivariateInOperator;
import com.gitee.qdbp.jdbc.operator.impl.DbMultivariateNotInOperator;
import com.gitee.qdbp.jdbc.operator.impl.DbTernaryBetweenOperator;
import com.gitee.qdbp.jdbc.operator.impl.DbTernaryNotBetweenOperator;
import com.gitee.qdbp.jdbc.operator.impl.DbUnaryIsNotNullOperator;
import com.gitee.qdbp.jdbc.operator.impl.DbUnaryIsNullOperator;
import com.gitee.qdbp.jdbc.operator.impl.DbUnaryToNullOperator;
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

    public void registerWhereOperator(DbBaseOperator operator) {
        registerOperator(whereOperators, operator);
    }

    public void registerUpdateOperator(DbBaseOperator operator) {
        registerOperator(updateOperators, operator);
    }

    @Override
    public DbBaseOperator getWhereOperator(String operatorType) {
        return whereOperators.get(convertKey(operatorType));
    }

    @Override
    public DbBaseOperator getUpdateOperator(String operatorType) {
        return updateOperators.get(convertKey(operatorType));
    }

    private static void registerOperator(Map<String, DbBaseOperator> container, DbBaseOperator operator) {
        container.put(convertKey(operator.getType()), operator);
        List<String> aliases = operator.getAliases();
        if (aliases != null) {
            for (String alias : aliases) {
                container.put(convertKey(alias), operator);
            }
        }
    }

    private static String convertKey(String operatorType) {
        return operatorType.toUpperCase();
    }
}
