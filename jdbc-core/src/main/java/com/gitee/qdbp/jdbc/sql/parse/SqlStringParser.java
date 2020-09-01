package com.gitee.qdbp.jdbc.sql.parse;

import java.io.IOException;
import java.util.Date;
import com.gitee.qdbp.able.exception.ResourceNotFoundException;
import com.gitee.qdbp.able.exception.ServiceException;
import com.gitee.qdbp.jdbc.exception.DbErrorCode;
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
        TemporaryReaderCreator readerCreator = new TemporaryReaderCreator(sqlString);
        TagParser parser = new TagParser(readerCreator);
        try {
            return parser.parse(temporarySqlId);
        } catch (Exception e) {
            throw new ServiceException(DbErrorCode.DB_SQL_FRAGMENT_PARSE_ERROR, e);
        }
    }

    private static final String temporarySqlId = "{TemporarySqlString}";

    private static class TemporaryReaderCreator implements IReaderCreator {

        private IReader reader;

        public TemporaryReaderCreator(String sqlString) {
            this.reader = new SimpleReader(temporarySqlId, sqlString);
        }

        @Override
        public IReader create(String sqlId) throws IOException, ResourceNotFoundException {
            if (sqlId.equals(temporarySqlId)) {
                return reader;
            } else {
                throw new ResourceNotFoundException(sqlId + " not found");
            }
        }

        @Override
        public String getRelativePath(String sqlId, String newId) {
            return newId;
        }

        @Override
        public Date getUpdateTime(String path) throws IOException, ResourceNotFoundException {
            return null;
        }
    }
}
