package com.gitee.qdbp.jdbc.biz;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
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
import com.gitee.qdbp.able.model.paging.Paging;
import com.gitee.qdbp.able.model.paging.PartList;
import com.gitee.qdbp.jdbc.api.SqlBufferJdbcOperations;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.SqlDialect;
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
    public <T> T queryForObject(SqlBuffer sb, Class<T> requiredType) throws DataAccessException {
        try {
            if (requiredType == String.class || Number.class.isAssignableFrom(requiredType)) {
                if (sb != null && log.isDebugEnabled()) {
                    log.debug("SQL:\n{}", DbTools.formatSql(sb, 1));
                }
                String sql = sb.getNamedSqlString();
                Map<String, Object> params = getVariables(sb);
                return namedParameterJdbcOperations.queryForObject(sql, params, requiredType);
            } else {
                Map<String, Object> map = queryForMap(sb);
                return map == null ? null : DbTools.resultToBean(map, requiredType);
            }
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
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
        // 执行数据库查询
        List<Map<String, Object>> list = queryForList(sb);
        if (list == null) {
            return null;
        } else if (list.isEmpty()) {
            return new ArrayList<T>();
        } else {
            // 结果转换
            List<T> results = new ArrayList<T>();
            for (Map<String, Object> entry : list) {
                results.add(DbTools.resultToBean(entry, elementType));
            }
            return results;
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
    public PartList<Map<String, Object>> queryForList(SqlBuffer qsb, SqlBuffer csb, Paging paging, SqlDialect dialect)
            throws DataAccessException {
        if (!paging.isPaging()) { // 不分页
            List<Map<String, Object>> list = query(qsb, new ColumnMapRowMapper());
            return list == null ? null : new PartList<>(list, list.size());
        } else { // 分页
            // 先查询总数据量
            Integer total = null;
            if (paging.isNeedCount()) {
                total = queryForObject(csb, Integer.class);
            }
            // 再查询数据列表
            List<Map<String, Object>> list;
            if (total != null && total == 0) {
                list = new ArrayList<>(); // 已知无数据, 不需要再查询
            } else {
                // 处理分页
                dialect.processPagingSql(qsb, paging);
                // 查询数据列表
                list = query(qsb, new ColumnMapRowMapper());
            }
            return new PartList<>(list, total == null ? list.size() : total);
        }
    }

    @Override
    public <T> PartList<T> queryForList(SqlBuffer qsb, SqlBuffer csb, Paging paging, SqlDialect dialect,
            Class<T> elementType) throws DataAccessException {
        if (!paging.isPaging()) { // 不分页
            List<T> list = queryForList(qsb, elementType);
            return list == null ? null : new PartList<>(list, list.size());
        } else { // 分页
            // 先查询总数据量
            Integer total = null;
            if (paging.isNeedCount()) {
                total = queryForObject(csb, Integer.class);
            }
            // 再查询数据列表
            List<T> list;
            if (total != null && total == 0) {
                list = new ArrayList<T>(); // 已知无数据, 不需要再查询
            } else {
                // 处理分页
                dialect.processPagingSql(qsb, paging);
                // 查询数据列表
                list = queryForList(qsb, elementType);
            }
            return new PartList<>(list, total == null ? list.size() : total);
        }
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
