package com.gitee.qdbp.jdbc.support;

import java.net.URL;
import java.util.Properties;
import org.testng.Assert;
import org.testng.annotations.Test;
import com.gitee.qdbp.tools.files.PathTools;
import com.gitee.qdbp.tools.utils.PropertyTools;

@Test
public class AutoDruidRegexpTest {

    private Properties properties;

    public AutoDruidRegexpTest() {
        String path = "settings/jdbc/druid.auto.properties";
        URL url = PathTools.findResource(path, AutoDruidDataSource.class);
        properties = PropertyTools.load(url);
    }

    @Test
    public void testRegexp() {
        // Oracle-DbName
        test("oracle:username:password@127.0.0.1:1521/orcl", "username", "password",
            "jdbc:oracle:thin:@//127.0.0.1:1521/orcl");
        // Oracle-SID
        test("oracle.sid@127.0.0.1:1521/DbSid", null, null, "jdbc:oracle:thin:127.0.0.1:1521:DbSid");
        // Oracle-TNS
        test("oracle.tns@~/TnsName", null, null, "jdbc:oracle:thin:@TnsName");
        // MySQL
        test("mysql:username:password@127.0.0.1:3306/dbname", "username", "password",
            "jdbc:mysql://127.0.0.1:3306/dbname?tinyInt1isBit=false&useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&failOverReadOnly=false&useSSL=false");
        // MariaDB
        test("mariadb:username:password@127.0.0.1:3306/dbname", "username", "password",
            "jdbc:mariadb://127.0.0.1:3306/dbname?tinyInt1isBit=false&useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&failOverReadOnly=false&useSSL=false");
        // DB2 需要schema
        test("db2:username:password@127.0.0.1:50000/dbname:schemaname", "username", "password",
            "jdbc:db2://127.0.0.1:50000/dbname:currentSchema=schemaname;");
        // H2 TCP模式
        test("h2.tcp:username:password@127.0.0.1:8082/dbname", "username", "password",
            "jdbc:h2:tcp://127.0.0.1:8082/~/dbname;MODE=MYSQL;AUTO_RECONNECT=TRUE");
        // H2 嵌入式(不需要ip:port)
        test("h2.embed:username:password@~/dbname", "username", "password",
            "jdbc:h2:~/dbname;MODE=MYSQL;AUTO_RECONNECT=TRUE");
        // H2 内存模式(不需要username/password/ip:port)
        test("h2.mem@~/dbname", null, null, "jdbc:h2:mem:dbname;MODE=MYSQL;AUTO_RECONNECT=TRUE");
        // SqlServer
        test("sqlserver:username:password@127.0.0.1:1433/DB_BBMJ", "username", "password",
            "jdbc:sqlserver://127.0.0.1:1433;DatabaseName=DB_BBMJ");
        // SqlServer 老版本
        test("sqlserver.2000:username:password@127.0.0.1:1433/DB_BBMJ", "username", "password",
            "jdbc:microsoft:sqlserver://127.0.0.1:1433;DatabaseName=DB_BBMJ");
        // PostgreSQL
        test("postgresql:username:password@127.0.0.1:5432/dbname:schemaname", "username", "password",
            "jdbc:postgresql://127.0.0.1:5432/dbname?currentSchema=schemaname&useUnicode=true&characterEncoding=UTF-8");
        // SQLite 内存模式
        test("sqlite.mem@~/dbname", null, null, "jdbc:sqlite::memory:");
        // SQLite 文件模式(windows)
        test("sqlite.file@~/F:/sqlite/main.db", null, null, "jdbc:sqlite://F:/sqlite/main.db");
        // SQLite 文件模式(linux)
        test("sqlite.file@~/home/sqlite/main.db", null, null, "jdbc:sqlite://home/sqlite/main.db");
        // SQLite 类路径中的文件
        test("sqlite.res@~/settings/sqlite/main.db", null, null, "jdbc:sqlite::resource:settings/sqlite/main.db");
    }

    private void test(String config, String username, String password, String url) {
        @SuppressWarnings("resource")
        AutoDruidDataSource ds = new AutoDruidDataSource();
        ds.setProperties(properties);
        ds.setConfig(config);
        ds.initProperty();
        Assert.assertEquals(ds.getUsername(), username, "username");
        Assert.assertEquals(ds.getPassword(), password, "password");
        Assert.assertEquals(ds.getUrl(), url, "url");
    }
}
