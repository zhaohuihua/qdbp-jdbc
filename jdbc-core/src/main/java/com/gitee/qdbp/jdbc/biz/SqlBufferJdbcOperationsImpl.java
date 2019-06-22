package com.gitee.qdbp.jdbc.biz;

import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import com.gitee.qdbp.jdbc.api.SqlBufferJdbcOperations;
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

    private NamedParameterJdbcOperations namedParameterJdbcOperations;

    // Character类型不能自动识别
    // @see org.springframework.jdbc.core.StatementCreatorUtils.setValue
    private Map<String, Object> getVariables(SqlBuffer sb) {
        Map<String, Object> params = sb.getNamedVariables();
        if (VerifyTools.isBlank(params)) {
            return params;
        }
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Character) { // Character类型不能自动识别
                map.put(key, new SqlParameterValue(Types.VARCHAR, value));
            } else {
                map.put(key, value);
            }
        }
        return map;
    }

    @Override
    public <T> T execute(SqlBuffer sb, PreparedStatementCallback<T> action) throws DataAccessException {
        if (sb != null && log.isDebugEnabled()) {
            log.debug("SQL:\n{}", DbTools.formatSql(sb, 1));
        }
        String sql = sb.getNamedSqlString();
        Map<String, Object> params = getVariables(sb);
        return namedParameterJdbcOperations.execute(sql, params, action);
    }

    @Override
    public <T> T query(SqlBuffer sb, ResultSetExtractor<T> rse) throws DataAccessException {
        if (sb != null && log.isDebugEnabled()) {
            log.debug("SQL:\n{}", DbTools.formatSql(sb, 1));
        }
        String sql = sb.getNamedSqlString();
        Map<String, Object> params = getVariables(sb);
        return namedParameterJdbcOperations.query(sql, params, rse);
    }

    @Override
    public void query(SqlBuffer sb, RowCallbackHandler rch) throws DataAccessException {
        if (sb != null && log.isDebugEnabled()) {
            log.debug("SQL:\n{}", DbTools.formatSql(sb, 1));
        }
        String sql = sb.getNamedSqlString();
        Map<String, Object> params = getVariables(sb);
        namedParameterJdbcOperations.query(sql, params, rch);
    }

    @Override
    public <T> List<T> query(SqlBuffer sb, RowMapper<T> rowMapper) throws DataAccessException {
        if (sb != null && log.isDebugEnabled()) {
            log.debug("SQL:\n{}", DbTools.formatSql(sb, 1));
        }
        String sql = sb.getNamedSqlString();
        Map<String, Object> params = getVariables(sb);
        try {
            return namedParameterJdbcOperations.query(sql, params, rowMapper);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public <T> T queryForObject(SqlBuffer sb, RowMapper<T> rowMapper) throws DataAccessException {
        if (sb != null && log.isDebugEnabled()) {
            log.debug("SQL:\n{}", DbTools.formatSql(sb, 1));
        }
        String sql = sb.getNamedSqlString();
        Map<String, Object> params = getVariables(sb);
        try {
            return namedParameterJdbcOperations.queryForObject(sql, params, rowMapper);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public <T> T queryForObject(SqlBuffer sb, Class<T> resultType) throws DataAccessException {
        try {
            if (isSimpleClass(resultType)) {
                if (sb != null && log.isDebugEnabled()) {
                    log.debug("SQL:\n{}", DbTools.formatSql(sb, 1));
                }
                String sql = sb.getNamedSqlString();
                Map<String, Object> params = getVariables(sb);
                return namedParameterJdbcOperations.queryForObject(sql, params, resultType);
            } else {
                return queryForObject(sb, new TableRowToBeanMapper<T>(resultType));
            }
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private boolean isSimpleClass(Class<?> clazz) {
        if (clazz == String.class || Number.class.isAssignableFrom(clazz) || Date.class.isAssignableFrom(clazz)
                || clazz == Boolean.class || clazz == double.class || clazz == int.class || clazz == long.class
                || clazz == short.class || clazz == float.class || clazz == boolean.class || clazz == byte.class) {
            return true;
        }
        return false;
    }

    @Override
    public Map<String, Object> queryForMap(SqlBuffer sb) throws DataAccessException {
        if (sb != null && log.isDebugEnabled()) {
            log.debug("SQL:\n{}", DbTools.formatSql(sb, 1));
        }
        String sql = sb.getNamedSqlString();
        Map<String, Object> params = getVariables(sb);
        try {
            return namedParameterJdbcOperations.queryForMap(sql, params);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public <T> List<T> queryForList(SqlBuffer sb, Class<T> elementType) throws DataAccessException {
        String sql = sb.getNamedSqlString();
        Map<String, Object> params = getVariables(sb);
        if (isSimpleClass(elementType)) {
            return namedParameterJdbcOperations.queryForList(sql, params, elementType);
        } else {
            return namedParameterJdbcOperations.query(sql, params, new TableRowToBeanMapper<T>(elementType));
        }
    }

    @Override
    public List<Map<String, Object>> queryForList(SqlBuffer sb) throws DataAccessException {
        if (sb != null && log.isDebugEnabled()) {
            log.debug("SQL:\n{}", DbTools.formatSql(sb, 1));
        }
        String sql = sb.getNamedSqlString();
        Map<String, Object> params = getVariables(sb);
        return namedParameterJdbcOperations.queryForList(sql, params);
    }

    @Override
    public SqlRowSet queryForRowSet(SqlBuffer sb) throws DataAccessException {
        if (sb != null && log.isDebugEnabled()) {
            log.debug("SQL:\n{}", DbTools.formatSql(sb, 1));
        }
        String sql = sb.getNamedSqlString();
        Map<String, Object> params = getVariables(sb);
        return namedParameterJdbcOperations.queryForRowSet(sql, params);
    }

    @Override
    public int update(SqlBuffer sb) throws DataAccessException {
        if (sb != null && log.isDebugEnabled()) {
            log.debug("SQL:\n{}", DbTools.formatSql(sb, 1));
        }
        String sql = sb.getNamedSqlString();
        Map<String, Object> params = getVariables(sb);
        return namedParameterJdbcOperations.update(sql, params);
    }

    @Override
    public int update(SqlBuffer sb, KeyHolder generatedKeyHolder) throws DataAccessException {
        if (sb != null && log.isDebugEnabled()) {
            log.debug("SQL:\n{}", DbTools.formatSql(sb, 1));
        }
        String sql = sb.getNamedSqlString();
        Map<String, Object> params = getVariables(sb);
        SqlParameterSource msps = new MapSqlParameterSource(params);
        return namedParameterJdbcOperations.update(sql, msps, generatedKeyHolder);
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
