package com.gitee.qdbp.jdbc.test.model;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Table;
import com.gitee.qdbp.able.jdbc.paging.PartList;
import com.gitee.qdbp.jdbc.test.enums.DataState;

/**
 * 部门信息实体类
 *
 * @author zhh
 * @version 180514
 */
@Table(name="TEST_DEPARTMENT_CORE_INFO")
public class SysDeptEntity implements Serializable {

    /** 版本序列号 **/
    private static final long serialVersionUID = 1L;

    /** 部门ID **/
    private String id;
    /** 租户编号 **/
    private String tenantCode;
    /** 部门编号 **/
    private String deptCode;
    /** 部门名称 **/
    private String deptName;
    /** 上级部门编号 **/
    private String parentCode;
    /** 排序号(越小越靠前) **/
    private Integer sortIndex;
    /** 创建人ID **/
    private String creatorId;
    /** 创建人姓名 **/
    private String creatorName;
    /** 创建时间 **/
    private Date createTime;
    /** 数据状态:1为正常|随机数为已删除 **/
    private DataState dataState;

    /** 获取部门ID **/
    public String getId() {
        return id;
    }

    /** 设置部门ID **/
    public void setId(String id) {
        this.id = id;
    }

    /** 获取租户编号 **/
    public String getTenantCode() {
        return tenantCode;
    }

    /** 设置租户编号 **/
    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    /** 获取部门编号 **/
    public String getDeptCode() {
        return deptCode;
    }

    /** 设置部门编号 **/
    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }

    /** 获取部门名称 **/
    public String getDeptName() {
        return deptName;
    }

    /** 设置部门名称 **/
    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    /** 获取上级部门编号 **/
    public String getParentCode() {
        return parentCode;
    }

    /** 设置上级部门编号 **/
    public void setParentCode(String parentCode) {
        this.parentCode = parentCode;
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

    /** 获取创建时间 **/
    public Date getCreateTime() {
        return createTime;
    }

    /** 设置创建时间 **/
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
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
    public <T extends SysDeptEntity> T to(Class<T> clazz) {
        T instance;
        try {
            instance = clazz.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to create " + clazz.getSimpleName() + " instance.", e);
        }

        instance.setId(this.getId()); // 部门ID
        instance.setTenantCode(this.getTenantCode()); // 租户编号
        instance.setDeptCode(this.getDeptCode()); // 部门编号
        instance.setDeptName(this.getDeptName()); // 部门名称
        instance.setParentCode(this.getParentCode()); // 上级部门编号
        instance.setSortIndex(this.getSortIndex()); // 排序号(越小越靠前)
        instance.setCreatorId(this.getCreatorId()); // 创建人ID
        instance.setCreatorName(this.getCreatorName()); // 创建人姓名
        instance.setCreateTime(this.getCreateTime()); // 创建时间
        instance.setDataState(this.getDataState()); // 数据状态:1为正常|随机数为已删除
        return instance;
    }

    /**
     * 将DepartmentCoreBean转换为子类对象
     *
     * @param beans 待转换的对象列表
     * @param clazz 目标类型
     * @param <T> DepartmentCoreBean或子类
     * @param <C> T的子类
     * @return 目标对象列表
     */
    public static <T extends SysDeptEntity, C extends T> List<C> to(List<T> beans, Class<C> clazz) {
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
        for (SysDeptEntity bean : beans) {
            list.add(bean.to(clazz));
        }
        return list;
    }

}

