package com.gitee.qdbp.jdbc.model;

import java.io.Serializable;
import com.gitee.qdbp.tools.utils.StringTools;
import com.gitee.qdbp.tools.utils.VerifyTools;
import com.gitee.qdbp.tools.utils.VersionCodeTools;

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

    /** 构造函数 **/
    public DbVersion() {
        this.dbType = MainDbType.Unknown;
    }

    /** 构造函数 **/
    public DbVersion(DbType dbType) {
        this.dbType = dbType;
    }

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

    /**
     * 比较版本号
     * 
     * @param versionString 目标版本号, <b>前2级必须是纯数字</b><br>
     *            4.3.20.RELEASE<br>
     *            1.2.8a<br>
     *            1.0.234_20200708001<br>
     *            1.0.0-R1<br>
     * @return 返回负数,0,正数, 分别代表当前对象小于等于大于指定版本
     * @see VersionCodeTools#compare(String, String)
     */
    public int versionCompareTo(String versionString) {
        VerifyTools.requireNotBlank(versionString, "versionString");
        String[] targets = VersionCodeTools.splitVersionString(versionString);
        // 第1级版本号与主版本号对比
        int firstDiff = compareVersionValue(this.majorVersion, targets[0], versionString);
        if (firstDiff != 0) {
            return firstDiff;
        }
        // 第2级版本号与次版本号对比
        String secondString = targets.length <= 1 ? null : targets[1];
        int secondDiff = compareVersionValue(this.minorVersion, secondString, versionString);
        if (secondDiff != 0) {
            return secondDiff;
        }
        // 后面的版本号, 与this.versionString对比
        String[] sources;
        if (VerifyTools.isBlank(this.versionString)) {
            sources = new String[0];
        } else {
            sources = VersionCodeTools.splitVersionString(this.versionString);
        }
        return VersionCodeTools.compareVersions(sources, targets, 2);
    }

    private static int compareVersionValue(int source, String target, String desc) {
        if (target == null) {
            // 0 = null; 1 > null 
            return source > 0 ? 1 : 0;
        } else {
            if (!StringTools.isDigit(target)) {
                throw new IllegalArgumentException("VersionStringFomatError: " + desc);
            }
        }
        return compareVersionNumber(source, Integer.parseInt(target));
    }

    private static int compareVersionNumber(int source, int target) {
        if (source > target) {
            return 1;
        } else if (source < target) {
            return -1;
        } else {
            return 0;
        }
    }
    
    /**
     * 当前数据库类型与版本是否满足指定的最低要求<br>
     * 先对比数据库类型, 如果一致再对比版本号<br>
     * 最低要求是以分号分隔的多个数据库类型与版本号, 当前版本满足任一条件即为匹配<br>
     * 如: MySql;MySQL;MariaDB;DB2;SqlServer:2008
     * 
     * @param minRequirement 最低要求
     * @return 是否匹配
     */
    public boolean matchesWith(String minRequirement) {
        if (VerifyTools.isBlank(minRequirement)) {
            return false;
        }
        // 按分号拆分, 满足任一条件即为匹配
        String[] items = StringTools.split(minRequirement, ';');
        for (String item : items) {
            if (itemMatchesWith(item)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean itemMatchesWith(String item) {
        int index = item.indexOf(':');
        if (index < 0) { // 只指定了类型
            return item.equals(dbType.name());
        } else if (index == 0) {
            // index=0, 是这种情况--> :1.0
            // 只指定了版本号: 任何类型, 版本号匹配都算?
            return this.versionCompareTo(item.substring(1)) >= 0;
        } else { // 指定了类型+版本号
            String type = item.substring(0, index);
            String code = item.substring(index + 1);
            return type.equals(dbType.name()) && this.versionCompareTo(code) >= 0;
        }
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
