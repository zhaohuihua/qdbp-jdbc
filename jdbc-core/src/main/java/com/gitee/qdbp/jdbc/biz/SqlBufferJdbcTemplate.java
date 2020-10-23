package com.gitee.qdbp.jdbc.biz;

import java.net.URL;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import org.springframework.jdbc.core.namedparam.ParsedSql;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.jdbc.support.JdbcAccessor;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import com.gitee.qdbp.able.exception.ServiceException;
import com.gitee.qdbp.able.result.IResultMessage;
import com.gitee.qdbp.able.result.ResultCode;
import com.gitee.qdbp.jdbc.api.SqlBufferJdbcOperations;
import com.gitee.qdbp.jdbc.api.SqlDao;
import com.gitee.qdbp.jdbc.model.DbVersion;
import com.gitee.qdbp.jdbc.plugins.MapToBeanConverter;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.result.FirstColumnMapper;
import com.gitee.qdbp.jdbc.result.RowToBeanMapper;
import com.gitee.qdbp.jdbc.result.TableRowToBeanMapper;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.parse.SqlFragmentContainer;
import com.gitee.qdbp.jdbc.utils.CountSqlParser;
import com.gitee.qdbp.jdbc.utils.DbTools;
import com.gitee.qdbp.tools.files.PathTools;
import com.gitee.qdbp.tools.utils.ConvertTools;
import com.gitee.qdbp.tools.utils.ReflectTools;
import com.gitee.qdbp.tools.utils.StringTools;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * SqlBuffer数据库操作类
 *
 * @author 赵卉华
 * @version 190601
 */
public class SqlBufferJdbcTemplate implements SqlBufferJdbcOperations {

    private static Logger log = LoggerFactory.getLogger(SqlBufferJdbcTemplate.class);

    private DbVersion dbVersion;
    private SqlDialect sqlDialect;
    private SqlDao sqlDao;
    private NamedParameterJdbcOperations namedParameterJdbcOperations;
    protected CountSqlParser countSqlParser = new CountSqlParser();

    public SqlBufferJdbcTemplate() {
    }

    public SqlBufferJdbcTemplate(NamedParameterJdbcOperations jdbcOperations) {
        this.setNamedParameterJdbcOperations(jdbcOperations);
    }

    public NamedParameterJdbcOperations getNamedParameterJdbcOperations() {
        return namedParameterJdbcOperations;
    }

    public void setNamedParameterJdbcOperations(NamedParameterJdbcOperations namedParameterJdbcOperations) {
        this.namedParameterJdbcOperations = namedParameterJdbcOperations;
    }

    private boolean inited = false;

    protected void init() {
        if (this.inited) {
            return;
        }
        this.doInit();
    }

    private synchronized void doInit() {
        if (this.inited) {
            return;
        }
        this.inited = true;

        JdbcOperations jdbcOperations = namedParameterJdbcOperations.getJdbcOperations();
        if (!(jdbcOperations instanceof JdbcAccessor)) {
            throw new IllegalStateException("Unsupported JdbcOperations: " + jdbcOperations.getClass().getName());
        }
        JdbcAccessor accessor = (JdbcAccessor) jdbcOperations;
        DataSource datasource = accessor.getDataSource();
        if (datasource == null) {
            throw new IllegalStateException("Datasource is null.");
        }
        if (dbVersion == null) {
            dbVersion = DbTools.findDbVersion(datasource);
            sqlDialect = DbTools.buildSqlDialect(dbVersion);
            log.debug("Database version: {}", dbVersion);
        }
        if (this.sqlDao == null) {
            SqlFragmentContainer container = SqlFragmentContainer.defaults();
            this.sqlDao = new SqlDaoImpl(container, this);
        }
    }

    @Override
    public DbVersion getDbVersion() {
        this.init();
        return dbVersion;
    }

    @Override
    public SqlDialect getSqlDialect() {
        this.init();
        return sqlDialect;
    }

    @Override
    public SqlDao getSqlDao() {
        this.init();
        return sqlDao;
    }

    private <T> RowToBeanMapper<T> newRowToBeanMapper(Class<T> clazz) {
        MapToBeanConverter converter = DbTools.getMapToBeanConverter();
        return new TableRowToBeanMapper<>(clazz, converter);
    }

    @Override
    public <T> T execute(SqlBuffer sb, PreparedStatementCallback<T> action) throws ServiceException {
        VerifyTools.requireNotBlank(sb, "sqlBuffer");
        long startTime = System.currentTimeMillis();
        String logsql = null;
        if (log.isDebugEnabled()) {
            log.debug("Executing sql statement:\n{}", logsql = getFormattedSqlString(sb, 1));
        }
        String sql = sb.getPreparedSqlString(sqlDialect);
        Map<String, Object> params = sb.getPreparedVariables(sqlDialect);
        try {
            T result = namedParameterJdbcOperations.execute(sql, params, action);
            if (log.isDebugEnabled()) {
                long time = System.currentTimeMillis() - startTime;
                log.debug("Sql query returns {} rows, elapsed time {}ms.", result == null ? 0 : 1, time);
            }
            return result;
        } catch (EmptyResultDataAccessException e) {
            if (log.isDebugEnabled()) {
                log.debug("Sql query returns 0 row.");
            }
            return null;
        } catch (DataAccessException e) {
            String details = "Sql:\n" + (logsql != null ? logsql : getFormattedSqlString(sb, 1));
            details = StringTools.concat('\n', details, e.getCause() == null ? null : e.getCause().getMessage());
            throw new ServiceException(ResultCode.DB_SELECT_ERROR, details, e);
        }
    }

    @Override
    public <T> T query(SqlBuffer sb, ResultSetExtractor<T> rse) throws ServiceException {
        VerifyTools.requireNotBlank(sb, "sqlBuffer");
        long startTime = System.currentTimeMillis();
        String logsql = null;
        if (log.isDebugEnabled()) {
            log.debug("Executing sql query:\n{}", logsql = getFormattedSqlString(sb, 1));
        }
        String sql = sb.getPreparedSqlString(sqlDialect);
        Map<String, Object> params = sb.getPreparedVariables(sqlDialect);
        try {
            T result = namedParameterJdbcOperations.query(sql, params, rse);
            if (log.isDebugEnabled()) {
                long time = System.currentTimeMillis() - startTime;
                log.debug("Sql query returns {} rows, elapsed time {}ms.", result == null ? 0 : 1, time);
            }
            return result;
        } catch (EmptyResultDataAccessException e) {
            if (log.isDebugEnabled()) {
                long time = System.currentTimeMillis() - startTime;
                log.debug("Sql query returns 0 row, elapsed time {}ms.", time);
            }
            return null;
        } catch (DataAccessException e) {
            String details = "Sql:\n" + (logsql != null ? logsql : getFormattedSqlString(sb, 1));
            details = StringTools.concat('\n', details, e.getCause() == null ? null : e.getCause().getMessage());
            throw new ServiceException(ResultCode.DB_SELECT_ERROR, details, e);
        }
    }

    @Override
    public void query(SqlBuffer sb, RowCallbackHandler rch) throws ServiceException {
        VerifyTools.requireNotBlank(sb, "sqlBuffer");
        long startTime = System.currentTimeMillis();
        String logsql = null;
        if (log.isDebugEnabled()) {
            log.debug("Executing sql query:\n{}", logsql = getFormattedSqlString(sb, 1));
        }
        String sql = sb.getPreparedSqlString(sqlDialect);
        Map<String, Object> params = sb.getPreparedVariables(sqlDialect);
        try {
            namedParameterJdbcOperations.query(sql, params, rch);
            if (log.isDebugEnabled()) {
                long time = System.currentTimeMillis() - startTime;
                log.debug("Sql query, elapsed time {}ms.", time);
            }
        } catch (DataAccessException e) {
            String details = "Sql:\n" + (logsql != null ? logsql : getFormattedSqlString(sb, 1));
            details = StringTools.concat('\n', details, e.getCause() == null ? null : e.getCause().getMessage());
            throw new ServiceException(ResultCode.DB_SELECT_ERROR, details, e);
        }
    }

    @Override
    public <T> List<T> query(SqlBuffer sb, RowMapper<T> rowMapper) throws ServiceException {
        VerifyTools.requireNotBlank(sb, "sqlBuffer");
        long startTime = System.currentTimeMillis();
        String logsql = null;
        if (log.isDebugEnabled()) {
            log.debug("Executing sql query:\n{}", logsql = getFormattedSqlString(sb, 1));
        }
        String sql = sb.getPreparedSqlString(sqlDialect);
        Map<String, Object> params = sb.getPreparedVariables(sqlDialect);
        try {
            List<T> list = namedParameterJdbcOperations.query(sql, params, rowMapper);
            if (log.isDebugEnabled()) {
                long time = System.currentTimeMillis() - startTime;
                log.debug("Sql query returns {} rows, elapsed time {}ms.", list == null ? 0 : list.size(), time);
            }
            return list;
        } catch (DataAccessException e) {
            String details = "Sql:\n" + (logsql != null ? logsql : getFormattedSqlString(sb, 1));
            details = StringTools.concat('\n', details, e.getCause() == null ? null : e.getCause().getMessage());
            throw new ServiceException(ResultCode.DB_SELECT_ERROR, details, e);
        }
    }

    @Override
    public <T> T queryForObject(SqlBuffer sb, RowMapper<T> rowMapper) throws ServiceException {
        VerifyTools.requireNotBlank(sb, "sqlBuffer");
        long startTime = System.currentTimeMillis();
        String logsql = null;
        if (log.isDebugEnabled()) {
            log.debug("Executing sql query:\n{}", logsql = getFormattedSqlString(sb, 1));
        }
        String sql = sb.getPreparedSqlString(sqlDialect);
        Map<String, Object> params = sb.getPreparedVariables(sqlDialect);
        try {
            T result = namedParameterJdbcOperations.queryForObject(sql, params, rowMapper);
            if (log.isDebugEnabled()) {
                long time = System.currentTimeMillis() - startTime;
                log.debug("Sql query returns {} rows, elapsed time {}ms.", result == null ? 0 : 1, time);
            }
            return result;
        } catch (EmptyResultDataAccessException e) {
            if (log.isDebugEnabled()) {
                long time = System.currentTimeMillis() - startTime;
                log.debug("Sql query returns 0 row, elapsed time {}ms.", time);
            }
            return null;
        } catch (DataAccessException e) {
            String details = "Sql:\n" + (logsql != null ? logsql : getFormattedSqlString(sb, 1));
            details = StringTools.concat('\n', details, e.getCause() == null ? null : e.getCause().getMessage());
            throw new ServiceException(ResultCode.DB_SELECT_ERROR, details, e);
        }
    }

    @Override
    public <T> T queryForObject(SqlBuffer sb, Class<T> resultType) throws ServiceException {
        VerifyTools.requireNotBlank(sb, "sqlBuffer");
        if (!ReflectTools.isPrimitive(resultType, false)) {
            return queryForObject(sb, newRowToBeanMapper(resultType));
        }
        long startTime = System.currentTimeMillis();
        String logsql = null;
        if (log.isDebugEnabled()) {
            log.debug("Executing sql query:\n{}", logsql = getFormattedSqlString(sb, 1));
        }
        try {
            String sql = sb.getPreparedSqlString(sqlDialect);
            Map<String, Object> params = sb.getPreparedVariables(sqlDialect);
            T result = namedParameterJdbcOperations.queryForObject(sql, params, resultType);
            if (log.isDebugEnabled()) {
                long time = System.currentTimeMillis() - startTime;
                queryForObjectLogResult(time, result);
            }
            return result;
        } catch (EmptyResultDataAccessException e) {
            if (log.isDebugEnabled()) {
                long time = System.currentTimeMillis() - startTime;
                log.debug("Sql query returns 0 row, elapsed time {}ms.", time);
            }
            return null;
        } catch (DataAccessException e) {
            String details = "Sql:\n" + (logsql != null ? logsql : getFormattedSqlString(sb, 1));
            details = StringTools.concat('\n', details, e.getCause() == null ? null : e.getCause().getMessage());
            throw new ServiceException(ResultCode.DB_SELECT_ERROR, details, e);
        }
    }

    private void queryForObjectLogResult(long time, Object result) {
        if (result == null) {
            log.debug("Sql query returns 0 row, elapsed time {}ms.", time);
            return;
        }
        Class<?> resultType = result.getClass();
        if (resultType == String.class) {
            String string = (String) result;
            String desc = string.length() > 100 ? "String(" + string.length() + ")" : string;
            log.debug("Sql query returns 1 row, the value is {}, elapsed time {}ms.", desc, time);
        } else if (ReflectTools.isPrimitive(resultType) || resultType.isEnum()) {
            String string = result.toString();
            log.debug("Sql query returns 1 row, the value is {}, elapsed time {}ms.", string, time);
        } else {
            log.debug("Sql query returns 1 row, elapsed time {}ms.", time);
        }
    }

    @Override
    public Map<String, Object> queryForMap(SqlBuffer sb) throws ServiceException {
        VerifyTools.requireNotBlank(sb, "sqlBuffer");
        long startTime = System.currentTimeMillis();
        String logsql = null;
        if (log.isDebugEnabled()) {
            log.debug("Executing sql query:\n{}", logsql = getFormattedSqlString(sb, 1));
        }
        String sql = sb.getPreparedSqlString(sqlDialect);
        Map<String, Object> params = sb.getPreparedVariables(sqlDialect);
        try {
            Map<String, Object> result = namedParameterJdbcOperations.queryForMap(sql, params);
            if (log.isDebugEnabled()) {
                long time = System.currentTimeMillis() - startTime;
                log.debug("Sql query returns {} rows, elapsed time {}ms.", result == null ? 0 : 1, time);
            }
            return result;
        } catch (EmptyResultDataAccessException e) {
            if (log.isDebugEnabled()) {
                long time = System.currentTimeMillis() - startTime;
                log.debug("Sql query returns 0 row, elapsed time {}ms.", time);
            }
            return null;
        } catch (DataAccessException e) {
            String details = "Sql:\n" + (logsql != null ? logsql : getFormattedSqlString(sb, 1));
            details = StringTools.concat('\n', details, e.getCause() == null ? null : e.getCause().getMessage());
            throw new ServiceException(ResultCode.DB_SELECT_ERROR, details, e);
        }
    }

    @Override
    public <T> List<T> queryForList(SqlBuffer sb, Class<T> elementType) throws ServiceException {
        VerifyTools.requireNotBlank(sb, "sqlBuffer");
        long startTime = System.currentTimeMillis();
        String logsql = null;
        if (log.isDebugEnabled()) {
            log.debug("Executing sql query:\n{}", logsql = getFormattedSqlString(sb, 1));
        }
        String sql = sb.getPreparedSqlString(sqlDialect);
        Map<String, Object> params = sb.getPreparedVariables(sqlDialect);
        try {
            List<T> list;
            if (ReflectTools.isPrimitive(elementType, false)) {
                list = namedParameterJdbcOperations.queryForList(sql, params, elementType);
            } else {
                list = namedParameterJdbcOperations.query(sql, params, newRowToBeanMapper(elementType));
            }
            if (log.isDebugEnabled()) {
                long time = System.currentTimeMillis() - startTime;
                log.debug("Sql query returns {} rows, elapsed time {}ms.", list == null ? 0 : list.size(), time);
            }
            return list;
        } catch (DataAccessException e) {
            String details = "Sql:\n" + (logsql != null ? logsql : getFormattedSqlString(sb, 1));
            details = StringTools.concat('\n', details, e.getCause() == null ? null : e.getCause().getMessage());
            throw new ServiceException(ResultCode.DB_SELECT_ERROR, details, e);
        }
    }

    @Override
    public List<Map<String, Object>> queryForList(SqlBuffer sb) throws ServiceException {
        VerifyTools.requireNotBlank(sb, "sqlBuffer");
        long startTime = System.currentTimeMillis();
        String logsql = null;
        if (log.isDebugEnabled()) {
            log.debug("Executing sql query:\n{}", logsql = getFormattedSqlString(sb, 1));
        }
        String sql = sb.getPreparedSqlString(sqlDialect);
        Map<String, Object> params = sb.getPreparedVariables(sqlDialect);
        try {
            List<Map<String, Object>> list = namedParameterJdbcOperations.queryForList(sql, params);
            if (log.isDebugEnabled()) {
                long time = System.currentTimeMillis() - startTime;
                log.debug("Sql query returns {} rows, elapsed time {}ms.", list == null ? 0 : list.size(), time);
            }
            return list;
        } catch (DataAccessException e) {
            String details = "Sql:\n" + (logsql != null ? logsql : getFormattedSqlString(sb, 1));
            details = StringTools.concat('\n', details, e.getCause() == null ? null : e.getCause().getMessage());
            throw new ServiceException(ResultCode.DB_SELECT_ERROR, details, e);
        }
    }

    @Override
    public SqlRowSet queryForRowSet(SqlBuffer sb) throws ServiceException {
        VerifyTools.requireNotBlank(sb, "sqlBuffer");
        long startTime = System.currentTimeMillis();
        String logsql = null;
        if (log.isDebugEnabled()) {
            log.debug("Executing sql query:\n{}", logsql = getFormattedSqlString(sb, 1));
        }
        String sql = sb.getPreparedSqlString(sqlDialect);
        Map<String, Object> params = sb.getPreparedVariables(sqlDialect);
        try {
            SqlRowSet result = namedParameterJdbcOperations.queryForRowSet(sql, params);
            if (log.isDebugEnabled()) {
                long time = System.currentTimeMillis() - startTime;
                log.debug("Sql query, elapsed time {}ms.", time);
            }
            return result;
        } catch (DataAccessException e) {
            String details = "Sql:\n" + (logsql != null ? logsql : getFormattedSqlString(sb, 1));
            details = StringTools.concat('\n', details, e.getCause() == null ? null : e.getCause().getMessage());
            throw new ServiceException(ResultCode.DB_SELECT_ERROR, details, e);
        }
    }

    @Override
    public int insert(SqlBuffer sb) throws ServiceException {
        VerifyTools.requireNotBlank(sb, "sqlBuffer");
        return doExecute(sb, "insert", ResultCode.DB_INSERT_ERROR);
    }

    @Override
    public int insert(SqlBuffer sb, KeyHolder generatedKeyHolder) throws ServiceException {
        VerifyTools.requireNotBlank(sb, "sqlBuffer");
        return doExecute(sb, generatedKeyHolder, "insert", ResultCode.DB_INSERT_ERROR);
    }

    @Override
    public int update(SqlBuffer sb) throws ServiceException {
        VerifyTools.requireNotBlank(sb, "sqlBuffer");
        return doExecute(sb, "update", ResultCode.DB_UPDATE_ERROR);
    }

    @Override
    public int delete(SqlBuffer sb) throws ServiceException {
        VerifyTools.requireNotBlank(sb, "sqlBuffer");
        return doExecute(sb, "delete", ResultCode.DB_DELETE_ERROR);
    }

    protected int doExecute(SqlBuffer sb, String desc, IResultMessage errorCode) throws ServiceException {
        long startTime = System.currentTimeMillis();
        String logsql = null;
        if (log.isDebugEnabled()) {
            log.debug("Executing sql {}:\n{}", desc, logsql = getFormattedSqlString(sb, 1));
        }
        String sql = sb.getPreparedSqlString(sqlDialect);
        Map<String, Object> params = sb.getPreparedVariables(sqlDialect);
        try {
            int rows = namedParameterJdbcOperations.update(sql, params);
            if (log.isDebugEnabled()) {
                long time = System.currentTimeMillis() - startTime;
                log.debug("Sql {} affected " + rows + " rows, elapsed time {}ms", desc, time);
            }
            return rows;
        } catch (DataAccessException e) {
            String details = "Sql:\n" + (logsql != null ? logsql : getFormattedSqlString(sb, 1));
            details = StringTools.concat('\n', details, e.getCause() == null ? null : e.getCause().getMessage());
            throw new ServiceException(errorCode, details, e);
        }
    }

    protected int doExecute(SqlBuffer sb, KeyHolder generatedKeyHolder, String desc, IResultMessage errorCode)
            throws ServiceException {
        long startTime = System.currentTimeMillis();
        String logsql = null;
        if (log.isDebugEnabled()) {
            log.debug("Executing sql {}:\n{}", desc, logsql = getFormattedSqlString(sb, 1));
        }
        String sql = sb.getPreparedSqlString(sqlDialect);
        Map<String, Object> params = sb.getPreparedVariables(sqlDialect);
        SqlParameterSource msps = new MapSqlParameterSource(params);
        try {
            int rows = namedParameterJdbcOperations.update(sql, msps, generatedKeyHolder);
            if (log.isDebugEnabled()) {
                long time = System.currentTimeMillis() - startTime;
                log.debug("Sql {} affected {} rows, elapsed time {}ms", desc, rows, time);
            }
            return rows;
        } catch (DataAccessException e) {
            String details = "Sql:\n" + (logsql != null ? logsql : getFormattedSqlString(sb, 1));
            details = StringTools.concat('\n', details, e.getCause() == null ? null : e.getCause().getMessage());
            throw new ServiceException(errorCode, details, e);
        }
    }

    @Override
    public int batchInsert(SqlBuffer sb) throws ServiceException {
        VerifyTools.requireNotBlank(sb, "sqlBuffer");
        return doBatchExecute(sb, "insert", ResultCode.DB_INSERT_ERROR);
    }

    @Override
    public int batchUpdate(SqlBuffer sb) throws ServiceException {
        VerifyTools.requireNotBlank(sb, "sqlBuffer");
        return doBatchExecute(sb, "update", ResultCode.DB_UPDATE_ERROR);
    }

    protected int doBatchExecute(SqlBuffer sb, String operate, IResultMessage errorCode) throws ServiceException {
        long startTime = System.currentTimeMillis();
        String logsql = null;
        if (log.isDebugEnabled()) {
            log.debug("Executing sql batch {}:\n{}", operate, logsql = getFormattedSqlString(sb, 1));
        }
        String sql = sb.getPreparedSqlString(sqlDialect);
        Map<String, Object> params = sb.getPreparedVariables(sqlDialect);
        try {
            int rows = namedParameterJdbcOperations.update(sql, params);
            if (log.isDebugEnabled()) {
                long time = System.currentTimeMillis() - startTime;
                log.debug("Sql batch {} affected {} rows, elapsed time {}ms.", operate, rows, time);
            }
            return rows;
        } catch (DataAccessException e) {
            String details = "Sql:\n" + (logsql != null ? logsql : getFormattedSqlString(sb, 1));
            details = StringTools.concat('\n', details, e.getCause() == null ? null : e.getCause().getMessage());
            throw new ServiceException(errorCode, details, e);
        }
    }

    protected String getFormattedSqlString(SqlBuffer sb, int indent) {
        // 未开启到TRACE级别的日志就执行省略模式
        boolean omitMode = !log.isTraceEnabled();
        String sql = sb.getLoggingSqlString(sqlDialect, omitMode);
        return DbTools.formatSql(sql, 1);
    }

    // key=querySql, value=countSql
    private static Map<String, SqlAndParams> COUNT_SQL_MAPS = new ConcurrentHashMap<>();

    @Override
    public int countByQuerySql(SqlBuffer querySql) {
        long startTime = System.currentTimeMillis();
        String namedQuerySql = querySql.getPreparedSqlString(sqlDialect);
        String countSql;
        Object[] paramArray;
        if (COUNT_SQL_MAPS.containsKey(namedQuerySql)) {
            SqlAndParams item = COUNT_SQL_MAPS.get(namedQuerySql);
            countSql = item.sql;
            paramArray = item.params;
        } else {
            // 解析查询语句生成统计语句
            Map<String, Object> paramMaps = querySql.getPreparedVariables(sqlDialect);
            SqlParameterSource paramSource = new MapSqlParameterSource(paramMaps);
            ParsedSql parsedQuerySql = NamedParameterUtils.parseSqlStatement(namedQuerySql);
            String actualQuerySql = NamedParameterUtils.substituteNamedParameters(parsedQuerySql, paramSource);
            paramArray = NamedParameterUtils.buildValueArray(parsedQuerySql, paramSource, null);

            countSql = countSqlParser.getSmartCountSql(actualQuerySql);

            COUNT_SQL_MAPS.put(namedQuerySql, new SqlAndParams(countSql, paramArray));
        }

        // 输出日志
        String logsql = null;
        if (log.isDebugEnabled()) {
            log.debug("Executing sql count:\n{}", logsql = getLoggingSqlForHashParamsSql(countSql, paramArray));
        }

        long countTime = System.currentTimeMillis();
        RowMapper<Integer> rowMapper = new FirstColumnMapper<>(Integer.class);
        try {
            // 执行统计语句
            JdbcOperations jdbc = namedParameterJdbcOperations.getJdbcOperations();
            int total = jdbc.queryForObject(countSql, paramArray, rowMapper);
            if (log.isDebugEnabled()) {
                long parseMills = countTime - startTime;
                long countMills = System.currentTimeMillis() - countTime;
                log.debug("Sql count returns {}, parse time {}ms, execute time {}ms.", total, parseMills, countMills);
            }
            return total;
        } catch (DataAccessException e) {
            String details = "Sql:\n" + (logsql != null ? logsql : getLoggingSqlForHashParamsSql(countSql, paramArray));
            details = StringTools.concat('\n', details, e.getCause() == null ? null : e.getCause().getMessage());
            throw new ServiceException(ResultCode.DB_SELECT_ERROR, details, e);
        }
    }

    private static class SqlAndParams {

        private String sql;
        private Object[] params;

        public SqlAndParams(String sql, Object[] params) {
            this.sql = sql;
            this.params = params;
        }
    }

    private String getLoggingSqlForHashParamsSql(String sql, Object[] params) {
        if (params == null || params.length == 0) {
            // 没有占位符
            return sql;
        } else if (StringTools.countCharacter(sql, '?') != params.length) {
            // 占位符的数量与参数个数不一致, 说明字符串中有问号, 处理起来非常麻烦, 直接输出带问题的SQL和参数
            return sql + '\n' + ConvertTools.joinToString(params, true);
        } else {
            // 根据sql和params生成SqlBuffer对象
            SqlBuffer buffer = new SqlBuffer();
            int v = 0;
            for (int i = 0, z = sql.length(); i < z; i++) {
                char c = sql.charAt(i);
                if (c == '?') {
                    buffer.addVariable(params[v++]);
                } else {
                    buffer.append(c);
                }
            }
            return getFormattedSqlString(buffer, 1);
        }
    }

    @Override
    public void executeSqlScript(String sqlFilePath, Class<?>... classes) {
        VerifyTools.requireNotBlank(sqlFilePath, "sqlFilePath");
        URL url = PathTools.findResource(sqlFilePath, classes);
        executeSqlScript(url);
    }

    @Override
    public void executeSqlScript(URL url) {
        VerifyTools.requireNotBlank(url, "url");

        JdbcOperations jdbcOperations = namedParameterJdbcOperations.getJdbcOperations();
        if (!(jdbcOperations instanceof JdbcAccessor)) {
            throw new IllegalStateException("Unsupported JdbcOperations: " + jdbcOperations.getClass().getName());
        }
        JdbcAccessor accessor = (JdbcAccessor) jdbcOperations;
        DataSource datasource = accessor.getDataSource();
        if (datasource == null) {
            throw new IllegalStateException("Datasource is null.");
        }

        Connection connection = DataSourceUtils.getConnection(datasource);
        SqlScriptTools.executeSqlScript(connection, url, true, true, // 遇到错误继续, 忽略失败的DROP语句
            ScriptUtils.DEFAULT_COMMENT_PREFIX, // 行注释前缀: --
            ScriptUtils.DEFAULT_STATEMENT_SEPARATOR, // SQL代码块分隔符: ;
            ScriptUtils.DEFAULT_BLOCK_COMMENT_START_DELIMITER, // 块注释开始符号: /*
            ScriptUtils.DEFAULT_BLOCK_COMMENT_END_DELIMITER); // 块注释结束符号: */
    }

    @Override
    public JdbcOperations getJdbcOperations() {
        return namedParameterJdbcOperations.getJdbcOperations();
    }
}
