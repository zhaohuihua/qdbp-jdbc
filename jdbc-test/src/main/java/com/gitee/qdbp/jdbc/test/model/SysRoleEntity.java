package com.gitee.qdbp.jdbc.test.model;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Table;
import com.gitee.qdbp.able.jdbc.paging.PartList;
import com.gitee.qdbp.jdbc.test.base.CommEntity;
import com.gitee.qdbp.jdbc.test.enums.UserType;

@Table(name = "TEST_ROLE_CORE_INFO")
public class SysRoleEntity extends CommEntity {

    /** 版本序列号 **/
    private static final long serialVersionUID = 1L;

    /** 租户编号 **/
    @Column
    private String tenantCode;
    /** 用户类型 **/
    @Column
    private UserType userType;
    /** 角色名称 **/
    @Column
    private String roleName;
    /** 描述 **/
    @Column
    private String roleDesc;
    /** 排序号(越小越靠前) **/
    @Column
    private Integer sortIndex;
    /** 创建人ID **/
    @Column
    private String creatorId;
    /** 创建人姓名 **/
    @Column
    private String creatorName;
    /** 默认角色(如果用户没有任何角色,默认会赋予该角色) **/
    @Column
    private Boolean defaults;
    /** 选项 **/
    @Column
    private RoleOptions options;

    /** 获取租户编号 **/
    public String getTenantCode() {
        return tenantCode;
    }

    /** 设置租户编号 **/
    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    /** 获取用户类型 **/
    public UserType getUserType() {
        return userType;
    }

    /** 设置用户类型 **/
    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    /** 获取角色名称 **/
    public String getRoleName() {
        return roleName;
    }

    /** 设置角色名称 **/
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    /** 获取描述 **/
    public String getRoleDesc() {
        return roleDesc;
    }

    /** 设置描述 **/
    public void setRoleDesc(String roleDesc) {
        this.roleDesc = roleDesc;
    }

    /** 获取排序号(越小越靠前) **/
    public Integer getSortIndex() {
        return sortIndex;
    }

    /** 设置排序号(越小越靠前) **/
    public void setSortIndex(Integer sortIndex) {
        this.sortIndex = sortIndex;
    }

    /** 获取创建人ID **/
    public String getCreatorId() {
        return creatorId;
    }

    /** 设置创建人ID **/
    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    /** 获取创建人姓名 **/
    public String getCreatorName() {
        return creatorName;
    }

    /** 设置创建人姓名 **/
    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    /** 获取默认角色(如果用户没有任何角色,默认会赋予该角色) **/
    public Boolean getDefaults() {
        return defaults;
    }

    /** 设置默认角色(如果用户没有任何角色,默认会赋予该角色) **/
    public void setDefaults(Boolean defaults) {
        this.defaults = defaults;
    }

    /** 获取选项 **/
    public RoleOptions getOptions() {
        return options;
    }

    /** 获取选项, force=是否强制返回非空对象 **/
    public RoleOptions getOptions(boolean force) {
        if (options == null && force) {
            options = new RoleOptions();
        }
        return options;
    }

    /** 设置选项 **/
    public void setOptions(RoleOptions options) {
        this.options = options;
    }

    /**
     * 将当前对象转换为子类对象
     *
     * @param clazz 目标类型
     * @return 目标对象
     */
    public <T extends SysRoleEntity> T to(Class<T> clazz) {
        T instance;
        try {
            instance = clazz.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to create " + clazz.getSimpleName() + " instance.", e);
        }

        instance.setId(this.getId()); // 角色ID
        instance.setTenantCode(this.getTenantCode()); // 租户编号
        instance.setUserType(this.getUserType()); // 用户类型
        instance.setRoleName(this.getRoleName()); // 角色名称
        instance.setRoleDesc(this.getRoleDesc()); // 描述
        instance.setSortIndex(this.getSortIndex()); // 排序号(越小越靠前)
        instance.setCreatorId(this.getCreatorId()); // 创建人ID
        instance.setCreatorName(this.getCreatorName()); // 创建人姓名
        instance.setCreateTime(this.getCreateTime()); // 创建时间
        instance.setDefaults(this.getDefaults()); // 默认角色(如果用户没有任何角色,默认会赋予该角色)
        instance.setOptions(this.getOptions()); // 选项
        instance.setDataState(this.getDataState()); // 数据状态:1为正常|随机数为已删除
        return instance;
    }

    /**
     * 将SysRoleEntity转换为子类对象
     *
     * @param beans 待转换的对象列表
     * @param clazz 目标类型
     * @param <T> SysRoleEntity或子类
     * @param <C> T的子类
     * @return 目标对象列表
     */
    public static <T extends SysRoleEntity, C extends T> List<C> to(List<T> beans, Class<C> clazz) {
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
        for (SysRoleEntity bean : beans) {
            list.add(bean.to(clazz));
        }
        return list;
    }

}
