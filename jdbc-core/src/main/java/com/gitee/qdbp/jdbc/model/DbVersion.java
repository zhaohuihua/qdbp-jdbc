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
    /** 版本编号 (由数字/字母/点/横杠/下划线组成的标准版本编号, 前两段必须与主版本号/次版本号保持一致) **/
    private String versionCode;
    /** 版本字符串 (描述信息) **/
    // 如MySQL.8返回的是8.0.13; DB2.10.5返回的是SQL10051; 
    // Oralce.12c返回的是Oracle Database 12c Enterprise Edition Release 12.2.0.1.0 - 64bit Production
    private String versionString;
    /** 主版本号 **/
    private Integer majorVersion = null;
    /** 次版本号 **/
    private Integer minorVersion = null;

    /** 构造函数 **/
    public DbVersion() {
        this.dbType = MainDbType.Unknown;
    }

    /** 构造函数 **/
    public DbVersion(DbType dbType) {
        this.dbType = dbType;
    }

    /** 构造函数 **/
    public DbVersion(DbType dbType, String versionCode) {
        this.dbType = dbType;
        this.versionCode = versionCode;
    }

    /** 数据库类型 **/
    public DbType getDbType() {
        return dbType;
    }

    /** 数据库类型 **/
    public void setDbType(DbType dbType) {
        this.dbType = dbType;
    }

    /** 获取版本编号 (由数字/字母/点/横杠/下划线组成的标准版本编号) **/
    public String getVersionCode() {
        return versionCode;
    }

    /**
     * 设置版本编号
     * 
     * @param versionCode 由数字/字母/点/横杠/下划线组成的标准版本编号<br>
     *            <b>前2级必须是纯数字</b><br>
     *            4.3.20.RELEASE<br>
     *            1.2.8a<br>
     *            1.0.234_20200708001<br>
     *            1.0.0-R1<br>
     */
    public void setVersionCode(String versionCode) {
        if (versionCode == null) {
            this.versionCode = null;
        } else {
            if (!isValidVersionCode(versionCode)) {
                throw new IllegalArgumentException("Version code format error: " + versionCode);
            }
            this.versionCode = versionCode;
        }
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
    public Integer getMajorVersion() {
        return majorVersion;
    }

    /** 主版本号 **/
    public void setMajorVersion(Integer majorVersion) {
        this.majorVersion = majorVersion;
    }

    /** 次版本号 **/
    public Integer getMinorVersion() {
        return minorVersion;
    }

    /** 次版本号 **/
    public void setMinorVersion(Integer minorVersion) {
        this.minorVersion = minorVersion;
    }

    /**
     * 比较版本号
     * 
     * @param targetVersionCode 目标版本号, <b>前2级必须是纯数字</b><br>
     *            4.3.20.RELEASE<br>
     *            1.2.8a<br>
     *            1.0.234_20200708001<br>
     *            1.0.0-R1<br>
     * @return 返回负数,0,正数, 分别代表当前对象小于等于大于指定版本
     * @see VersionCodeTools#compare(String, String)
     */
    public int versionCompareTo(String targetVersionCode) {
        VerifyTools.requireNotBlank(targetVersionCode, "targetVersionCode");
        if (VerifyTools.isNotBlank(this.versionCode)) {
            return VersionCodeTools.compare(this.versionCode, targetVersionCode);
        }

        String[] targets = VersionCodeTools.splitVersionString(targetVersionCode);
        // 第1级版本号与主版本号对比
        int majorDiff = compareVersionValue(this.majorVersion, targets[0], targetVersionCode);
        if (majorDiff != 0) {
            return majorDiff;
        }
        // 第2级版本号与次版本号对比
        String minorTarget = targets.length <= 1 ? null : targets[1];
        int minorDiff = compareVersionValue(this.minorVersion, minorTarget, targetVersionCode);
        if (minorDiff != 0) {
            return minorDiff;
        }
        // 后面的版本号, 只要不是对方不是全0, 都算对方大
        String[] sources = new String[0];
        return VersionCodeTools.compareVersions(sources, targets, 2);
    }

    private static int compareVersionValue(Integer source, String target, String desc) {
        if (source == null) {
            source = 0;
        }
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
     * 如: mysql,mysql,mariadb.10.2.2,db2,sqlserver.2008
     * 
     * @param minRequirement 最低要求
     * @return 是否匹配
     */
    public boolean matchesWith(String minRequirement) {
        if (VerifyTools.isBlank(minRequirement)) {
            return false;
        }
        // 按分号拆分, 满足任一条件即为匹配
        String[] items = StringTools.split(minRequirement, ',');
        for (String item : items) {
            if (itemMatchesWith(item)) {
                return true;
            }
        }
        return false;
    }

    private boolean itemMatchesWith(String item) {
        int index = item.indexOf('.');
        if (index < 0) { // 只指定了类型
            return item.equalsIgnoreCase(dbType.name());
        } else if (index == 0) {
            // index=0, 是这种情况--> .1.0
            // 只指定了版本号: 任何类型, 版本号匹配都算?
            return this.versionCompareTo(item.substring(1)) >= 0;
        } else { // 指定了类型+版本号
            String type = item.substring(0, index);
            String code = item.substring(index + 1);
            return type.equalsIgnoreCase(dbType.name()) && this.versionCompareTo(code) >= 0;
        }
    }

    @Override
    public String toString() {
        return toString(true);
    }

    public String toVersionString() {
        return toString(false);
    }

    private String toString(boolean details) {
        if (dbType == null) {
            return "null";
        }
        StringBuilder buffer = new StringBuilder();
        if (details) {
            buffer.append(dbType.name());
        } else {
            buffer.append(dbType.name().toLowerCase());
        }
        if (this.versionCode != null) {
            buffer.append('.').append(this.versionCode);
        } else {
            if (this.majorVersion != null && this.majorVersion >= 0) {
                buffer.append('.').append(this.majorVersion);
                if (this.minorVersion != null && this.minorVersion >= 0) {
                    buffer.append('.').append(this.minorVersion);
                }
            }
        }
        if (details && VerifyTools.isNotBlank(versionString)) {
            buffer.append('(').append(versionString).append(')');
        }
        return buffer.toString();
    }

    /**
     * 判断是不是有效的版本号
     * 
     * @param versionCode 由数字/字母/点/横杠/下划线组成的标准版本编号<br>
     *            <b>前2级必须是以点作为分隔符的纯数字</b><br>
     *            4.3.20.RELEASE<br>
     *            1.2.8a<br>
     *            1.0.234_20200708001<br>
     *            1.0.0-R1<br>
     * @return 是否有效
     */
    public static boolean isValidVersionCode(String versionCode) {
        VerifyTools.requireNotBlank(versionCode, "versionCode");
        for (int i = 0, z = versionCode.length(); i < z; i++) {
            char c = versionCode.charAt(i);
            // 数字/字母
            if (!(c >= '0' && c <= '9' || c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z'
            // 点/横杠/下划线
                    || c == '.' || c == '-' || c == '_')) {
                return false;
            }
        }
        // 前2级必须是以点作为分隔符的纯数字
        String[] parts = StringTools.split(versionCode, '.');
        for (int i = 0; i < 2 && i < parts.length; i++) {
            String part = parts[i];
            if (!StringTools.isDigit(part)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断是不是数字的版本号
     * 
     * @param versionCode 由数字/点/组成的数字版本编号<br>
     *            4.3.20<br>
     *            1.2.8<br>
     *            1.0.234<br>
     *            12.2.0.1.0<br>
     * @return 是否有效
     */
    public static boolean isDigitVersionCode(String versionCode) {
        VerifyTools.requireNotBlank(versionCode, "versionCode");
        for (int i = 0, z = versionCode.length(); i < z; i++) {
            char c = versionCode.charAt(i);
            if (!(c >= '0' && c <= '9' || c == '.')) {
                return false;
            }
        }
        String[] parts = StringTools.split(versionCode, '.');
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (!StringTools.isDigit(part)) {
                return false;
            }
        }
        return true;
    }
}
