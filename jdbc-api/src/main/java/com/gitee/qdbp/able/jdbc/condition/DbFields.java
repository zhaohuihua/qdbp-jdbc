package com.gitee.qdbp.able.jdbc.condition;

import java.util.List;
import com.gitee.qdbp.able.jdbc.base.DbCondition;

/**
 * 字段容器接口
 *
 * @author zhaohuihua
 * @version 190620
 */
interface DbFields extends DbCondition {

    /** 获取内容 **/
    List<DbCondition> items();

    /** 是否存在指定的字段 **/
    boolean contains(String fieldName);

    /** 是否为空 **/
    boolean isEmpty();

    /** 移除内容 **/
    void remove(String fieldName);

    /** 清空内容 **/
    void clear();
}
