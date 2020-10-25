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
    private String deptId;

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

    public String getDeptId() {
        return deptId;
    }

    public void setDeptId(String deptId) {
        this.deptId = deptId;
    }

}
