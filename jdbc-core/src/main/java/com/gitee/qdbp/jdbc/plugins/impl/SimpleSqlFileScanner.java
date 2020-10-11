package com.gitee.qdbp.jdbc.plugins.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.gitee.qdbp.able.instance.ToStringComparator;
import com.gitee.qdbp.able.matches.FileMatcher;
import com.gitee.qdbp.able.matches.StringMatcher.LogicType;
import com.gitee.qdbp.able.matches.WrapFileMatcher;
import com.gitee.qdbp.jdbc.plugins.SqlFileScanner;
import com.gitee.qdbp.tools.files.PathTools;
import com.gitee.qdbp.tools.utils.ConvertTools;
import com.gitee.qdbp.tools.utils.StringTools;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * SQL模板文件扫描接口实现类
 *
 * @author zhaohuihua
 * @version 20200830
 */
public class SimpleSqlFileScanner implements SqlFileScanner {

    private static Logger log = LoggerFactory.getLogger(SimpleSqlFileScanner.class);

    /** 待扫描的文件夹, 多个以逗号分隔 **/
    private String folders;
    /** 待扫描的文件名过滤规则, 多个以逗号分隔, 如 *.sql,*.stpl **/
    private String filters;

    public SimpleSqlFileScanner() {
    }

    public SimpleSqlFileScanner(String folders, String filters) {
        this.folders = folders;
        this.filters = filters;
    }

    @Override
    public List<URL> scanSqlFiles() {
        String folder = VerifyTools.nvl(this.folders, "settings/sqls/");
        String[] folders = StringTools.split(folder, ',');
        String filter = VerifyTools.nvl(this.filters, "*.sql");
        FileMatcher matcher = WrapFileMatcher.parseMatchers(filter, LogicType.OR, "name", "ant", ',', '\n');
        Date startTime = new Date();
        List<URL> urls = new ArrayList<>();
        for (String item : folders) {
            List<URL> temp = PathTools.scanResources(item, matcher);
            if (temp != null && !temp.isEmpty()) {
                urls.addAll(temp);
            }
        }
        Collections.sort(urls, ToStringComparator.INSTANCE);
        if (log.isInfoEnabled()) {
            String msg = "Success to scan sql templates, elapsed time {}, total of {} files were found.";
            log.info(msg, ConvertTools.toDuration(startTime, true), urls.size());
        }
        return urls;
    }

    /** 待扫描的文件夹, 多个以逗号分隔 **/
    public String getFolders() {
        return folders;
    }

    /** 待扫描的文件夹, 多个以逗号分隔 **/
    public void setFolders(String folders) {
        this.folders = folders;
    }

    /** 待扫描的文件名过滤规则, 多个以逗号分隔, 如 *.sql,*.stpl **/
    public String getFilters() {
        return filters;
    }

    /** 待扫描的文件名过滤规则, 多个以逗号分隔, 如 *.sql,*.stpl **/
    public void setFilters(String filters) {
        this.filters = filters;
    }

}
