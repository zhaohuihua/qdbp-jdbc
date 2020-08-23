package com.gitee.qdbp.jdbc.sql.mapper;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.gitee.qdbp.able.instance.ToStringComparator;
import com.gitee.qdbp.tools.files.PathTools;

/**
 * SQL模板扫描
 *
 * @author zhaohuihua
 * @version 20200817
 */
class SqlTemplateScanner {

    private static Logger log = LoggerFactory.getLogger(SqlTemplateScanner.class);

    public void scanSqlTemplates(String folder) {
        List<URL> urls = PathTools.scanResources(folder, "*.sql");
        Collections.sort(urls, ToStringComparator.INSTANCE);

        SqlTemplateParser parser = new SqlTemplateParser();
        for (URL url : urls) {
            String absolutePath = PathTools.toUriPath(url);
            try {
                String sqlContent = PathTools.downloadString(url);
                parser.parseSqlContent(absolutePath, sqlContent);
            } catch (IOException e) {
                log.warn("Failed to read sql template: {}", absolutePath, e);
            } catch (Exception e) {
                log.warn("Failed to parse sql template: {}", absolutePath, e);
            }
        }
    }

}
