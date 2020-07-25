package com.gitee.qdbp.jdbc.test.enums;

/**
 * DataState枚举类<br>
 * 数据状态(1.正常|2.作废|其他=已删除)<br>
 * 如果使用随机数标记已删除记录, 那么如果除了正常和删除还有其他值, 一定要保证删除的码值是最大的<br>
 * 因为随机数作为标记时, 在数据库中会大于任何其他码值<br>
 * 这样dataState=DataState.DELETED条件就能转换为dataState&gt;9999<br>
 * 也只有将删除码值设为最大, dataState&lt;DataState.DELETED条件才能符合逻辑
 *
 * @author zhh
 * @version 160902
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
