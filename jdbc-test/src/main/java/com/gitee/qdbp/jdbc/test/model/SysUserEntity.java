package com.gitee.qdbp.jdbc.test.model;

import java.util.ArrayList;
import java.util.List;
import com.gitee.qdbp.able.jdbc.paging.PartList;
import com.gitee.qdbp.jdbc.test.base.CommEntity;
import com.gitee.qdbp.jdbc.test.enums.AccountType;
import com.gitee.qdbp.jdbc.test.enums.Gender;
import com.gitee.qdbp.jdbc.test.enums.UserSource;
import com.gitee.qdbp.jdbc.test.enums.UserState;

/**
 * 用户基础信息实体类
 *
 * @author zhh
 * @version 170712
 */
public class SysUserEntity extends CommEntity {

    /** 版本序列号 **/
    private static final long serialVersionUID = 1L;

    /** 表名 **/
    public static final String TABLE = "sys_user_core_info";

    /** 租户编号 **/
    private String tenantCode;
    /** 用户类型 **/
    private AccountType userType;
    /** 部门编号 **/
    private String deptCode;
    /** 账号/工号 **/
    private String userCode;
    /** 登录用户名 **/
    private String userName;
    /** 昵称 **/
    private String nickName;
    /** 真实姓名 **/
    private String realName;
    /** 电话 **/
    private String phone;
    /** 邮箱 **/
    private String email;
    /** 性别(0.未知|1.男|2.女) **/
    private Gender gender;
    /** 头像 **/
    private String photo;
    /** 城市 **/
    private String city;
    /** 身份证 **/
    private String identity;
    /** 密码 **/
    private String password;
    /** 是否为超级用户 **/
    private Boolean superman;
    /** 选项 **/
    private UserCoreOptions options;
    /** 状态(0.正常|1.锁定|2.待激活|3.注销) **/
    private UserState userState;
    /** 注册来源 **/
    private UserSource userSource;

    /** 获取租户编号 **/
    public String getTenantCode() {
        return tenantCode;
    }

    /** 设置租户编号 **/
    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    /** 获取用户类型 **/
    public AccountType getUserType() {
        return userType;
    }

    /** 设置用户类型 **/
    public void setUserType(AccountType userType) {
        this.userType = userType;
    }

    /** 获取部门编号 **/
    public String getDeptCode() {
        return deptCode;
    }

    /** 设置部门编号 **/
    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }

    /** 获取账号/工号 **/
    public String getUserCode() {
        return userCode;
    }

    /** 设置账号/工号 **/
    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    /** 获取登录用户名 **/
    public String getUserName() {
        return userName;
    }

    /** 设置登录用户名 **/
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /** 获取昵称 **/
    public String getNickName() {
        return nickName;
    }

    /** 设置昵称 **/
    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    /** 获取真实姓名 **/
    public String getRealName() {
        return realName;
    }

    /** 设置真实姓名 **/
    public void setRealName(String realName) {
        this.realName = realName;
    }

    /** 获取电话 **/
    public String getPhone() {
        return phone;
    }

    /** 设置电话 **/
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /** 获取邮箱 **/
    public String getEmail() {
        return email;
    }

    /** 设置邮箱 **/
    public void setEmail(String email) {
        this.email = email;
    }

    /** 获取性别(0.未知|1.男|2.女) **/
    public Gender getGender() {
        return gender;
    }

    /** 设置性别(0.未知|1.男|2.女) **/
    public void setGender(Gender gender) {
        this.gender = gender;
    }

    /** 获取头像 **/
    public String getPhoto() {
        return photo;
    }

    /** 设置头像 **/
    public void setPhoto(String photo) {
        this.photo = photo;
    }

    /** 获取城市 **/
    public String getCity() {
        return city;
    }

    /** 设置城市 **/
    public void setCity(String city) {
        this.city = city;
    }

    /** 获取身份证 **/
    public String getIdentity() {
        return identity;
    }

    /** 设置身份证 **/
    public void setIdentity(String identity) {
        this.identity = identity;
    }

    /** 获取密码 **/
    public String getPassword() {
        return password;
    }

    /** 设置密码 **/
    public void setPassword(String password) {
        this.password = password;
    }

    /** 获取是否为超级用户 **/
    public Boolean getSuperman() {
        return superman;
    }

    /** 设置是否为超级用户 **/
    public void setSuperman(Boolean superman) {
        this.superman = superman;
    }

    /** 获取状态(0.正常|1.锁定|2.待激活|3.注销) **/
    public UserState getUserState() {
        return userState;
    }

    /** 设置状态(0.正常|1.锁定|2.待激活|3.注销) **/
    public void setUserState(UserState userState) {
        this.userState = userState;
    }

    /** 获取选项 **/
    public UserCoreOptions getOptions() {
        return options;
    }

    /** 获取选项, force=是否强制返回非空选项 **/
    public UserCoreOptions getOptions(boolean force) {
        if (options == null && force) {
            options = new UserCoreOptions();
        }
        return options;
    }

    /** 设置选项 **/
    public void setOptions(UserCoreOptions options) {
        this.options = options;
    }

    /** 获取注册来源 **/
    public UserSource getUserSource() {
        return userSource;
    }

    /** 设置注册来源 **/
    public void setUserSource(UserSource userSource) {
        this.userSource = userSource;
    }

    /** 获取用户的显示名称 **/
    public String toDisplayName() {
        if (nickName != null && nickName.length() > 0) {
            return nickName;
        } else if (realName != null && realName.length() > 0) {
            return realName;
        } else if (phone != null && phone.length() > 0) {
            return phone;
        } else if (email != null && email.length() > 0) {
            return email;
        } else if (userCode != null && userCode.length() > 0) {
            return userCode;
        } else if (userName != null && userName.length() > 0) {
            return userName;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        String name = toDisplayName();
        return name == null ? getId() : name;
    }

    /**
     * 将当前对象转换为子类对象
     *
     * @param clazz 目标类型
     * @return 目标对象
     */
    public <T extends SysUserEntity> T to(Class<T> clazz) {
        T instance;
        try {
            instance = clazz.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to create " + clazz.getSimpleName() + " instance.", e);
        }

        instance.setId(this.getId()); // 用户ID
        instance.setTenantCode(this.getTenantCode()); // 租户编号
        instance.setUserType(this.getUserType()); // 用户类型
        instance.setDeptCode(this.getDeptCode()); // 部门编号
        instance.setUserCode(this.getUserCode()); // 账号/工号
        instance.setUserName(this.getUserName()); // 登录用户名
        instance.setNickName(this.getNickName()); // 昵称
        instance.setRealName(this.getRealName()); // 真实姓名
        instance.setPhone(this.getPhone()); // 电话
        instance.setEmail(this.getEmail()); // 邮箱
        instance.setGender(this.getGender()); // 性别(0.未知|1.男|2.女)
        instance.setPhoto(this.getPhoto()); // 头像
        instance.setCity(this.getCity()); // 城市
        instance.setIdentity(this.getIdentity()); // 身份证
        instance.setPassword(this.getPassword()); // 密码
        instance.setCreateTime(this.getCreateTime()); // 创建时间
        instance.setSuperman(this.getSuperman()); // 是否为超级用户
        instance.setUserState(this.getUserState()); // 状态(0.正常|1.锁定|2.待激活|3.注销)
        instance.setUserSource(this.getUserSource()); // 来源
        instance.setDataState(this.getDataState()); // 数据状态:0为正常|其他为删除
        return instance;
    }

    /**
     * 将UserCoreBean转换为子类对象
     *
     * @param beans 待转换的对象列表
     * @param clazz 目标类型
     * @param <T> UserCoreBean或子类
     * @param <C> T的子类
     * @return 目标对象列表
     */
    public static <T extends SysUserEntity, C extends T> List<C> to(List<T> beans, Class<C> clazz) {
        if (beans == null) {
            return null;
        }
        List<C> list;
        if (beans instanceof PartList) {
            PartList<C> partlist = new PartList<>();
            partlist.setTotal(((PartList<?>) beans).getTotal());
            list = partlist;
        } else {
            list = new ArrayList<>();
        }
        for (SysUserEntity bean : beans) {
            list.add(bean.to(clazz));
        }
        return list;
    }

}
