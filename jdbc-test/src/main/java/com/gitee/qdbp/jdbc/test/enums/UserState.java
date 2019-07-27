package com.gitee.qdbp.jdbc.test.enums;

/**
 * UserState枚举类<br>
 * 状态(0.正常|1.锁定|2.待激活|3.注销)
 *
 * @author zhh
 * @version 170214
 */
public enum UserState {

    /** 0.正常 **/
    NORMAL,

    /** 1.锁定 **/
    LOCKED,

    /** 2.待激活 **/
    UNACTIVATED,

    /** 3.注销 **/
    LOGOFF;
}