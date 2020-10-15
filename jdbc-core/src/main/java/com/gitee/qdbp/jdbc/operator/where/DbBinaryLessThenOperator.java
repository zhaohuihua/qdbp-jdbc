package com.gitee.qdbp.jdbc.operator.where;

/**
 * 二元等号运算符
 *
 * @author zhaohuihua
 * @version 20200123
 */
public class DbBinaryLessThenOperator extends DbBinarySymbolOperator {

    /** 版本序列号 **/
    private static final long serialVersionUID = 1L;

    public DbBinaryLessThenOperator() {
        super("<", "LessThen");
    }

}
