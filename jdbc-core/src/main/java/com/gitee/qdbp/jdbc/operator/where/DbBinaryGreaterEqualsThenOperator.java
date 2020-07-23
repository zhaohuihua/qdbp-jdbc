package com.gitee.qdbp.jdbc.operator.where;

/**
 * 二元等号运算符
 *
 * @author zhaohuihua
 * @version 20200123
 */
public class DbBinaryGreaterEqualsThenOperator extends DbBinarySymbolOperator {

    public DbBinaryGreaterEqualsThenOperator() {
        super(">=", "GreaterEqualsThen", "EqualsOrGreaterThen", "Min");
    }

}
