package com.gitee.qdbp.jdbc.sql.parse;

import java.io.IOException;
import java.util.Date;
import com.gitee.qdbp.able.exception.ResourceNotFoundException;
import com.gitee.qdbp.able.exception.ServiceException;
import com.gitee.qdbp.jdbc.exception.DbErrorCode;
import com.gitee.qdbp.jdbc.sql.parse.SqlFragmentParser.CacheBox;
import com.gitee.qdbp.staticize.common.IMetaData;
import com.gitee.qdbp.staticize.common.IReader;
import com.gitee.qdbp.staticize.io.IReaderCreator;
import com.gitee.qdbp.staticize.io.SimpleReader;
import com.gitee.qdbp.staticize.parse.TagParser;

/**
 * 解析SQL字符串
 *
 * @author zhaohuihua
 * @version 20200830
 */
public class SqlStringParser {

    /**
     * 解析SQL字符串
     * 
     * @param sqlString SQL字符串
     * @return 解析后的标签元数据
     */
    public static IMetaData parseSqlString(String sqlString) {
        return parseSqlString(sqlString, null);
    }

    /**
     * 解析SQL字符串
     * 
     * @param sqlString SQL字符串
     * @return 解析后的标签元数据
     */
    public static IMetaData parseSqlString(String sqlString, CacheBox cacheBox) {
        TemporaryReaderCreator readerCreator = new TemporaryReaderCreator(sqlString, cacheBox);
        TagParser parser = new TagParser(readerCreator);
        try {
            return parser.parse(temporarySqlId);
        } catch (Exception e) {
            throw new ServiceException(DbErrorCode.DB_SQL_FRAGMENT_PARSE_ERROR, e);
        }
    }

    private static final String temporarySqlId = "{SqlString(Temporary)}";

    private static class TemporaryReaderCreator implements IReaderCreator {

        private IReader reader;
        private CacheBox cacheBox;

        public TemporaryReaderCreator(String sqlString, CacheBox cacheBox) {
            this.reader = new SimpleReader(temporarySqlId, sqlString);
            this.cacheBox = cacheBox;
        }

        @Override
        public IReader create(String sqlId) throws IOException, ResourceNotFoundException {
            if (sqlId.equals(temporarySqlId)) {
                return reader;
            } else {
                return cacheBox == null ? null : cacheBox.create(sqlId);
            }
        }

        @Override
        public String getRelativePath(String sqlId, String newId) {
            return newId;
        }

        @Override
        public Date getUpdateTime(String path) throws IOException, ResourceNotFoundException {
            if (path.equals(temporarySqlId)) {
                return null;
            } else {
                return cacheBox == null ? null : cacheBox.getUpdateTime(path);
            }
        }
    }
}
