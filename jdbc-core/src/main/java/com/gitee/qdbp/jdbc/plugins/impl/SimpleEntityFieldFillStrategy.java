package com.gitee.qdbp.jdbc.plugins.impl;

import java.util.Date;
import java.util.Map;
import com.gitee.qdbp.able.jdbc.condition.DbUpdate;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.jdbc.model.AllFieldColumn;
import com.gitee.qdbp.jdbc.plugins.EntityFillBizResolver;
import com.gitee.qdbp.jdbc.plugins.EntityFieldFillStrategy;
import com.gitee.qdbp.tools.utils.RandomTools;

/**
 * 默认实体数据填充业务处理类<br>
 * 自动填充创建人/创建时间/修改人/修改时间字段
 *
 * @author zhaohuihua
 * @version 190602
 */
public class SimpleEntityFieldFillStrategy implements EntityFieldFillStrategy {

    /** 创建时间字段名 **/
    private String createTimeField;
    /** 修改时间字段名 **/
    private String updateTimeField;
    /** 创建用户字段名 **/
    private String createUserField;
    /** 修改用户字段名 **/
    private String updateUserField;
    /** 与业务强相关的数据提供者 **/
    private EntityFillBizResolver entityFillBizResolver;

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

    /** 与业务强相关的数据提供者 **/
    public EntityFillBizResolver getEntityFillBizResolver() {
        return entityFillBizResolver;
    }

    /** 与业务强相关的数据提供者 **/
    public void setEntityFillBizResolver(EntityFillBizResolver entityFillBizResolver) {
        this.entityFillBizResolver = entityFillBizResolver;
    }

    /** {@inheritDoc} **/
    @Override
    public String getLoginAccount() {
        if (entityFillBizResolver == null) {
            return null;
        } else {
            return entityFillBizResolver.getLoginAccount();
        }
    }

    /** {@inheritDoc} **/
    @Override
    public String generatePrimaryKeyCode(String tableName) {
        if (entityFillBizResolver == null) {
            return RandomTools.generateUuid();
        } else {
            return entityFillBizResolver.generatePrimaryKeyCode(tableName);
        }
    }

    /** {@inheritDoc} **/
    @Override
    public void fillQueryWhereParams(DbWhere where, String tableAlias, AllFieldColumn<?> allFields) {
        // 无默认操作
    }

    /** {@inheritDoc} **/
    @Override
    public void fillUpdateWhereParams(DbWhere where, AllFieldColumn<?> allFields) {
        // 无默认操作
    }

    /** {@inheritDoc} **/
    @Override
    public void fillDeleteWhereParams(DbWhere where, AllFieldColumn<?> allFields) {
        // 无默认操作
    }

    /** {@inheritDoc} **/
    @Override
    public void fillEntityCreateParams(Map<String, Object> entity, AllFieldColumn<?> allFields) {
        Date now = new Date();
        EntityFillTools.fillValueIfAbsent(entity, createTimeField, now, allFields);
        EntityFillTools.fillValueIfAbsent(entity, updateTimeField, now, allFields);

        String account = getLoginAccount();
        EntityFillTools.fillValueIfAbsent(entity, createUserField, account, allFields);
        EntityFillTools.fillValueIfAbsent(entity, updateUserField, account, allFields);
    }

    /** {@inheritDoc} **/
    @Override
    public void fillEntityUpdateParams(Map<String, Object> entity, AllFieldColumn<?> allFields) {
        Date now = new Date();
        EntityFillTools.fillValueIfAbsent(entity, updateTimeField, now, allFields);

        String account = getLoginAccount();
        EntityFillTools.fillValueIfAbsent(entity, updateUserField, account, allFields);
    }

    /** {@inheritDoc} **/
    @Override
    public void fillEntityUpdateParams(DbUpdate ud, AllFieldColumn<?> allFields) {
        Date now = new Date();
        EntityFillTools.fillValueIfAbsent(ud, updateTimeField, now, allFields);

        Object account = getLoginAccount();
        EntityFillTools.fillValueIfAbsent(ud, updateUserField, account, allFields);
    }

    /** {@inheritDoc} **/
    @Override
    public void fillLogicalDeleteParams(Map<String, Object> entity, AllFieldColumn<?> allFields) {
        this.fillEntityUpdateParams(entity, allFields);
    }

    /** {@inheritDoc} **/
    @Override
    public void fillLogicalDeleteParams(DbUpdate ud, AllFieldColumn<?> allFields) {
        this.fillEntityUpdateParams(ud, allFields);
    }

}
