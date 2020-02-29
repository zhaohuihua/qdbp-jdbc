package com.gitee.qdbp.base.settings.druid;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.filter.config.ConfigFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.gitee.qdbp.tools.utils.StringTools;

/**
 * 简化配置的DruidDataSource<br>
 * 配置文件应分为两部分, 一是需要开发评估才能决定修改的, 二是运维人员即可修改的<br>
 * 关于数据库的配置, 用户名/密码/访问地址/数据库名是运维的事, 其他的协议类型/各种参数是开发的事<br>
 * 运维人员的这部分配置文件应该尽量集中到一个文件中<br>
 * 数据库密码加密: java -cp druid-1.0.5.jar com.alibaba.druid.filter.config.ConfigTools password<br>
 * <pre>
jdbc.xxx = username@address/dbname?Base64Password
&lt;!-- MySQL --&gt;
&lt;bean class="com.gitee.qdbp.base.settings.druid.EasyDruidDataSource" init-method="init" destroy-method="close"&gt;
    &lt;property name="settings" value="${jdbc.xxx}" /&gt;
    &lt;property name="url" value="jdbc:mysql://{address}/{dbname}" /&gt;
    &lt;property name="params" value="?useUnicode=true&amp;amp;characterEncoding=UTF-8&amp;amp;autoReconnect=true&amp;amp;failOverReadOnly=false" /&gt;
&lt;/bean&gt;
&lt;!-- Oracle-DbName --&gt;
&lt;bean class="com.gitee.qdbp.base.settings.druid.EasyDruidDataSource" init-method="init" destroy-method="close"&gt;
    &lt;property name="settings" value="${jdbc.xxx}" /&gt;
    &lt;property name="url" value="jdbc:oracle:thin:@//{address}/{dbname}" /&gt;
&lt;/bean&gt;
&lt;!-- Oracle-SID --&gt;
&lt;bean class="com.gitee.qdbp.base.settings.druid.EasyDruidDataSource" init-method="init" destroy-method="close"&gt;
    &lt;property name="settings" value="${jdbc.xxx}" /&gt;
    &lt;property name="url" value="jdbc:oracle:thin:{address}:{dbname}" /&gt;
&lt;/bean&gt;
&lt;!-- H2 TCP --&gt;
&lt;bean class="com.gitee.qdbp.base.settings.druid.EasyDruidDataSource" init-method="init" destroy-method="close"&gt;
    &lt;property name="settings" value="${jdbc.xxx}" /&gt;
    &lt;property name="url" value="jdbc:h2:tcp://{address}/~/{dbname}" /&gt;
    &lt;property name="params" value=";MODE=MYSQL;AUTO_RECONNECT=TRUE" /&gt;
&lt;/bean&gt;
&lt;!-- SQLServer --&gt;
&lt;bean class="com.gitee.qdbp.base.settings.druid.EasyDruidDataSource" init-method="init" destroy-method="close"&gt;
    &lt;property name="settings" value="${jdbc.xxx}" /&gt;
    &lt;property name="url" value="jdbc:sqlserver://{address};DatabaseName={dbname}" /&gt;
    &lt;property name="params" value=";MODE=MYSQL;AUTO_RECONNECT=TRUE" /&gt;
&lt;/bean&gt;

jdbc.xxx = username@~/dbname?Base64Password
&lt;!-- Oracle-TNS --&gt;
&lt;bean class="com.gitee.qdbp.base.settings.druid.EasyDruidDataSource" init-method="init" destroy-method="close"&gt;
    &lt;property name="settings" value="${jdbc.xxx}" /&gt;
    &lt;property name="url" value="jdbc:oracle:thin:@{dbname}" /&gt;
&lt;/bean&gt;
&lt;!-- H2 Embedded --&gt;
&lt;bean class="com.gitee.qdbp.base.settings.druid.EasyDruidDataSource" init-method="init" destroy-method="close"&gt;
    &lt;property name="settings" value="${jdbc.xxx}" /&gt;
    &lt;property name="url" value="jdbc:h2:~/{dbname}" /&gt;
    &lt;property name="params" value=";MODE=MYSQL;AUTO_RECONNECT=TRUE" /&gt;
&lt;/bean&gt;
 * </pre>
 * 
 * @author zhaohuihua
 * @version 170607
 * @since V3.1.0 仅支持MySQL之类protocol://address/dbname?params格式(settings+params)
 * @since V3.3.0 适应各种数据库JDBC配置(settings+url+params)
 */
public class EasyDruidDataSource extends DruidDataSource {

    /** 版本序列号 **/
    private static final long serialVersionUID = 1L;

    // jdbc:oracle:thin:@//192.168.1.218:1521/DbName
    // jdbc:oracle:thin:192.168.1.218:1521:DbSid
    // jdbc:oracle:thin:@TnsName
    // jdbc:db2://192.168.1.218:60000/DbName
    // jdbc:db2://192.168.1.218:60000/DbName:currentSchema=SchemaName;
    // jdbc:h2:~/DbName // 嵌入式
    // jdbc:h2:mem:DbName // 内存数据库
    // jdbc:h2:tcp://192.168.1.218:8084/~/DbName
    // jdbc:sqlserver://192.168.1.218:1433;DatabaseName=DB_BBMJ

    /** 解析数据库配置的正则表达式 **/
    private static final Pattern SETTINGS = Pattern.compile(
        //    jdbc         :    mysql       ://      username:password      @       ip:port             /   db-name     ?Base64Password
        "([\\-\\.\\w]+(?:\\:[\\-\\.\\w]+)+\\://)?([\\-\\.\\w]+)(?:\\:(.+?))?@([~\\-\\.\\w]+(?:\\:\\d+)?)/([\\-\\.\\w]+)(?:\\?(.+))?");

    private Boolean isNeedDecrypt;

    private String protocol;
    private String address;
    private String dbname;
    private String params;

    /**
     * 配置JDBC用户名密码连接地址数据库名称<br>
     * 支持两种格式, 第1种是明文密码, 第2种是加密密码<br>
     * username:password@ip:port/db-name // 明文密码<br>
     * username@ip:port/db-name?Base64Password // 加密密码<br>
     * 
     * @param settings
     */
    public void setSettings(String settings) {
        if (inited) {
            throw new UnsupportedOperationException();
        }

        // 解析配置参数
        Matcher matcher = SETTINGS.matcher(settings.trim());
        if (!matcher.matches()) {
            String one = "username:password@address/dbname";
            String two = "username@address/dbname?Base64Password";
            throw new IllegalArgumentException("Settings string format error. format <1> " + one + " <2> " + two);
        }

        int i = 1;
        protocol = matcher.group(i++);
        String username = matcher.group(i++);
        String password = matcher.group(i++);
        address = matcher.group(i++);
        dbname = matcher.group(i++);
        String encrypted = matcher.group(i++);
        if (password != null && encrypted != null) {
            String desc = "password and Base64Password coexistence is not allowed.";
            throw new IllegalArgumentException("Settings string format error. " + desc);
        }

        this.setUsername(username);
        if (encrypted != null) {
            this.isNeedDecrypt = true;
            this.setPassword(encrypted);
        } else {
            this.isNeedDecrypt = false;
            this.setPassword(password);
        }
    }

    /**
     * 配置JDBC的URL连接参数<br>
     * ?useUnicode=true&amp;characterEncoding=UTF-8&amp;autoReconnect=true&amp;failOverReadOnly=false<br>
     * 
     * @param params
     */
    public void setParams(String params) {
        if (inited) {
            throw new UnsupportedOperationException();
        }

        params = params.trim();
        if (params.length() > 0 && Character.isAlphabetic(params.charAt(0))) {
            params = "?" + params; // 兼容旧版本
        }
        this.params = params;
    }

    // 初始化
    public void init() throws SQLException {

        String s = this.getUrl();
        if (s == null && protocol != null) {
            s = protocol + "{address}/{dbname}"; // 兼容旧版本
        }

        // 替换占位符
        if (s != null && s.indexOf('{') > 0 && s.indexOf('}') > 0 && address != null && dbname != null) {
            String url = StringTools.format(s, "address", address, "dbname", dbname);
            if (params != null) {
                this.setUrl(url + params);
            } else {
                this.setUrl(url);
            }
        }

        // 判断数据库密码是否加密
        if (isNeedDecrypt != null) {
            String value = String.valueOf(isNeedDecrypt.booleanValue());
            this.connectProperties.put("config.decrypt", value);
            for (Filter i : this.filters) {
                // 已知config.decrypt是给ConfigFilter用的
                if (i instanceof ConfigFilter) {
                    // 尽管目前ConfigFilter.configFromProperties是个空实现, 还是调一下
                    i.configFromProperties(this.connectProperties);
                }
            }
        }
        String driverClass = this.getDriverClassName();
        String jdbcUrl = this.getUrl();
        if (driverClass == null && jdbcUrl != null && jdbcUrl.startsWith("jdbc:db2")) {
            // Resolve the DB2 driver from JDBC URL
            // Type2 COM.ibm.db2.jdbc.app.DB2Driver, url = jdbc:db2:databasename
            // Type3 COM.ibm.db2.jdbc.net.DB2Driver, url = jdbc:db2:ServerIP:6789:databasename
            // Type4 8.1+ com.ibm.db2.jcc.DB2Driver, url = jdbc:db2://ServerIP:50000/databasename
            String prefix = "jdbc:db2:";
            if (jdbcUrl.startsWith(prefix + "//")) { // Type4
                this.setDriverClassName("com.ibm.db2.jcc.DB2Driver");
            } else {
                String suffix = jdbcUrl.substring(prefix.length());
                if (suffix.indexOf(':') > 0) { // Type3
                    this.setDriverClassName("COM.ibm.db2.jdbc.net.DB2Driver");
                } else { // Type2
                    this.setDriverClassName("COM.ibm.db2.jdbc.app.DB2Driver");
                }
            }
        }
        super.init();
    }

    public static void main(String[] args) {
        String prefix = "jdbc:db2:";
        System.out.println("jdbc:db2:ServerIP:6789:databasename".substring(prefix.length()));
    }
}
