package com.gitee.qdbp.jdbc.support;

import java.lang.reflect.Method;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.alibaba.druid.pool.DruidDataSource;
import com.gitee.qdbp.tools.crypto.GlobalCipherTools;
import com.gitee.qdbp.tools.files.PathTools;
import com.gitee.qdbp.tools.utils.ConvertTools;
import com.gitee.qdbp.tools.utils.PropertyTools;
import com.gitee.qdbp.tools.utils.ReflectTools;
import com.gitee.qdbp.tools.utils.StringTools;

/**
 * 简化配置的DruidDataSource, 切换数据库只需要修改一行配置(jdbc.xxx)<br>
 * 配置文件应分为两部分, 一是需要开发评估才能决定修改的, 二是运维人员即可修改的<br>
 * 关于数据库的配置, 用户名/密码/访问地址/数据库名是运维的事, 其他的协议类型/各种参数是开发的事<br>
 * 运维人员的这部分配置文件应该尽量集中到一个文件中<br>
 * <br>
 * 密码支持两种配置方式, 第1种是明文密码, 第2种是加密密码<br>
 * dbtype:username:password@address/dbname:schema // 明文密码<br>
 * dbtype:username@address/dbname:schema?EncryptedPassword // 加密密码<br>
 * 数据库密码加密: java -cp qdbp-able.jar com.gitee.qdbp.tools.crypto.GlobalCipherTools db password<br>
 * config第1段是dbtype.subtype, subtype可以没有. 例如mysql, mysql.8, oralce, oralce.sid<br>
 * 通过dbtype.subtype从properties之中自动查找jdbc.url/jdbc.params/jdbc.driver/jdbc.testquery<br>
 * 查找时优先查找jdbc.url.dbtype.subtype, 如果没有再查找jdbc.url.dbtype, 最后查找jdbc.url<br>
 * <pre>
## MySQL
jdbc.xxx = mysql:username:password@address/dbname
## MySQL(8.0的参数不同)
jdbc.xxx = mysql.8:username:password@address/dbname
## MySQL(加密的密码放在最后)
jdbc.xxx = mysql:username@address/dbname?EncryptedPassword
## Oralce-DbName
jdbc.xxx = oralce:username:password@address/dbname
## Oralce-SID
jdbc.xxx = oralce.sid:username:password@address/dbname
## DB2需要schema
jdbc.xxx = db2:username:password@127.0.0.1:50000/dbname:schema
## 内存模式,不需要username/password/address
jdbc.xxx = h2.mem@~/dbname

&lt;bean id="setting" class="org.springframework.beans.factory.config.PropertiesFactoryBean"&gt;
    &lt;property  name="fileEncoding" value="UTF-8" /&gt;
    &lt;property name="locations"&gt;
        &lt;list&gt;
            &lt;value&gt;classpath:settings/web/general.properties&lt;/value&gt;
            &lt;value&gt;classpath:settings/web/business.properties&lt;/value&gt;
            &lt;value&gt;classpath:setting.properties&lt;/value&gt;
        &lt;/list&gt;
    &lt;/property&gt;
&lt;/bean&gt;
&lt;bean class="org.springframework.beans.factory.config.PreferencesPlaceholderConfigurer"&gt;
    &lt;property name="properties" ref="setting" /&gt;
&lt;/bean&gt;
&lt;bean class="com.gitee.qdbp.base.settings.druid.AutoDruidDataSource" init-method="init" destroy-method="close"&gt;
    &lt;property name="properties" ref="setting" /&gt;
    &lt;property name="config" value="${jdbc.xxx}" /&gt;
    &lt;property name="url" value="auto" /&gt;
    &lt;property name="driverClassName" value="auto" /&gt;
    &lt;property name="validationQuery" value="auto" /&gt;
&lt;/bean&gt;
 * </pre>
 * 
 * @author zhaohuihua
 * @version 20200328
 */
public class AutoDruidDataSource extends DruidDataSource {

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

    // ## MySQL
    // # jdbc.xxx = mysql:username:password@127.0.0.1:3306/dbname
    // ## MySQL(加密的密码放在最后)
    // # jdbc.xxx = mysql:username@127.0.0.1:3306/dbname?EncryptedPassword
    // ## MariaDB
    // # jdbc.xxx = mariadb:username:password@127.0.0.1:3306/dbname
    // ## Oralce-DbName
    // # jdbc.xxx = oralce:username:password@127.0.0.1:1521/dbname
    // ## Oralce-SID
    // # jdbc.xxx = oralce.sid:username:password@127.0.0.1:1521/dbname
    // ## DB2 需要schema
    // # jdbc.xxx = db2:username:password@127.0.0.1:50000/dbname:schema
    // ## H2 嵌入式(不需要ip:port)
    // # jdbc.xxx = h2.embed:username:password@~/dbname
    // ## H2 TCP模式
    // # jdbc.xxx = h2.tcp:username:password@127.0.0.1:8082/dbname
    // ## H2 内存模式(不需要username/password/ip:port)
    // # jdbc.xxx = h2.mem@~/dbname
    // ## SqlServer
    // # jdbc.xxx = sqlserver:username:password@127.0.0.1:1433/dbname
    // ## SqlServer 老版本
    // # jdbc.xxx = sqlserver.2000:username:password@127.0.0.1:1433/dbname
    // ## PostgreSQL
    // # jdbc.xxx = postgresql:username:password@127.0.0.1:5432/dbname
    /** 解析数据库配置的正则表达式 **/
    private static final Pattern CONFIG = Pattern.compile(
        //    dbtype.subtype     : username       : password   @ address /   dbname    :   schema   ?EncryptedPassword
        "(\\w+?(?:\\.\\w+)?)(?:\\:([\\-\\.\\w]+))?(?:\\:(.+?))?@([^@/?]+)/([\\-\\.\\w]+)(?:\\:(.+?))?(?:\\?(.+))?");

    protected String dbconfig;
    protected Properties properties;
    protected String dbtype;
    protected String dbname;
    protected String dbschema;
    protected String dbaddress;

    /**
     * 设置配置参数<br>
     * 这个方法要在最前面调用, 因为配置信息中的参数优先级应低于直接通过setter方法设置的<br>
     * 在最前面调用, 后面通过setter方法设置的参数就能覆盖配置信息中的参数<br>
     * 这里面有2部分配置:<br>
     * 1. DruidDataSource连接池配置信息, 这部分在setProperties()时加载;<br>
     * 2. AutoDruidDataSource新增的, 与数据库类型相关的配置信息, 这部分在init()方法中通过findPropertyUseSuffix()调用<br>
     * 
     * @param properties 配置信息
     */
    public void setProperties(Properties properties) {
        // 查找默认配置文件
        Properties defaults = loadDefaultProperties();
        if (properties != null) {
            defaults.putAll(properties);
        }
        this.properties = defaults;

        // 加载配置信息中的连接池信息
        this.loadConfigFieldValue(properties);
    }

    // 加载配置信息中的连接池信息
    protected void loadConfigFieldValue(Properties properties) {
        // String excludeFields = "config,name,url,jdbcUrl,driverClassName,validationQuery,username,password";
        String excludeFields = "config,name,url,jdbcUrl";
        Map<String, ?> excludeMaps = ConvertTools.toKeyMaps(excludeFields);
        Method[] setters = ReflectTools.getAllSetter(DruidDataSource.class);
        String propertyKey = "jdbc.datasource.";
        for (Method setter : setters) {
            String methodName = setter.getName();
            String fieldName = methodName.substring(3);
            fieldName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
            if (excludeMaps.containsKey(fieldName)) {
                continue;
            }
            String configKey = propertyKey + fieldName;
            String configValue = properties.getProperty(configKey);
            if (configValue == null || configValue.length() == 0) {
                continue;
            }
            Class<?> paramType = setter.getParameterTypes()[0];
            Object realValue = null;
            if (paramType == String.class) {
                realValue = configValue;
            } else if (paramType == boolean.class || paramType == Boolean.class) {
                realValue = ConvertTools.toBoolean(configValue, null);
            } else if (paramType == int.class || paramType == Integer.class) {
                realValue = ConvertTools.toInteger(configValue, null);
            } else if (paramType == long.class || paramType == Long.class) {
                realValue = ConvertTools.toLong(configValue, null);
            } else if (paramType == double.class || paramType == Double.class) {
                realValue = ConvertTools.toDouble(configValue, null);
            } else if (paramType == float.class || paramType == Float.class) {
                realValue = ConvertTools.toFloat(configValue, null);
            }
            if (realValue != null) {
                ReflectTools.invokeMethod(this, setter, realValue);
            }
        }
    }

    protected Properties loadDefaultProperties() {
        // 查找默认配置文件
        // 1. {classpath}/settings/jdbc/druid.auto.properties<br>
        // 2. qdbc-jdbc-spring.jar!/settings/jdbc/druid.auto.properties<br>
        URL path = PathTools.findResource("settings/jdbc/druid.auto.properties", AutoDruidDataSource.class);
        return PropertyTools.load(path);
    }

    /**
     * 配置JDBC用户名密码连接地址数据库名称<br>
     * 密码支持两种配置方式, 第1种是明文密码, 第2种是加密密码<br>
     * dbtype:username:password@address/dbname:schema // 明文密码<br>
     * dbtype:username@address/dbname:schema?EncryptedPassword // 加密密码<br>
     * 
     * @param config
     */
    public void setConfig(String config) {
        if (inited) {
            throw new UnsupportedOperationException("Initialization is complete");
        }
        this.dbconfig = config;

        // 解析配置参数
        Matcher matcher = CONFIG.matcher(config.trim());
        if (!matcher.matches()) {
            String one = "dbtype:username:password@address/dbname";
            String two = "dbtype:username@address/dbname?EncryptedPassword";
            throw new IllegalArgumentException("Config string format error. format <1> " + one + " <2> " + two);
        }

        // dbtype:username:password@address/dbname:schema?EncryptedPassword
        int i = 1;
        dbtype = matcher.group(i++);
        String username = matcher.group(i++);
        String password = matcher.group(i++);
        dbaddress = matcher.group(i++);
        dbname = matcher.group(i++);
        dbschema = matcher.group(i++);
        String encrypted = matcher.group(i++);
        if (password != null && encrypted != null) {
            String desc = "password and EncryptedPassword coexistence is not allowed.";
            throw new IllegalArgumentException("Config string format error. " + desc);
        }

        this.setUsername(username);
        if (encrypted != null) {
            this.setPassword(GlobalCipherTools.decrypt("db", encrypted));
        } else {
            this.setPassword(password);
        }
    }

    @Override
    public void setUrl(String url) {
        if (!"auto".equalsIgnoreCase(url)) {
            String m = "SetUrl not supported, we will automatically find content from the properties";
            throw new UnsupportedOperationException(m);
        }
    }

    @Override
    public void setDriverClassName(String driverClassName) {
        if (!"auto".equalsIgnoreCase(driverClassName)) {
            String m = "SetDriverClassName not supported, we will automatically find content from the properties";
            throw new UnsupportedOperationException(m);
        }
    }

    @Override
    public void setValidationQuery(String validationQuery) {
        if (!"auto".equalsIgnoreCase(validationQuery)) {
            String m = "SetValidationQuery not supported, we will automatically find content from the properties";
            throw new UnsupportedOperationException(m);
        }
    }

    // 初始化
    public void init() throws SQLException {
        if (inited) {
            return;
        }
        if (properties == null) {
            properties = loadDefaultProperties();
        }
        if (dbconfig == null || dbconfig.trim().length() == 0) {
            throw new IllegalArgumentException("Missing argument for dbconfig");
        }
        String url = findPropertyUseSuffix("jdbc.url", true, true);
        String params = findPropertyUseSuffix("jdbc.params", true, false);
        String driver = findPropertyUseSuffix("jdbc.driver", false, false);
        if (params != null && params.length() > 0) {
            super.setUrl(url + params);
        } else {
            super.setUrl(url);
        }
        if (driver != null && driver.length() > 0) {
            super.setDriverClassName(driver);
        }

        // 处理特殊的Driver
        resolveSpecialDriver();
        super.init();
    }

    /**
     * 根据dbtype从配置项中查找配置内容<br>
     * 如key = jdbc.url, suffixes=x.y.z<br>
     * 取值顺序为: jdbc.url.x.y.z - jdbc.url.x.y - jdbc.url.x - jdbc.url
     * 
     * @param key KEY前缀
     * @param replacePlaceholder 是否替换占位符
     * @param throwOnNotFound 未找到配置时是否报错
     * @return 配置内容
     */
    protected String findPropertyUseSuffix(String key, boolean replacePlaceholder, boolean throwOnNotFound) {
        String suffixes = this.dbtype;
        char dot = '.';
        List<String> keys = new ArrayList<>();
        keys.add(key + dot + suffixes);
        int index = suffixes.length();
        while (true) {
            index = suffixes.lastIndexOf(dot, index - 1);
            if (index <= 0) {
                break;
            }
            keys.add(key + dot + suffixes.substring(0, index));
        }
        keys.add(key);

        for (String k : keys) {
            String value = PropertyTools.getString(properties, k, false);
            if (value == null) {
                continue;
            }
            if (value.length() > 0) {
                if (!replacePlaceholder) {
                    return value;
                } else {
                    return replacePlaceholder(value);
                }
            } else {
                if (throwOnNotFound) {
                    throw new IllegalArgumentException("Property value is blank, key=" + k);
                } else {
                    return null;
                }
            }
        }

        if (!throwOnNotFound) {
            return null;
        }

        throw new IllegalArgumentException("Property value not found: " + ConvertTools.joinToString(keys, " or "));
    }

    private String replacePlaceholder(String s) {
        if (s == null || s.indexOf('{') < 0 || s.indexOf('}') < 0) {
            return s;
        }
        return StringTools.format(s, "address", dbaddress, "dbname", dbname, "schema", dbschema);
    }

    /** 推断Driver(1.1.22以上版本不需要了,已贡献到主版本) **/
    protected void resolveSpecialDriver() {
        String driverClass = this.getDriverClassName();
        String jdbcUrl = this.getUrl();
        if ((driverClass == null || driverClass.length() == 0) && jdbcUrl != null && jdbcUrl.startsWith("jdbc:db2")) {
            // 优化DB2 Driver的推断逻辑, 参考自 https://www.cnblogs.com/lidg/articles/3588566.html
            // Resolve the DB2 driver from JDBC URL
            // Type2 COM.ibm.db2.jdbc.app.DB2Driver, url = jdbc:db2:databasename
            // Type3 COM.ibm.db2.jdbc.net.DB2Driver, url = jdbc:db2:ServerIP:6789:databasename
            // Type4 8.1+ com.ibm.db2.jcc.DB2Driver, url = jdbc:db2://ServerIP:50000/databasename
            String prefix = "jdbc:db2:";
            if (jdbcUrl.startsWith(prefix + "//")) { // Type4
                super.setDriverClassName("com.ibm.db2.jcc.DB2Driver");
            } else {
                String suffix = jdbcUrl.substring(prefix.length());
                if (suffix.indexOf(':') > 0) { // Type3
                    super.setDriverClassName("COM.ibm.db2.jdbc.net.DB2Driver");
                } else { // Type2
                    super.setDriverClassName("COM.ibm.db2.jdbc.app.DB2Driver");
                }
            }
        }
    }

}
