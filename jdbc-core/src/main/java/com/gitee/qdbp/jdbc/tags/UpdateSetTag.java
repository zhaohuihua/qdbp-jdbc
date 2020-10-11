package com.gitee.qdbp.jdbc.tags;

/**
 * 在前面增加SET, 去掉最后一个逗号<br>
 *
 * @author zhaohuihua
 * @version 20200917
 */
public class UpdateSetTag extends TrimBase {

    public UpdateSetTag() {
        setPrefix("SET");
        setSuffixOverrides(",");
    }
}
