package com.gitee.qdbp.jdbc.biz;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
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
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.JdbcAccessor;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import com.gitee.qdbp.jdbc.api.SqlBufferJdbcOperations;
import com.gitee.qdbp.jdbc.model.DbVersion;
import com.gitee.qdbp.jdbc.plugins.MapToBeanConverter;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.result.RowToBeanMapper;
import com.gitee.qdbp.jdbc.result.TableRowToBeanMapper;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.utils.DbTools;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * SqlBuffer数据库操作类
 *
 * @author 赵卉华
 * @version 190601
 */
public class SqlBufferJdbcOperationsImpl implements SqlBufferJdbcOperations {

    private static Logger log = LoggerFactory.getLogger(SqlBufferJdbcOperationsImpl.class);

    private DbVersion dbVersion;
    private SqlDialect sqlDialect;
    private Lock jdbcInitLock = new ReentrantLock();
    private NamedParameterJdbcOperations namedParameterJdbcOperations;

    /**
     * 查找数据库版本信息(从当前数据源查找)
     * 
     * @return 数据库版本信息
     */
    @Override
    public DbVersion findDbVersion() {
        init();
        return dbVersion;
    }

    /**
     * 查找数据库版本信息并生成SQL方言处理类
     * 
     * @return SQL方言处理类
     */
    @Override
    public SqlDialect findSqlDialect() {
        init();
        return sqlDialect;
    }

    public void init() {
        if (dbVersion != null) {
            return;
        }
        JdbcOperations jdbcOperations = namedParameterJdbcOperations.getJdbcOperations();
        if (jdbcOperations instanceof JdbcAccessor) {
            JdbcAccessor accessor = (JdbcAccessor) jdbcOperations;
            DataSource datasource = accessor.getDataSource();
            if (datasource == null) {
                throw new IllegalStateException("Datasource is null.");
            }
            jdbcInitLock.lock();
            try {
                if (dbVersion == null) {
                    dbVersion = DbTools.findDbVersion(datasource);
                    sqlDialect = DbTools.buildSqlDialect(dbVersion);
                    log.trace("Database version: {}", dbVersion);
                }
            } finally {
                jdbcInitLock.unlock();
            }
            return;
        }
        throw new IllegalStateException("Unsupported JdbcOperations: " + jdbcOperations.getClass().getName());
    }

    private <T> RowToBeanMapper<T> newRowToBeanMapper(Class<T> clazz) {
        MapToBeanConverter converter = DbTools.getMapToBeanConverter();
        return new TableRowToBeanMapper<>(clazz, converter);
    }

    private String getFormattedSqlString(SqlBuffer sb, int indent) {
        String sql = sb.getExecutableSqlString(sqlDialect);
        return DbTools.formatSql(sql, 1);
    }

    @Override
    public <T> T execute(SqlBuffer sb, PreparedStatementCallback<T> action) throws DataAccessException {
        VerifyTools.requireNotBlank(sb, "sqlBuffer");
        long startTime = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("Executing SQL statement:\n{}", getFormattedSqlString(sb, 1));
        }
        String sql = sb.getPreparedSqlString();
        Map<String, Object> params = sb.getPreparedVariables();
        try {
            T result = namedParameterJdbcOperations.execute(sql, params, action);
            if (log.isDebugEnabled()) {
                long time = System.currentTimeMillis() - startTime;
                log.debug("SQL query returns {} rows, elapsed time {}ms.", result == null ? 0 : 1, time);
            }
            return result;
        } catch (EmptyResultDataAccessException e) {
            if (log.isDebugEnabled()) {
                log.debug("SQL query returns 0 row.");
            }
            return null;
        }
    }

    @Override
    public <T> T query(SqlBuffer sb, ResultSetExtractor<T> rse) throws DataAccessException {
        VerifyTools.requireNotBlank(sb, "sqlBuffer");
        long startTime = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("Executing SQL query:\n{}", getFormattedSqlString(sb, 1));
        }
        String sql = sb.getPreparedSqlString();
        Map<String, Object> params = sb.getPreparedVariables();
        try {
            T result = namedParameterJdbcOperations.query(sql, params, rse);
            if (log.isDebugEnabled()) {
                long time = System.currentTimeMillis() - startTime;
                log.debug("SQL query returns {} rows, elapsed time {}ms.", result == null ? 0 : 1, time);
            }
            return result;
        } catch (EmptyResultDataAccessException e) {
            if (log.isDebugEnabled()) {
                long time = System.currentTimeMillis() - startTime;
                log.debug("SQL query returns 0 row, {}ms.", time);
            }
            return null;
        }
    }

    @Override
    public void query(SqlBuffer sb, RowCallbackHandler rch) throws DataAccessException {
        VerifyTools.requireNotBlank(sb, "sqlBuffer");
        long startTime = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("Executing SQL query:\n{}", getFormattedSqlString(sb, 1));
        }
        String sql = sb.getPreparedSqlString();
        Map<String, Object> params = sb.getPreparedVariables();
        namedParameterJdbcOperations.query(sql, params, rch);
        if (log.isDebugEnabled()) {
            long time = System.currentTimeMillis() - startTime;
            log.debug("SQL query, elapsed time {}ms.", time);
        }
    }

    @Override
    public <T> List<T> query(SqlBuffer sb, RowMapper<T> rowMapper) throws DataAccessException {
        VerifyTools.requireNotBlank(sb, "sqlBuffer");
        long startTime = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("Executing SQL query:\n{}", getFormattedSqlString(sb, 1));
        }
        String sql = sb.getPreparedSqlString();
        Map<String, Object> params = sb.getPreparedVariables();
        List<T> list = namedParameterJdbcOperations.query(sql, params, rowMapper);
        if (log.isDebugEnabled()) {
            long time = System.currentTimeMillis() - startTime;
            log.debug("SQL query returns {} rows, elapsed time {}ms.", list == null ? 0 : list.size(), time);
        }
        return list;
    }

    @Override
    public <T> T queryForObject(SqlBuffer sb, RowMapper<T> rowMapper) throws DataAccessException {
        VerifyTools.requireNotBlank(sb, "sqlBuffer");
        long startTime = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("Executing SQL query:\n{}", getFormattedSqlString(sb, 1));
        }
        String sql = sb.getPreparedSqlString();
        Map<String, Object> params = sb.getPreparedVariables();
        try {
            T result = namedParameterJdbcOperations.queryForObject(sql, params, rowMapper);
            if (log.isDebugEnabled()) {
                long time = System.currentTimeMillis() - startTime;
                log.debug("SQL query returns {} rows, elapsed time {}ms.", result == null ? 0 : 1, time);
            }
            return result;
        } catch (EmptyResultDataAccessException e) {
            if (log.isDebugEnabled()) {
                long time = System.currentTimeMillis() - startTime;
                log.debug("SQL query returns 0 row, {}ms.", time);
            }
            return null;
        }
    }

    @Override
    public <T> T queryForObject(SqlBuffer sb, Class<T> resultType) throws DataAccessException {
        long startTime = System.currentTimeMillis();
        try {
            T result;
            if (isSimpleClass(resultType)) {
                VerifyTools.requireNotBlank(sb, "sqlBuffer");
                if (log.isDebugEnabled()) {
                    log.debug("Executing SQL query:\n{}", getFormattedSqlString(sb, 1));
                }
                String sql = sb.getPreparedSqlString();
                Map<String, Object> params = sb.getPreparedVariables();
                result = namedParameterJdbcOperations.queryForObject(sql, params, resultType);
            } else {
                result = queryForObject(sb, newRowToBeanMapper(resultType));
            }
            if (log.isDebugEnabled()) {
                long time = System.currentTimeMillis() - startTime;
                log.debug("SQL query returns {} rows, elapsed time {}ms.", result == null ? 0 : 1, time);
            }
            return result;
        } catch (EmptyResultDataAccessException e) {
            if (log.isDebugEnabled()) {
                long time = System.currentTimeMillis() - startTime;
                log.debug("SQL query returns 0 row, {}ms.", time);
            }
            return null;
        }
    }

    @Override
    public Map<String, Object> queryForMap(SqlBuffer sb) throws DataAccessException {
        VerifyTools.requireNotBlank(sb, "sqlBuffer");
        long startTime = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("Executing SQL query:\n{}", getFormattedSqlString(sb, 1));
        }
        String sql = sb.getPreparedSqlString();
        Map<String, Object> params = sb.getPreparedVariables();
        try {
            Map<String, Object> result = namedParameterJdbcOperations.queryForMap(sql, params);
            if (log.isDebugEnabled()) {
                long time = System.currentTimeMillis() - startTime;
                log.debug("SQL query returns {} rows, elapsed time {}ms.", result == null ? 0 : 1, time);
            }
            return result;
        } catch (EmptyResultDataAccessException e) {
            if (log.isDebugEnabled()) {
                long time = System.currentTimeMillis() - startTime;
                log.debug("SQL query returns 0 row, {}ms.", time);
            }
            return null;
        }
    }

    @Override
    public <T> List<T> queryForList(SqlBuffer sb, Class<T> elementType) throws DataAccessException {
        VerifyTools.requireNotBlank(sb, "sqlBuffer");
        long startTime = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("Executing SQL query:\n{}", getFormattedSqlString(sb, 1));
        }
        String sql = sb.getPreparedSqlString();
        Map<String, Object> params = sb.getPreparedVariables();
        List<T> list;
        if (isSimpleClass(elementType)) {
            list = namedParameterJdbcOperations.queryForList(sql, params, elementType);
        } else {
            list = namedParameterJdbcOperations.query(sql, params, newRowToBeanMapper(elementType));
        }
        if (log.isDebugEnabled()) {
            long time = System.currentTimeMillis() - startTime;
            log.debug("SQL query returns {} rows, elapsed time {}ms.", list == null ? 0 : list.size(), time);
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> queryForList(SqlBuffer sb) throws DataAccessException {
        VerifyTools.requireNotBlank(sb, "sqlBuffer");
        long startTime = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("Executing SQL query:\n{}", getFormattedSqlString(sb, 1));
        }
        String sql = sb.getPreparedSqlString();
        Map<String, Object> params = sb.getPreparedVariables();
        List<Map<String, Object>> list = namedParameterJdbcOperations.queryForList(sql, params);
        if (log.isDebugEnabled()) {
            long time = System.currentTimeMillis() - startTime;
            log.debug("SQL query returns {} rows, elapsed time {}ms.", list == null ? 0 : list.size(), time);
        }
        return list;
    }

    @Override
    public SqlRowSet queryForRowSet(SqlBuffer sb) throws DataAccessException {
        VerifyTools.requireNotBlank(sb, "sqlBuffer");
        long startTime = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("Executing SQL query:\n{}", getFormattedSqlString(sb, 1));
        }
        String sql = sb.getPreparedSqlString();
        Map<String, Object> params = sb.getPreparedVariables();
        SqlRowSet result = namedParameterJdbcOperations.queryForRowSet(sql, params);
        if (log.isDebugEnabled()) {
            long time = System.currentTimeMillis() - startTime;
            log.debug("SQL query, elapsed time {}ms.", time);
        }
        return result;
    }

    @Override
    public int update(SqlBuffer sb) throws DataAccessException {
        VerifyTools.requireNotBlank(sb, "sqlBuffer");
        long startTime = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("Executing SQL update:\n{}", getFormattedSqlString(sb, 1));
        }
        String sql = sb.getPreparedSqlString();
        Map<String, Object> params = sb.getPreparedVariables();
        int rows = namedParameterJdbcOperations.update(sql, params);
        if (log.isDebugEnabled()) {
            long time = System.currentTimeMillis() - startTime;
            log.debug("SQL update affected " + rows + " rows, elapsed time {}ms", time);
        }
        return rows;
    }

    @Override
    public int update(SqlBuffer sb, KeyHolder generatedKeyHolder) throws DataAccessException {
        VerifyTools.requireNotBlank(sb, "sqlBuffer");
        long startTime = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("Executing SQL update:\n{}", getFormattedSqlString(sb, 1));
        }
        String sql = sb.getPreparedSqlString();
        Map<String, Object> params = sb.getPreparedVariables();
        SqlParameterSource msps = new MapSqlParameterSource(params);
        int rows = namedParameterJdbcOperations.update(sql, msps, generatedKeyHolder);
        if (log.isDebugEnabled()) {
            long time = System.currentTimeMillis() - startTime;
            log.debug("SQL update affected {} rows, elapsed time {}ms.", rows, time);
        }
        return rows;
    }

    private boolean isSimpleClass(Class<?> clazz) {
        return clazz == String.class || Number.class.isAssignableFrom(clazz) || Date.class.isAssignableFrom(clazz)
                || clazz == Boolean.class || clazz == double.class || clazz == int.class || clazz == long.class
                || clazz == short.class || clazz == float.class || clazz == boolean.class || clazz == byte.class;
    }

    @Override
    public JdbcOperations getJdbcOperations() {
        return namedParameterJdbcOperations.getJdbcOperations();
    }

    public NamedParameterJdbcOperations getNamedParameterJdbcOperations() {
        return namedParameterJdbcOperations;
    }

    public void setNamedParameterJdbcOperations(NamedParameterJdbcOperations namedParameterJdbcOperations) {
        this.namedParameterJdbcOperations = namedParameterJdbcOperations;
    }
}
