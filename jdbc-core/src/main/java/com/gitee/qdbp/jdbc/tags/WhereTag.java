package com.gitee.qdbp.jdbc.tags;

/**
 * 去掉第1个AND|OR, 在前面增加WHERE<br>
 *
 * @author zhaohuihua
 * @version 20200906
 */
public class WhereTag extends TrimBase {

    public WhereTag() {
        setPrefix("WHERE");
        setPrefixOverrides("AND|OR");
    }
}
