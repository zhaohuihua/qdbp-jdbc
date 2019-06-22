package com.gitee.qdbp.jdbc.model;

import java.io.Serializable;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 数据库版本信息
 *
 * @author zhaohuihua
 * @version 190602
 */
public class DbVersion implements Serializable {

    /** serialVersionUID **/
    private static final long serialVersionUID = 1L;

    /** 数据库类型 **/
    private DbType dbType;
    /** 版本字符串 **/
    private String versionString;
    /** 主版本号 **/
    private int majorVersion;
    /** 次版本号 **/
    private int minorVersion;

    /** 数据库类型 **/
    public DbType getDbType() {
        return dbType;
    }

    /** 数据库类型 **/
    public void setDbType(DbType dbType) {
        this.dbType = dbType;
    }

    /** 版本字符串 **/
    public String getVersionString() {
        return versionString;
    }

    /** 版本字符串 **/
    public void setVersionString(String versionString) {
        this.versionString = versionString;
    }

    /** 主版本号 **/
    public int getMajorVersion() {
        return majorVersion;
    }

    /** 主版本号 **/
    public void setMajorVersion(int majorVersion) {
        this.majorVersion = majorVersion;
    }

    /** 次版本号 **/
    public int getMinorVersion() {
        return minorVersion;
    }

    /** 次版本号 **/
    public void setMinorVersion(int minorVersion) {
        this.minorVersion = minorVersion;
    }

    public String toString() {
        if (dbType == null) {
            return "null";
        }
        StringBuilder buffer = new StringBuilder();
        buffer.append(dbType);
        if (VerifyTools.isNotBlank(versionString)) {
            buffer.append('(').append(versionString).append(')');
        }
        return buffer.toString();
    }

}
