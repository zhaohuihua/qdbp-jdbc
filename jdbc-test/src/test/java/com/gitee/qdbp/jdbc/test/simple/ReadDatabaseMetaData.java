package com.gitee.qdbp.jdbc.test.simple;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import com.alibaba.druid.pool.DruidDataSource;
import com.gitee.qdbp.jdbc.model.DbVersion;
import com.gitee.qdbp.jdbc.plugins.DbPluginContainer;
import com.gitee.qdbp.jdbc.plugins.DbVersionFinder;
import com.gitee.qdbp.jdbc.support.AutoDruidDataSource;
import com.gitee.qdbp.tools.files.FileTools;
import com.gitee.qdbp.tools.utils.ConvertTools;
import com.gitee.qdbp.tools.utils.ReflectTools;
import com.gitee.qdbp.tools.utils.StringTools;

/**
 * 读取数据库信息
 *
 * @author zhaohuihua
 * @version 20201016
 */
public class ReadDatabaseMetaData {

    public static void main(String[] args) throws IOException, SQLException {
        String jdbcUrl = "h2.embed@~/qdbctest";
        // String jdbcUrl = "sqlite.file@~/F:/tools/sqlite/main.db";
        // String jdbcUrl = "mysql:develop:dev2018lop@192.168.70.225:3306/joyin-tagging";
        // String jdbcUrl = "mysql:develop:dev2018lop@127.0.0.1:3306/qdbp-general";
        // String jdbcUrl = "oracle:qdbctest:qdbctest@192.168.70.195:1521/plat";
        // String jdbcUrl = "db2:db2inst1:123@192.168.90.7:60006/platform:PLATFORM";
        // String jdbcUrl = "postgresql:username:password@127.0.0.1:5432/dbname:schema";
        // String jdbcUrl = "sqlserver:username:password@127.0.0.1:1433/dbname";
        // String jdbcUrl = "sqlserver.2000:username:password@127.0.0.1:1433/dbname";

        int keyMaxLength = 0;
        for (String method : METHODS) {
            String key = StringTools.removePrefix(method, "get");
            if (keyMaxLength < key.length()) {
                keyMaxLength = key.length();
            }
        }

        StringBuilder messages = new StringBuilder();
        DbVersion version = null;
        try (DruidDataSource datasource = AutoDruidDataSource.buildWith(jdbcUrl);
                Connection connection = datasource.getConnection()) {
            try {
                DbVersionFinder finder = DbPluginContainer.defaults().getDbVersionFinder();
                version = finder.findDbVersion(datasource);
            } catch (Exception e) {
                System.out.println("Failed to get database version. " + e.toString());
            }
            DatabaseMetaData metadata = connection.getMetaData();
            for (String method : METHODS) {
                Object value = ReflectTools.invokeMethod(metadata, method);
                if (value == null) {
                    continue;
                }
                String key = StringTools.removePrefix(method, "get");
                messages.append(StringTools.pad(key, keyMaxLength)).append(" = ");
                if (value instanceof Collection || value.getClass().isArray()) {
                    List<Object> values = ConvertTools.parseList(value);
                    messages.append(ConvertTools.joinToString(values, true)).append('\n');
                } else {
                    messages.append(value).append('\n');
                }
            }
        }
        String fileName = "DatabaseMetaData.txt";
        if (version != null) {
            fileName = "DatabaseMetaData." + version.toVersionString() + ".txt";
        }
        File file = new File(fileName);
        FileTools.saveFile(messages.toString(), file);
        System.out.println(file.getAbsolutePath());
        System.out.println(messages.toString());
    }

    // @formatter:off
    private static String[] METHODS = new String[] {
            "getURL",
            "getUserName",
            "getDatabaseProductName",
            "getDatabaseProductVersion",
            "getDatabaseMajorVersion",
            "getDatabaseMinorVersion",
            "getDriverName",
            "getDriverVersion",
            "getDriverMajorVersion",
            "getDriverMinorVersion",
            "getJDBCMajorVersion",
            "getJDBCMinorVersion",
            "getIdentifierQuoteString",
            "getSearchStringEscape",
            "getExtraNameCharacters",
            "getSQLKeywords",
            "getNumericFunctions",
            "getStringFunctions",
            "getSystemFunctions",
            "getTimeDateFunctions"
    };
    // @formatter:on
}
