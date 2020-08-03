package com.gitee.qdbp.jdbc.test.model;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Table;
import com.gitee.qdbp.jdbc.test.base.CommEntity;
import com.gitee.qdbp.jdbc.test.enums.SettingState;

/**
 * 系统设置表
 *
 * @author zhaohuihua
 * @version 20200208
 */
@Table(name="TEST_SETTING")
public class SysSettingEntity extends CommEntity {

    /** 版本序列号 **/
    private static final long serialVersionUID = 1L;

    @Column
    private String name;
    @Column
    private String value;
    @Column
    private Integer version;
    @Column
    private String remark;
    @Column
    private SettingState state;
    /** 测试与父类重复的字段 **/
    @Column
    private Date createTime;
    @Column
    private Date updateTime;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public SettingState getState() {
        return state;
    }

    public void setState(SettingState state) {
        this.state = state;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

}
