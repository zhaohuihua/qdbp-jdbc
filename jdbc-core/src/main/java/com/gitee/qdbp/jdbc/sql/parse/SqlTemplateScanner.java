package com.gitee.qdbp.jdbc.sql.parse;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.gitee.qdbp.able.instance.ToStringComparator;
import com.gitee.qdbp.staticize.common.IMetaData;
import com.gitee.qdbp.tools.files.PathTools;
import com.gitee.qdbp.tools.utils.ConvertTools;

/**
 * SQL模板扫描
 *
 * @author zhaohuihua
 * @version 20200817
 */
class SqlTemplateScanner {

    private static Logger log = LoggerFactory.getLogger(SqlTemplateScanner.class);

    public Map<String, IMetaData> scanSqlTemplates(String folder) {
        Date startTime = new Date();
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

        Map<String, IMetaData> tagDataMaps = parser.parseCachedSqlTemplates();
        if (log.isInfoEnabled()) {
            String msg = "Success to scan and parse sql templates, elapsed time {}, "
                    + "total of {} files and {} fragments were found.";
            log.info(msg, ConvertTools.toDuration(startTime), urls.size(), tagDataMaps.size());
        }
        return tagDataMaps;
    }

}
