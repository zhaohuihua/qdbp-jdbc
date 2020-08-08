package com.gitee.qdbp.jdbc.test.model;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Table;
import com.gitee.qdbp.able.jdbc.paging.PartList;
import com.gitee.qdbp.jdbc.test.base.IdEntity;
import com.gitee.qdbp.jdbc.test.enums.DataState;

@Table(name = "TEST_USER_ROLE_REF")
public class SysUserRoleEntity extends IdEntity {

    /** 版本序列号 **/
    private static final long serialVersionUID = 1L;

    /** 用户ID **/
    @Column
    private String userId;
    /** 角色ID **/
    @Column
    private String roleId;
    /** 数据状态:1为正常|随机数为已删除 **/
    @Column
    private DataState dataState;

    /** 获取用户ID **/
    public String getUserId() {
        return userId;
    }

    /** 设置用户ID **/
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /** 获取角色ID **/
    public String getRoleId() {
        return roleId;
    }

    /** 设置角色ID **/
    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    /** 获取数据状态:1为正常|随机数为已删除 **/
    public DataState getDataState() {
        return dataState;
    }

    /** 设置数据状态:1为正常|随机数为已删除 **/
    public void setDataState(DataState dataState) {
        this.dataState = dataState;
    }

    /**
     * 将当前对象转换为子类对象
     *
     * @param clazz 目标类型
     * @return 目标对象
     */
    public <T extends SysUserRoleEntity> T to(Class<T> clazz) {
        T instance;
        try {
            instance = clazz.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to create " + clazz.getSimpleName() + " instance.", e);
        }

        instance.setId(this.getId()); // 主键ID
        instance.setUserId(this.getUserId()); // 用户ID
        instance.setRoleId(this.getRoleId()); // 角色ID
        instance.setDataState(this.getDataState()); // 数据状态:1为正常|随机数为已删除
        return instance;
    }

    /**
     * 将SysUserRoleEntity转换为子类对象
     *
     * @param beans 待转换的对象列表
     * @param clazz 目标类型
     * @param <T> SysUserRoleEntity或子类
     * @param <C> T的子类
     * @return 目标对象列表
     */
    public static <T extends SysUserRoleEntity, C extends T> List<C> to(List<T> beans, Class<C> clazz) {
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
        for (SysUserRoleEntity bean : beans) {
            list.add(bean.to(clazz));
        }
        return list;
    }
}
