package com.gitee.qdbp.jdbc.test.enums;

import com.gitee.qdbp.jdbc.plugins.EntityDataStateFillStrategy;

/**
 * DataState枚举类<br>
 * 数据状态(1.正常|2.作废|随机数=已删除)
 *
 * @author zhh
 * @version 160902
 * @see EntityDataStateFillStrategy
 */
public enum DataState {
    /** 0.NULL **/
    NULL,
    /** 1.正常 **/
    NORMAL,
    /** 2.作废 **/
    DISABLED,
    /** 3.删除 **/
    DELETED;
}
