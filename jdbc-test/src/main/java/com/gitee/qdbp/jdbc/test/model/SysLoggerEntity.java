package com.gitee.qdbp.jdbc.test.model;

import com.gitee.qdbp.able.jdbc.annotations.ColumnDefault;
import com.gitee.qdbp.jdbc.test.base.CommEntity;

/**
 * 系统日志表
 *
 * @author zhaohuihua
 * @version 20200208
 */
public class SysLoggerEntity extends CommEntity {

    /** 版本序列号 **/
    private static final long serialVersionUID = 1L;
    /** 表名 **/
    public static final String TABLE = "TEST_LOGGER";

    private String name;
    private String content;
    @ColumnDefault("1")
    private Integer sortIndex;

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

    public Integer getSortIndex() {
        return sortIndex;
    }

    public void setSortIndex(Integer sortIndex) {
        this.sortIndex = sortIndex;
    }

}
