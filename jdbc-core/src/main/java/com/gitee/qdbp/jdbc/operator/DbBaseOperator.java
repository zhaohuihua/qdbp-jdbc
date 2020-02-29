package com.gitee.qdbp.jdbc.operator;

import java.util.List;

/**
 * 基本运算符接口<br>
 * 如不等于: Type是!=; Alias是NotEquals和Not Equals; Name是NotEquals<br>
 *
 * @author zhaohuihua
 * @version 20200123
 */
public interface DbBaseOperator {

    /** 运算符名称(无空格的英文名称,将用于从request中自动构造where和update) **/
    String getName();

    /** 运算符类型(一般是数据库可识别的运算符) **/
    String getType();

    /** 运算符别名(书写时方便识别的运算符别名) **/
    List<String> getAliases();
}
