package com.gitee.qdbp.jdbc.sql.parse;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.gitee.qdbp.able.exception.ServiceException;
import com.gitee.qdbp.jdbc.exception.DbErrorCode;
import com.gitee.qdbp.jdbc.model.DbType;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.plugins.SqlFileScanner;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.utils.DbTools;
import com.gitee.qdbp.staticize.common.IMetaData;
import com.gitee.qdbp.staticize.exception.TagException;
import com.gitee.qdbp.staticize.tags.base.Taglib;
import com.gitee.qdbp.tools.files.PathTools;
import com.gitee.qdbp.tools.utils.ConvertTools;

/**
 * Sql片断的容器
 *
 * @author zhaohuihua
 * @version 20200830
 */
public class SqlFragmentContainer {

    private static Logger log = LoggerFactory.getLogger(SqlFileScanner.class);

    private static final SqlFragmentContainer DEFAULTS = new SqlFragmentContainer();

    public static SqlFragmentContainer defaults() {
        return DEFAULTS;
    }

    private boolean scaned = false;
    private Map<String, IMetaData> cache = new ConcurrentHashMap<>();

    private SqlFragmentContainer() {
    }

    /**
     * 注册SQL模板标签元数据树
     * 
     * @param sqlId SQL编号
     * @param dbType 数据库类型
     * @param data 解析后的模板标签元数据树
     */
    protected void register(String sqlId, DbType dbType, IMetaData data) {
        String sqlKey = sqlId + '(' + (dbType == null ? "*" : dbType.name().toLowerCase()) + ')';
        this.cache.put(sqlKey, data);
    }

    /**
     * 查找SQL模板标签元数据树
     * 
     * @param sqlId SQL编号
     * @param dbType 数据库类型
     * @return 标签元数据树
     */
    protected IMetaData find(String sqlId, DbType dbType) {
        this.scanSqlFiles();
        String sqlKey = sqlId + '(' + dbType.name().toLowerCase() + ')';
        IMetaData sqlData = this.cache.get(sqlKey);
        if (sqlData == null) {
            sqlData = this.cache.get(sqlId + "(*)");
        }
        if (sqlData != null) {
            return sqlData;
        } else {
            String details = "sqlId=" + sqlId + ", dbType=" + dbType.name().toLowerCase();
            throw new ServiceException(DbErrorCode.DB_SQL_FRAGMENT_NOT_FOUND, details);
        }
    }

    /** 根据SqlId从缓存中获取SQL模板, 渲染为SqlBuffer对象 **/
    public SqlBuffer render(String sqlId, Map<String, Object> data, SqlDialect dialect) {
        IMetaData tags = find(sqlId, dialect.getDbVersion().getDbType());
        return publish(tags, data, dialect);
    }

    /** 解析SQL模板内容, 渲染为SqlBuffer对象 **/
    public SqlBuffer parse(String sqlString, Map<String, Object> data, SqlDialect dialect) {
        IMetaData tags = SqlStringParser.parseSqlString(sqlString);
        return publish(tags, data, dialect);
    }

    protected SqlBuffer publish(IMetaData tags, Map<String, Object> data, SqlDialect dialect) {
        SqlBufferPublisher publisher = new SqlBufferPublisher(tags);
        try {
            return publisher.publish(data, dialect);
        } catch (TagException e) {
            throw new ServiceException(DbErrorCode.DB_SQL_FRAGMENT_RENDER_ERROR, e);
        } catch (IOException e) {
            throw new ServiceException(DbErrorCode.DB_SQL_FRAGMENT_RENDER_ERROR, e);
        }
    }

    /** 扫描SQL模板文件 **/
    public void scanSqlFiles() {
        if (this.scaned) {
            return;
        }
        this.doScanSqlFiles();
        this.scaned = true;
    }

    /** 扫描SQL模板文件 **/
    private synchronized void doScanSqlFiles() {
        if (this.scaned) {
            return;
        }
        SqlFileScanner scanner = DbTools.getSqlFileScanner();
        List<URL> urls = scanner.scanSqlFiles();

        Date startTime = new Date();
        List<DbType> dbTypes = DbTools.getAvailableDbTypes();
        Taglib taglib = DbTools.getSqlTaglib();
        SqlFragmentParser parser = new SqlFragmentParser(taglib, dbTypes);
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

        Map<String, IMetaData> tagDataMaps = parser.parseCachedSqlFragments();
        if (log.isInfoEnabled()) {
            String msg = "Success to parse sql templates, elapsed time {}, total of {} files and {} fragments.";
            log.info(msg, ConvertTools.toDuration(startTime), urls.size(), tagDataMaps.size());
        }
        this.cache.putAll(tagDataMaps);
    }
}
