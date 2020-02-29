package com.gitee.qdbp.jdbc.test.model;

import java.io.Serializable;
import java.util.Date;
import com.gitee.qdbp.jdbc.test.enums.DataState;

/**
 * 系统日志表
 *
 * @author zhaohuihua
 * @version 20200208
 */
public class SysLoggerEntity implements Serializable {

    /** 版本序列号 **/
    private static final long serialVersionUID = 1L;
    /** 表名 **/
    public static final String TABLE = "TEST_LOGGER";

    private String id;
    private String name;
    private String content;
    private Date createTime;
    private DataState dataState;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public DataState getDataState() {
        return dataState;
    }

    public void setDataState(DataState dataState) {
        this.dataState = dataState;
    }

}
