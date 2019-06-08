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
        VerifyTools.requireNotBlank(fieldName, "fieldName");
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    /** 构造函数 **/
    public DbField(String operateType, String fieldName, Object fieldValue) {
        VerifyTools.requireNotBlank(fieldName, "fieldName");
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

    /**
     * 与指定的字段是否匹配<br>
     * 如果未指定表别名或该FieldColumn没有表别名, 只要字段名匹配即为匹配<br>
     * 如果指定了表别名且该FieldColumn有表别名, 则需要表别名和字段名同时匹配
     * 
     * @param fieldName 指定的字段名, 可带表别名, 如: u.userName
     * @return 是否匹配
     */
    public boolean matchesWithField(String fieldName) {
        String thatTableAlias = null;
        String thatRealFieldName = fieldName;
        {
            int dotIndex = fieldName.indexOf('.');
            if (dotIndex > 0) {
                thatTableAlias = fieldName.substring(0, dotIndex);
                thatRealFieldName = fieldName.substring(dotIndex + 1);
            } else if (dotIndex == 0) {
                thatRealFieldName = fieldName.substring(dotIndex + 1);
            }
        }
        String thisTableAlias = null;
        String thisRealFieldName = this.fieldName;
        {
            int dotIndex = this.fieldName.indexOf('.');
            if (dotIndex > 0) {
                thisTableAlias = this.fieldName.substring(0, dotIndex);
                thisRealFieldName = this.fieldName.substring(dotIndex + 1);
            } else if (dotIndex == 0) {
                thisRealFieldName = this.fieldName.substring(dotIndex + 1);
            }
        }
        if (VerifyTools.isBlank(thatTableAlias) || VerifyTools.isBlank(thisTableAlias)) {
            return thatRealFieldName.equals(thisRealFieldName);
        } else {
            return thatTableAlias.equals(thisTableAlias) && thatRealFieldName.equals(thisRealFieldName);
        }
    }
}
