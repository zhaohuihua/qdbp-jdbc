package com.gitee.qdbp.jdbc.sql.parse;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.gitee.qdbp.able.beans.KeyString;
import com.gitee.qdbp.able.exception.ServiceException;
import com.gitee.qdbp.jdbc.exception.DbErrorCode;
import com.gitee.qdbp.jdbc.model.DbType;
import com.gitee.qdbp.jdbc.model.DbVersion;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.plugins.SqlFileScanner;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.utils.DbTools;
import com.gitee.qdbp.staticize.common.IMetaData;
import com.gitee.qdbp.staticize.exception.TagException;
import com.gitee.qdbp.staticize.tags.base.Taglib;
import com.gitee.qdbp.tools.files.PathTools;
import com.gitee.qdbp.tools.utils.ConvertTools;
import com.gitee.qdbp.tools.utils.StringTools;
import com.gitee.qdbp.tools.utils.VersionCodeTools;

/**
 * Sql片断的容器
 *
 * @author zhaohuihua
 * @version 20200830
 * @since 3.2.0
 */
public class SqlFragmentContainer {

    private static Logger log = LoggerFactory.getLogger(SqlFileScanner.class);

    private static final SqlFragmentContainer DEFAULTS = new SqlFragmentContainer();

    public static SqlFragmentContainer defaults() {
        return DEFAULTS;
    }

    private boolean scaned = false;

    /** 未指定数据库类型的SQL模板 **/
    // key=sqlId
    private Map<String, IMetaData> untypedCache = new HashMap<>();
    /** 指定了数据库类型的SQL模板 **/
    // key=sqlId(dbType), value=[{version,IMetaData}], value按版本号从大到小排列, 无版本号的放最后
    private Map<String, List<TagData>> typedCache = new HashMap<>();

    private SqlFragmentContainer() {
    }

    /**
     * 注册SQL模板标签元数据树
     * 
     * @param sqlId SQL编号
     * @param supports 支持哪些数据库版本
     * @param data 解析后的模板标签元数据树
     */
    protected void register(String sqlId, String supports, IMetaData data) {
        if (supports == null || "*".equals(supports)) {
            if (!untypedCache.containsKey(sqlId)) {
                // 存入未指定数据库类型的SQL模板容器
                untypedCache.put(sqlId, data);
            }
        } else {
            // <supports>mysql.8,mariadb.10.2.2,postgresql,db2,sqlserver,sqlite.3.8.3</supports>
            String[] supportArray = StringTools.split(supports, ',');
            for (String supportItem : supportArray) {
                if (supportItem.length() == 0) {
                    continue;
                }
                int dotIndex = supportItem.indexOf('.');
                String dbType;
                String version = null;
                if (dotIndex < 0) { // 不带版本号, 如mariadb
                    dbType = supportItem.toLowerCase();
                } else if (dotIndex == 0) { // .10.2.2?
                    continue;
                } else { // 带版本号, 如mariadb.10.2.2
                    dbType = supportItem.substring(0, dotIndex).toLowerCase();
                    version = supportItem.substring(dotIndex + 1);
                }
                String sqlKey = sqlId + '(' + dbType + ')';
                TagData value = new TagData(version, data);
                if (this.typedCache.containsKey(sqlKey)) {
                    mergeTagDatas(value, this.typedCache.get(sqlKey));
                } else {
                    this.typedCache.put(sqlKey, ConvertTools.toList(value));
                }
            }

        }
    }

    private static void mergeTagDatas(TagData item, List<TagData> list) {
        String version = item.getMinVersion();
        if (version == null || "*".equals(version)) {
            list.add(item);
            return;
        }
        boolean inserted = false;
        for (int index = 0; index < list.size(); index++) {
            TagData target = list.get(index);
            String targetVersion = target.getMinVersion();
            if (targetVersion == null || "*".equals(targetVersion)) {
                list.add(index, item);
                inserted = true;
                break;
            } else if (VersionCodeTools.compare(targetVersion, version) < 0) {
                // 插入首个小于当前版本的位置的前面
                list.add(index, item);
                inserted = true;
                break;
            }
        }
        if (!inserted) {
            list.add(item);
        }
    }

    /**
     * 查找SQL模板标签元数据树
     * 
     * @param sqlId SQL编号
     * @param dbVersion 数据库版本
     * @return 标签元数据树
     */
    public IMetaData find(String sqlId, DbVersion dbVersion) {
        return find(sqlId, dbVersion, true);
    }

    // key=sqlId(dbType.version)
    private Map<String, IMetaData> tmplFondCache = new HashMap<>();
    // key=sqlId(dbType.version), value=details message
    private Map<String, String> tmplErrorCache = new HashMap<>();

    protected IMetaData find(String sqlId, DbVersion dbVersion, boolean throwOnNotFound) {

        String dbType = dbVersion.getDbType().name().toLowerCase();
        String currVersion = dbVersion.getVersionCode();

        String cacheKey = sqlId + '(' + dbType + '.' + currVersion + ')';
        if (tmplFondCache.containsKey(cacheKey)) {
            return tmplFondCache.get(cacheKey);
        }
        if (tmplErrorCache.containsKey(cacheKey)) {
            if (throwOnNotFound) {
                String details = tmplErrorCache.get(cacheKey);
                throw new ServiceException(DbErrorCode.DB_SQL_FRAGMENT_NOT_FOUND, details);
            } else {
                return null;
            }
        }

        IMetaData found = null;
        // 记录下尝试过哪些模板, 用于匹配失败时输出日志
        // key=dbType+dbVersion, value=location
        List<KeyString> tryedLocations = new ArrayList<>();
        { // 开始查找模板
            this.scanSqlFiles();
            String sqlKey = sqlId + '(' + dbType + ')';
            List<TagData> typedList = typedCache.get(sqlKey);
            if (typedList != null && !typedList.isEmpty()) {
                for (TagData item : typedList) {
                    // 此模板的最低版本要求
                    String minVersion = item.getMinVersion();
                    tryedLocations.add(new KeyString(dbType + '.' + minVersion, item.getMetaData().getRealPath()));
                    if (minVersion == null || "*".equals(minVersion)) {
                        found = item.getMetaData(); // 没有最低要求, 则当前数据库为匹配
                        break;
                    }
                    if (VersionCodeTools.compare(currVersion, minVersion) >= 0) {
                        found = item.getMetaData(); // 满足最低要求, 匹配成功
                        break;
                    }
                }
            }

            // 带版本的模板未匹配成功, 判断是否存在不带版本要求的模板
            if (found == null && untypedCache.containsKey(sqlId)) {
                found = untypedCache.get(sqlId);
            }
        }

        if (found != null) {
            tmplFondCache.put(cacheKey, found);
            return found;
        }

        // 未匹配成功
        StringBuilder details = new StringBuilder();
        if (tryedLocations.isEmpty()) {
            details.append("sqlId=").append(sqlId).append(", dbVersion=").append(dbVersion.toVersionString());
        } else {
            details.append("\nsqlId=").append(sqlId).append(", dbVersion=").append(dbVersion.toVersionString());
            details.append("\nmatches tryed locations:\n").append(locationsToString(tryedLocations));
        }
        tmplErrorCache.put(cacheKey, details.toString());
        if (throwOnNotFound) {
            throw new ServiceException(DbErrorCode.DB_SQL_FRAGMENT_NOT_FOUND, details.toString());
        } else {
            return null;
        }
    }

    private String locationsToString(List<KeyString> locations) {
        int maxVersionLength = 0;
        for (KeyString item : locations) {
            if (maxVersionLength < item.getKey().length()) {
                maxVersionLength = item.getKey().length();
            }
        }
        StringBuilder buffer = new StringBuilder();
        for (KeyString item : locations) {
            if (buffer.length() > 0) {
                buffer.append('\n');
            }
            buffer.append(StringTools.pad(item.getKey(), ' ', false, maxVersionLength));
            buffer.append(" --> ");
            buffer.append(item.getValue());
        }
        return buffer.toString();
    }

    /**
     * 判断SQL模板是否存在
     * 
     * @param sqlId SQL编号
     * @param dbVersion 数据库版本
     * @return 是否存在
     */
    public boolean exist(String sqlId, DbVersion dbVersion) {
        this.scanSqlFiles();
        if (untypedCache.containsKey(sqlId)) {
            return true;
        }
        IMetaData metadata = find(sqlId, dbVersion, false);
        return metadata != null;
    }

    /** 根据SqlId从缓存中获取SQL模板, 渲染为SqlBuffer对象 **/
    public SqlBuffer render(String sqlId, Map<String, Object> data, SqlDialect dialect) {
        IMetaData tags = find(sqlId, dialect.getDbVersion());
        return publish(tags, data, dialect);
    }

    /** 解析SQL模板内容, 渲染为SqlBuffer对象 **/
    public SqlBuffer parse(String sqlString, Map<String, Object> data, SqlDialect dialect) {
        IMetaData tags = SqlStringParser.parseSqlString(sqlString);
        return publish(tags, data, dialect);
    }

    /** 将SQL模板渲染为SqlBuffer对象 **/
    public SqlBuffer publish(IMetaData tags, Map<String, Object> data, SqlDialect dialect) {
        SqlBufferPublisher publisher = new SqlBufferPublisher(tags);
        try {
            SqlBuffer buffer = publisher.publish(data, dialect);
            return buffer.trim();
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

        List<ParsedFragment> tagDatas = parser.parseCachedSqlFragments();
        if (log.isInfoEnabled()) {
            String msg = "Success to parse sql templates, elapsed time {}, total of {} files and {} fragments.";
            log.info(msg, ConvertTools.toDuration(startTime, true), urls.size(), tagDatas.size());
        }
        for (ParsedFragment item : tagDatas) {
            this.register(item.getSqlId(), item.getSupports(), item.getMetaData());
            if (item.getAlias() != null) {
                this.register(item.getAlias(), item.getSupports(), item.getMetaData());
            }
        }
    }

    protected static class TagData {

        /** 最低版本要求 **/
        private final String minVersion;
        private final IMetaData metadata;

        public TagData(String minVersion, IMetaData data) {
            this.minVersion = minVersion;
            this.metadata = data;
        }

        /** 最低版本要求 **/
        public String getMinVersion() {
            return minVersion;
        }

        public IMetaData getMetaData() {
            return metadata;
        }
    }

    protected static class ParsedFragment {

        private final String sqlId;
        private final String alias;
        private final String supports;
        private final IMetaData metadata;

        public ParsedFragment(String sqlId, String alias, String supports, IMetaData data) {
            this.sqlId = sqlId;
            this.alias = alias;
            this.supports = supports;
            this.metadata = data;
        }

        public String getSqlId() {
            return sqlId;
        }

        public String getAlias() {
            return alias;
        }

        public IMetaData getMetaData() {
            return metadata;
        }

        public String getSupports() {
            return supports;
        }

    }
}
