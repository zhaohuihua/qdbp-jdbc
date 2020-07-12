package com.gitee.qdbp.jdbc.biz;

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
        String sql = sb.getLoggingSqlString(sqlDialect);
        return DbTools.formatSql(sql, 1);
    }

    @Override
    public <T> T execute(SqlBuffer sb, PreparedStatementCallback<T> action) throws DataAccessException {
        VerifyTools.requireNotBlank(sb, "sqlBuffer");
        long startTime = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("Executing SQL statement:\n{}", getFormattedSqlString(sb, 1));
        }
        String sql = sb.getPreparedSqlString(sqlDialect);
        Map<String, Object> params = sb.getPreparedVariables(sqlDialect);
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
        String sql = sb.getPreparedSqlString(sqlDialect);
        Map<String, Object> params = sb.getPreparedVariables(sqlDialect);
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
                log.debug("SQL query returns 0 row, elapsed time {}ms.", time);
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
        String sql = sb.getPreparedSqlString(sqlDialect);
        Map<String, Object> params = sb.getPreparedVariables(sqlDialect);
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
        String sql = sb.getPreparedSqlString(sqlDialect);
        Map<String, Object> params = sb.getPreparedVariables(sqlDialect);
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
        String sql = sb.getPreparedSqlString(sqlDialect);
        Map<String, Object> params = sb.getPreparedVariables(sqlDialect);
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
                log.debug("SQL query returns 0 row, elapsed time {}ms.", time);
            }
            return null;
        }
    }

    @Override
    public <T> T queryForObject(SqlBuffer sb, Class<T> resultType) throws DataAccessException {
        long startTime = System.currentTimeMillis();
        try {
            T result;
            if (ReflectTools.isPrimitive(resultType, false)) {
                VerifyTools.requireNotBlank(sb, "sqlBuffer");
                if (log.isDebugEnabled()) {
                    log.debug("Executing SQL query:\n{}", getFormattedSqlString(sb, 1));
                }
                String sql = sb.getPreparedSqlString(sqlDialect);
                Map<String, Object> params = sb.getPreparedVariables(sqlDialect);
                result = namedParameterJdbcOperations.queryForObject(sql, params, resultType);
            } else {
                result = queryForObject(sb, newRowToBeanMapper(resultType));
            }
            if (log.isDebugEnabled()) {
                long time = System.currentTimeMillis() - startTime;
                queryForObjectLogResult(time, result);
            }
            return result;
        } catch (EmptyResultDataAccessException e) {
            if (log.isDebugEnabled()) {
                long time = System.currentTimeMillis() - startTime;
                log.debug("SQL query returns 0 row, elapsed time {}ms.", time);
            }
            return null;
        }
    }

    private void queryForObjectLogResult(long time, Object result) {
        if (result == null) {
            log.debug("SQL query returns 0 row, elapsed time {}ms.", time);
            return;
        }
        Class<?> resultType = result.getClass();
        if (resultType == String.class) {
            String string = (String) result;
            String desc = string.length() > 100 ? "String(" + string.length() + ")" : string;
            log.debug("SQL query returns 1 row, the value is {}, elapsed time {}ms.", desc, time);
        } else if (ReflectTools.isPrimitive(resultType) || resultType.isEnum()) {
            String string = result.toString();
            log.debug("SQL query returns 1 row, the value is {}, elapsed time {}ms.", string, time);
        } else {
            log.debug("SQL query returns 1 row, elapsed time {}ms.", time);
        }
    }

    @Override
    public Map<String, Object> queryForMap(SqlBuffer sb) throws DataAccessException {
        VerifyTools.requireNotBlank(sb, "sqlBuffer");
        long startTime = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("Executing SQL query:\n{}", getFormattedSqlString(sb, 1));
        }
        String sql = sb.getPreparedSqlString(sqlDialect);
        Map<String, Object> params = sb.getPreparedVariables(sqlDialect);
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
                log.debug("SQL query returns 0 row, elapsed time {}ms.", time);
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
        String sql = sb.getPreparedSqlString(sqlDialect);
        Map<String, Object> params = sb.getPreparedVariables(sqlDialect);
        List<T> list;
        if (ReflectTools.isPrimitive(elementType, false)) {
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
        String sql = sb.getPreparedSqlString(sqlDialect);
        Map<String, Object> params = sb.getPreparedVariables(sqlDialect);
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
        String sql = sb.getPreparedSqlString(sqlDialect);
        Map<String, Object> params = sb.getPreparedVariables(sqlDialect);
        SqlRowSet result = namedParameterJdbcOperations.queryForRowSet(sql, params);
        if (log.isDebugEnabled()) {
            long time = System.currentTimeMillis() - startTime;
            log.debug("SQL query, elapsed time {}ms.", time);
        }
        return result;
    }

    @Override
    public int insert(SqlBuffer sb) throws DataAccessException {
        VerifyTools.requireNotBlank(sb, "sqlBuffer");
        return doUpdate(sb, "insert");
    }

    @Override
    public int insert(SqlBuffer sb, KeyHolder generatedKeyHolder) throws DataAccessException {
        VerifyTools.requireNotBlank(sb, "sqlBuffer");
        return doUpdate(sb, generatedKeyHolder, "insert");
    }

    @Override
    public int update(SqlBuffer sb) throws DataAccessException {
        VerifyTools.requireNotBlank(sb, "sqlBuffer");
        return doUpdate(sb, "update");
    }

    @Override
    public int delete(SqlBuffer sb) throws DataAccessException {
        VerifyTools.requireNotBlank(sb, "sqlBuffer");
        return doUpdate(sb, "delete");
    }

    protected int doUpdate(SqlBuffer sb, String desc) throws DataAccessException {
        long startTime = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("Executing SQL {}:\n{}", desc, getFormattedSqlString(sb, 1));
        }
        String sql = sb.getPreparedSqlString(sqlDialect);
        Map<String, Object> params = sb.getPreparedVariables(sqlDialect);
        int rows = namedParameterJdbcOperations.update(sql, params);
        if (log.isDebugEnabled()) {
            long time = System.currentTimeMillis() - startTime;
            log.debug("SQL {} affected " + rows + " rows, elapsed time {}ms", desc, time);
        }
        return rows;
    }

    protected int doUpdate(SqlBuffer sb, KeyHolder generatedKeyHolder, String desc) throws DataAccessException {
        long startTime = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("Executing SQL {}:\n{}", desc, getFormattedSqlString(sb, 1));
        }
        String sql = sb.getPreparedSqlString(sqlDialect);
        Map<String, Object> params = sb.getPreparedVariables(sqlDialect);
        SqlParameterSource msps = new MapSqlParameterSource(params);
        int rows = namedParameterJdbcOperations.update(sql, msps, generatedKeyHolder);
        if (log.isDebugEnabled()) {
            long time = System.currentTimeMillis() - startTime;
            log.debug("SQL {} affected " + rows + " rows, elapsed time {}ms", desc, time);
        }
        return rows;
    }

    @Override
    public int batchInsert(SqlBuffer sb) throws DataAccessException {
        VerifyTools.requireNotBlank(sb, "sqlBuffer");
        return doBatchUpdate(sb, "insert");
    }

    @Override
    public int batchUpdate(SqlBuffer sb) throws DataAccessException {
        VerifyTools.requireNotBlank(sb, "sqlBuffer");
        return doBatchUpdate(sb, "update");
    }

    protected int doBatchUpdate(SqlBuffer sb, String desc) throws DataAccessException {
        long startTime = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("Executing SQL batch {}:\n{}", desc, getCompressedSqlString(sb, 1));
        }
        String sql = sb.getPreparedSqlString(sqlDialect);
        Map<String, Object> params = sb.getPreparedVariables(sqlDialect);
        int rows = namedParameterJdbcOperations.update(sql, params);
        if (log.isDebugEnabled()) {
            long time = System.currentTimeMillis() - startTime;
            log.debug("SQL {} affected batch {} rows, elapsed time {}ms.", desc, rows, time);
        }
        return rows;
    }

    protected String getCompressedSqlString(SqlBuffer sb, int indent) {
        String sqlString = getFormattedSqlString(sb, indent);
        if (sqlString.trim().indexOf('\n') < 0) {
            return StringTools.ellipsis(sqlString, 200);
        } else {
            List<String> temp = ConvertTools.toList(StringTools.split(sqlString, false, '\n'));
            int size = temp.size();
            if (size <= 7) {
                return sqlString;
            } else { // 取前3行+后3行
                StringBuilder buffer = new StringBuilder();
                buffer.append(temp.get(0)).append('\n');
                buffer.append(temp.get(1)).append('\n');
                buffer.append(temp.get(2)).append('\n');
                buffer.append('\t').append("...").append('(').append(size - 6).append(')').append('\n');
                buffer.append(temp.get(size - 3)).append('\n');
                buffer.append(temp.get(size - 2)).append('\n');
                buffer.append(temp.get(size - 1));
                return buffer.toString();
            }
        }
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
