package com.gitee.qdbp.jdbc.plugins.impl;

import java.util.Date;
import java.util.Map;
import com.gitee.qdbp.jdbc.condition.DbUpdate;
import com.gitee.qdbp.jdbc.condition.DbWhere;
import com.gitee.qdbp.tools.utils.RandomTools;

/**
 * 默认实体类业务处理类
 *
 * @author zhaohuihua
 * @version 190602
 */
public class SimpleModelDataHandler extends BaseModelDataHandler {

    /** 逻辑删除字段名 **/
    private String logicalDeleteField;
    /** 数据有效标记 **/
    private Object dataEffectiveFlag;
    /** 数据无效标记 **/
    private Object dataIneffectiveFlag;
    /** 创建时间字段名 **/
    private String createTimeField;
    /** 修改时间字段名 **/
    private String updateTimeField;
    /** 创建用户字段名 **/
    private String createUserField;
    /** 修改用户字段名 **/
    private String updateUserField;

    /** 逻辑删除字段名 **/
    public String getLogicalDeleteField() {
        return logicalDeleteField;
    }

    /** 逻辑删除字段名 **/
    public void setLogicalDeleteField(String logicalDeleteField) {
        this.logicalDeleteField = logicalDeleteField;
    }

    /** 数据有效标记 **/
    public Object getDataEffectiveFlag() {
        return dataEffectiveFlag;
    }

    /** 数据有效标记 **/
    public void setDataEffectiveFlag(Object dataEffectiveFlag) {
        this.dataEffectiveFlag = dataEffectiveFlag;
    }

    /** 数据无效标记 **/
    public Object getDataIneffectiveFlag() {
        return dataIneffectiveFlag;
    }

    /** 数据无效标记 **/
    public void setDataIneffectiveFlag(Object dataIneffectiveFlag) {
        this.dataIneffectiveFlag = dataIneffectiveFlag;
    }

    /** 创建时间字段名 **/
    public String getCreateTimeField() {
        return createTimeField;
    }

    /** 创建时间字段名 **/
    public void setCreateTimeField(String createTimeField) {
        this.createTimeField = createTimeField;
    }

    /** 修改时间字段名 **/
    public String getUpdateTimeField() {
        return updateTimeField;
    }

    /** 修改时间字段名 **/
    public void setUpdateTimeField(String updateTimeField) {
        this.updateTimeField = updateTimeField;
    }

    /** 创建用户字段名 **/
    public String getCreateUserField() {
        return createUserField;
    }

    /** 创建用户字段名 **/
    public void setCreateUserField(String createUserField) {
        this.createUserField = createUserField;
    }

    /** 修改用户字段名 **/
    public String getUpdateUserField() {
        return updateUserField;
    }

    /** 修改用户字段名 **/
    public void setUpdateUserField(String updateUserField) {
        this.updateUserField = updateUserField;
    }

    /** 获取当前登录账号, 一般是UserId **/
    protected Object getLoginAccount() {
        return null;
    }

    /** 生成主键编号 **/
    public String generatePrimaryKeyCode(String tableName) {
        return RandomTools.generateUuid();
    }
    
    /**
     * 是否存在逻辑删除字段
     * 
     * @return 否存在
     */
    public boolean containsLogicalDeleteFlag(Map<String, String> fieldColumnMap) {
        return containsField(logicalDeleteField, fieldColumnMap);
    }

    /**
     * 填充数据有效标记
     * 
     * @param condition 条件
     */
    public void fillDataEffectiveFlag(Map<String, Object> condition, Map<String, String> fieldColumnMap) {
        fillValueIfAbsent(condition, logicalDeleteField, dataEffectiveFlag, fieldColumnMap);
    }

    /**
     * 填充数据有效标记
     * 
     * @param where 条件
     */
    public void fillDataEffectiveFlag(DbWhere where, Map<String, String> fieldColumnMap) {
        fillValueIfAbsent(where, logicalDeleteField, dataEffectiveFlag, fieldColumnMap);
    }

    /**
     * 填充数据有效标记
     * 
     * @param ud 更新对象
     */
    public void fillDataEffectiveFlag(DbUpdate ud, Map<String, String> fieldColumnMap) {
        fillValueIfAbsent(ud, logicalDeleteField, dataEffectiveFlag, fieldColumnMap);
    }

    /**
     * 填充数据无效标记
     * 
     * @param condition 条件
     */
    public void fillDataIneffectiveFlag(Map<String, Object> condition, Map<String, String> fieldColumnMap) {
        fillValueIfAbsent(condition, logicalDeleteField, dataIneffectiveFlag, fieldColumnMap);
    }

    /**
     * 填充数据无效标记
     * 
     * @param where 条件
     */
    public void fillDataIneffectiveFlag(DbWhere where, Map<String, String> fieldColumnMap) {
        fillValueIfAbsent(where, logicalDeleteField, dataIneffectiveFlag, fieldColumnMap);
    }

    /**
     * 填充数据无效标记
     * 
     * @param ud 更新对象
     */
    public void fillDataIneffectiveFlag(DbUpdate ud, Map<String, String> fieldColumnMap) {
        fillValueIfAbsent(ud, logicalDeleteField, dataIneffectiveFlag, fieldColumnMap);
    }

    /**
     * 填充创建参数
     * 
     * @param model 实体对象
     */
    public void fillCreteParams(Map<String, Object> model, Map<String, String> fieldColumnMap) {
        Date now = new Date();
        fillValueIfAbsent(model, createTimeField, now, fieldColumnMap);
        fillValueIfAbsent(model, updateTimeField, now, fieldColumnMap);

        Object account = getLoginAccount();
        fillValueIfAbsent(model, createUserField, account, fieldColumnMap);
        fillValueIfAbsent(model, updateUserField, account, fieldColumnMap);
    }

    /**
     * 填充更新参数
     * 
     * @param model 实体对象
     */
    public void fillUpdateParams(Map<String, Object> model, Map<String, String> fieldColumnMap) {
        Date now = new Date();
        fillValueIfAbsent(model, updateTimeField, now, fieldColumnMap);

        Object account = getLoginAccount();
        fillValueIfAbsent(model, updateUserField, account, fieldColumnMap);
    }

    /**
     * 填充更新参数
     * 
     * @param ud 实体对象
     */
    public void fillUpdateParams(DbUpdate ud, Map<String, String> fieldColumnMap) {
        Date now = new Date();
        fillValueIfAbsent(ud, updateTimeField, now, fieldColumnMap);

        Object account = getLoginAccount();
        fillValueIfAbsent(ud, updateUserField, account, fieldColumnMap);
    }

    /**
     * 填充数据有效性标记
     * 
     * @param model 实体对象
     */
    public void fillEffectiveFlag(Object model) {
        setFieldValueIfAbsent(model, logicalDeleteField, dataEffectiveFlag);
    }

    /**
     * 填充创建参数
     * 
     * @param model 实体对象
     */
    public void fillCreteParams(Object model) {
        Date now = new Date();

        setFieldValueIfAbsent(model, createTimeField, now);
        setFieldValueIfAbsent(model, updateTimeField, now);

        Object account = getLoginAccount();
        setFieldValueIfAbsent(model, createUserField, account);
        setFieldValueIfAbsent(model, updateUserField, account);
    }

    /**
     * 填充更新参数
     * 
     * @param model 实体对象
     */
    public void fillUpdateParams(Object model) {
        Date now = new Date();
        setFieldValueIfAbsent(model, updateTimeField, now);

        Object account = getLoginAccount();
        setFieldValueIfAbsent(model, updateUserField, account);
    }
}
