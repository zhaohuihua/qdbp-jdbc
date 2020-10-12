package com.gitee.qdbp.jdbc.plugins;

import java.net.URL;
import java.util.List;

/**
 * SQL模板文件扫描接口
 *
 * @author zhaohuihua
 * @version 20200830
 * @since 3.2.0
 */
public interface SqlFileScanner {

    /**
     * 扫描SQL模板文件
     * 
     * @return SQL模板文件列表
     */
    List<URL> scanSqlFiles();
}
