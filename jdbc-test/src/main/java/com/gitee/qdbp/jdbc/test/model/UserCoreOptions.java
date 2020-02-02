package com.gitee.qdbp.jdbc.test.model;

import com.gitee.qdbp.able.model.reusable.ExtraData;

/**
 * 选项
 *
 * @author zhh
 * @version 170712
 */
public class UserCoreOptions extends ExtraData {

    /** 版本序列号 **/
    private static final long serialVersionUID = 1L;

    /** 备注 **/
    private String remark;

    /** 备注 **/
    public String getRemark() {
        return remark;
    }

    /** 备注 **/
    public void setRemark(String remark) {
        this.remark = remark;
    }

}
