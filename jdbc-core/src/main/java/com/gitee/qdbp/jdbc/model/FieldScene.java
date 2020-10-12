package com.gitee.qdbp.jdbc.model;

/**
 * 字段使用场景
 *
 * @author zhaohuihua
 * @version 20200904
 * @since 3.2.0
 */
public enum FieldScene {

    /** 新增场景, 只取insertable=true的字段 **/
    INSERT,
    /** 更新场景, 只取updatable=true的字段 **/
    UPDATE,
    /** 条件场景, 只取insertable=true或updatable=true的字段 **/
    CONDITION,
    /** 结果场景, 取所有字段 **/
    RESULT
}
