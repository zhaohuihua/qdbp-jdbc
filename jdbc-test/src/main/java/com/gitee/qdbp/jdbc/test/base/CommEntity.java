package com.gitee.qdbp.jdbc.test.base;

import java.util.Date;
import com.gitee.qdbp.jdbc.test.enums.DataState;

public class CommEntity extends IdEntity {

    /** 版本序列号 **/
    private static final long serialVersionUID = 1L;
    /** 创建时间 **/
    private Date createTime;
    /** 数据状态:1为正常|随机数为已删除 **/
    private DataState dataState;

    /** 获取创建时间 **/
    public Date getCreateTime() {
        return createTime;
    }

    /** 设置创建时间 **/
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /** 获取数据状态:1为正常|随机数为已删除 **/
    public DataState getDataState() {
        return dataState;
    }

    /** 设置数据状态:1为正常|随机数为已删除 **/
    public void setDataState(DataState dataState) {
        this.dataState = dataState;
    }
}
