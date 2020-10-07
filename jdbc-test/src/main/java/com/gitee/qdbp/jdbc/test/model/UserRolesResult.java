package com.gitee.qdbp.jdbc.test.model;

/**
 * 用户角色结果类
 *
 * @author zhaohuihua
 * @version 20201006
 */
public class UserRolesResult extends SysRoleEntity {

    /** serialVersionUID **/
    private static final long serialVersionUID = 1L;
    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
