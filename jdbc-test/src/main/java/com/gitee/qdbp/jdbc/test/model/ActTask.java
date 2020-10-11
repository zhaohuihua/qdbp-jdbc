package com.gitee.qdbp.jdbc.test.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "ACT_RU_TASK")
public class ActTask implements Serializable {

    /** serialVersionUID **/
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID_")
    private String id;
    @Column(name = "ASSIGNEE_")
    private String assignee;
    @Column(name = "CREATE_TIME_")
    private Date createTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

}
