package com.gitee.qdbp.jdbc.condition;

import java.io.Serializable;
import com.gitee.qdbp.able.model.db.DbCondition;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 数据库字段
 *
 * @author zhaohuihua
 * @version 181221
 */
public class DbField implements DbCondition, Serializable {

    /** SerialVersionUID **/
    private static final long serialVersionUID = 1L;

    /** 字段名 **/
    private String fieldName;
    /** 字段值 **/
    private Object fieldValue;
    /** 操作类型 **/
    private String operateType;

    /** 构造函数 **/
    public DbField() {
    }

    public boolean isEmpty() {
        return VerifyTools.isBlank(fieldName);
    }

    /** 构造函数 **/
    public DbField(String fieldName, Object fieldValue) {
        if (VerifyTools.isBlank(fieldName)) {
            throw new IllegalArgumentException("Argument 'fieldName' can not be empty.");
        }
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    /** 构造函数 **/
    public DbField(String operateType, String fieldName, Object fieldValue) {
        if (VerifyTools.isBlank(fieldName)) {
            throw new IllegalArgumentException("Argument 'fieldName' can not be empty.");
        }
        this.operateType = operateType;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    /** 字段名 **/
    public String getFieldName() {
        return fieldName;
    }

    /** 字段名 **/
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    /** 字段值 **/
    public Object getFieldValue() {
        return fieldValue;
    }

    /** 字段值 **/
    public void setFieldValue(Object fieldValue) {
        this.fieldValue = fieldValue;
    }

    /** 操作类型 **/
    public String getOperateType() {
        return operateType;
    }

    /** 操作类型 **/
    public void setOperateType(String operateType) {
        this.operateType = operateType;
    }

}
