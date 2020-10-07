package com.gitee.qdbp.jdbc.test.model;

import javax.persistence.Column;
import javax.persistence.Table;
import com.gitee.qdbp.jdbc.test.base.IdEntity;

@Table(name = "ACT_PROC_STATE")
public class ActProcState extends IdEntity {

    /** serialVersionUID **/
    private static final long serialVersionUID = 1L;

    @Column
    private String projectCode;
    @Column
    private String projectName;
    @Column
    private String orgId;

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

}
