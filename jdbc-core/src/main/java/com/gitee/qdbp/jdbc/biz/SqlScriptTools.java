package com.gitee.qdbp.jdbc.biz;

import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.CannotReadScriptException;
import org.springframework.jdbc.datasource.init.ScriptException;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.jdbc.datasource.init.UncategorizedScriptException;
import org.springframework.util.StringUtils;
import com.gitee.qdbp.tools.files.FileTools;
import com.gitee.qdbp.tools.files.PathTools;

/**
 * SqlScript执行工具
 *
 * @author zhaohuihua
 * @version 20200829
 */
class SqlScriptTools {

    private static final Logger log = LoggerFactory.getLogger(SqlScriptTools.class);

    private static final Charset UTF8 = Charset.forName("UTF-8");

    // 自动识别文件的编码格式
    private static Charset resolveCharset(URL url) {
        try (InputStream input = url.openStream()) {
            String charsetName = FileTools.getEncoding(input);
            log.trace("Success to get file encoding [{}], {}, {}.", charsetName, url.toString());
            return Charset.forName(charsetName);
        } catch (Exception e) {
            log.warn("Failed to get file encoding, {}, {}.", url.toString(), e.toString());
            return UTF8;
        }
    }

    // copy from ScriptUtils.executeSqlScript(Connection, EncodedResource, boolean, boolean, String, ...)
    // 修复问题: 
    // 1. 未清除警告信息, 警告信息一直累加
    // 2. DROP语句如果设置了ignoreFailedDrops就不需要输出异常堆栈信息, 会扰乱日志信息
    // 3. 优化日志信息, 输出执行了多少个SQL片断
    public static void executeSqlScript(Connection connection, URL url, boolean continueOnError,
            boolean ignoreFailedDrops, String commentPrefix, String separator, String blockCommentStartDelimiter,
            String blockCommentEndDelimiter) throws ScriptException {

        Charset charset = resolveCharset(url);
        EncodedResource resource = new EncodedResource(new UrlResource(url), charset);

        String path = PathTools.getFileName(url.toString());
        try {
            log.info("Executing SQL script from {}", resource);
            long startTime = System.currentTimeMillis();

            String script;
            LineNumberReader reader = new LineNumberReader(resource.getReader());
            try {
                script = ScriptUtils.readScript(reader, commentPrefix, separator);
            } catch (IOException e) {
                throw new CannotReadScriptException(resource, e);
            } finally {
                reader.close();
            }

            if (!ScriptUtils.EOF_STATEMENT_SEPARATOR.equals(separator)
                    && !ScriptUtils.containsSqlScriptDelimiters(script, separator)) {
                separator = ScriptUtils.FALLBACK_STATEMENT_SEPARATOR;
            }

            List<String> fragments = new LinkedList<String>();
            ScriptUtils.splitSqlScript(resource, script, separator, commentPrefix, blockCommentStartDelimiter,
                blockCommentEndDelimiter, fragments);

            int sqlIndex = 0;
            Statement stmt = connection.createStatement();
            try {
                for (String fragment : fragments) {
                    sqlIndex++;
                    executeSqlScript(stmt, fragment, sqlIndex, path, continueOnError, ignoreFailedDrops);
                }
            } finally {
                try {
                    stmt.close();
                } catch (Throwable e) {
                    log.debug("Could not close JDBC Statement", e);
                }
            }

            long elapsedTime = System.currentTimeMillis() - startTime;
            if (log.isInfoEnabled()) {
                log.info("Executed {} SQL script fragments in {} ms from {}.", sqlIndex, elapsedTime, resource);
            }
        } catch (Exception e) {
            if (e instanceof ScriptException) {
                throw (ScriptException) e;
            }
            throw new UncategorizedScriptException("Failed to execute SQL script from " + resource, e);
        }
    }

    private static void executeSqlScript(Statement stmt, String sql, int index, String path, boolean continueOnError,
            boolean ignoreFailedDrops) {
        try {
            stmt.execute(sql);
            int rowsAffected = stmt.getUpdateCount();
            if (log.isDebugEnabled()) {
                log.debug(rowsAffected + " returned as update count for SQL: " + sql);
                SQLWarning warn = stmt.getWarnings();
                while (warn != null) {
                    String msg = "SQLWarning ignored, SqlState:{}, ErrorCode:{}, Message:[{}]";
                    log.debug(msg, warn.getSQLState(), warn.getErrorCode(), warn.getMessage());
                    warn = warn.getNextWarning();
                }
                stmt.clearWarnings();
            }
        } catch (SQLException e) {
            boolean dropStatement = StringUtils.startsWithIgnoreCase(sql.trim(), "drop");
            if (dropStatement && ignoreFailedDrops) {
                if (log.isDebugEnabled()) {
                    String msg = "Failed to execute SQL fragment #{} of {}: {}, {}";
                    log.debug(msg, index, path, sql, e.toString());
                }
            } else if (continueOnError) {
                if (log.isDebugEnabled()) {
                    String msg = "Failed to execute SQL fragment #{} of {}: {}";
                    log.debug(msg, index, path, sql, e);
                }
            } else {
                throw new ScriptFragmentFailedException(sql, index, path, e);
            }
        }
    }

    private static class ScriptFragmentFailedException extends ScriptException {

        /** serialVersionUID **/
        private static final long serialVersionUID = 1L;

        public ScriptFragmentFailedException(String sql, int index, String path, Throwable e) {
            super(buildErrorMessage(sql, index, path), e);
        }

        public static String buildErrorMessage(String sql, int index, String path) {
            String fmt = "Failed to execute SQL fragment #%s of %s: %s";
            return String.format(fmt, index, path, sql);
        }
    }
}
