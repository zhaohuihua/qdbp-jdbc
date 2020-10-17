package com.gitee.qdbp.jdbc.api;

import java.net.URL;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import com.gitee.qdbp.able.exception.ServiceException;
import com.gitee.qdbp.jdbc.model.DbVersion;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.tools.files.PathTools;

/**
 * SqlBuffer数据库操作类<br>
 * 参考自spring-jdbc NamedParameterJdbcOperations
 *
 * @author 赵卉华
 * @version 190601
 */
public interface SqlBufferJdbcOperations {

    /**
     * 查找数据库版本信息(从当前数据源查找)
     * 
     * @return 数据库版本信息
     */
    DbVersion findDbVersion();

    /**
     * 查找数据库版本信息并生成SQL方言处理类
     * 
     * @return SQL方言处理类
     */
    SqlDialect findSqlDialect();

    /**
     * Execute a JDBC data access operation, implemented as callback action
     * working on a JDBC PreparedStatement. This allows for implementing arbitrary
     * data access operations on a single Statement, within Spring's managed
     * JDBC environment: that is, participating in Spring-managed transactions
     * and converting JDBC SQLExceptions into Spring's ServiceException hierarchy.
     * <p>The callback action can return a result object, for example a
     * domain object or a collection of domain objects.
     * @param sb SqlBuffer
     * @param action callback object that specifies the action
     * @return a result object returned by the action, or {@code null}
     * @throws ServiceException if there is any problem
     */
    <T> T execute(SqlBuffer sb, PreparedStatementCallback<T> action) throws ServiceException;

    /**
     * Query given SQL to create a prepared statement from SQL and a list
     * of arguments to bind to the query, reading the ResultSet with a
     * ResultSetExtractor.
     * @param sb SqlBuffer
     * @param rse object that will extract results
     * @return an arbitrary result object, as returned by the ResultSetExtractor
     * @throws ServiceException if the query fails
     */
    <T> T query(SqlBuffer sb, ResultSetExtractor<T> rse) throws ServiceException;

    /**
     * Query given SQL to create a prepared statement from SQL and a list of
     * arguments to bind to the query, reading the ResultSet on a per-row basis
     * with a RowCallbackHandler.
     * @param sb SqlBuffer
     * @param rch object that will extract results, one row at a time
     * @throws ServiceException if the query fails
     */
    void query(SqlBuffer sb, RowCallbackHandler rch) throws ServiceException;

    /**
     * Query given SQL to create a prepared statement from SQL and a list
     * of arguments to bind to the query, mapping each row to a Java object
     * via a RowMapper.
     * @param sb SqlBuffer
     * @param rowMapper object that will map one object per row
     * @return the result List, containing mapped objects
     * @throws ServiceException if the query fails
     */
    <T> List<T> query(SqlBuffer sb, RowMapper<T> rowMapper) throws ServiceException;

    /**
     * Query given SQL to create a prepared statement from SQL and a list
     * of arguments to bind to the query, mapping a single result row to a
     * Java object via a RowMapper.
     * @param sql SQL query to execute
     * @param paramSource container of arguments to bind to the query
     * @param rowMapper object that will map one object per row
     * @return the single mapped object
     * @throws org.springframework.dao.IncorrectResultSizeServiceException
     * if the query does not return exactly one row, or does not return exactly
     * one column in that row
     * @throws ServiceException if the query fails
     */
    <T> T queryForObject(SqlBuffer sb, RowMapper<T> rowMapper) throws ServiceException;

    /**
     * Query given SQL to create a prepared statement from SQL and a
     * list of arguments to bind to the query, expecting a result object.
     * <p>The query is expected to be a single row/single column query; the returned
     * result will be directly mapped to the corresponding object type.
     * @param sb SqlBuffer
     * @param requiredType the type that the result object is expected to match
     * @return the result object of the required type, or {@code null} in case of SQL NULL
     * @throws org.springframework.dao.IncorrectResultSizeServiceException
     * if the query does not return exactly one row, or does not return exactly
     * one column in that row
     * @throws ServiceException if the query fails
     * @see org.springframework.jdbc.core.JdbcTemplate#queryForObject(String, Class)
     * @see com.gitee.qdbp.able.jdbc.utils.DbTools#mapToJavaBean(Map, Class)
     */
    <T> T queryForObject(SqlBuffer sb, Class<T> requiredType) throws ServiceException;

    /**
     * Query given SQL to create a prepared statement from SQL and a
     * list of arguments to bind to the query, expecting a result Map.
     * <p>The query is expected to be a single row query; the result row will be
     * mapped to a Map (one entry for each column, using the column name as the key).
     * @param sb SqlBuffer
     * @return the result Map (one entry for each column, using the column name as the key)
     * @throws org.springframework.dao.IncorrectResultSizeServiceException
     * if the query does not return exactly one row
     * @throws ServiceException if the query fails
     * @see org.springframework.jdbc.core.JdbcTemplate#queryForMap(String)
     * @see org.springframework.jdbc.core.ColumnMapRowMapper
     */
    Map<String, Object> queryForMap(SqlBuffer sb) throws ServiceException;

    /**
     * Query given SQL to create a prepared statement from SQL and a
     * list of arguments to bind to the query, expecting a result list.
     * <p>The results will be mapped to a List (one entry for each row) of
     * result objects, each of them matching the specified element type.
     * @param sb SqlBuffer
     * @param elementType the required type of element in the result list
     * (for example, {@code Integer.class})
     * @return a List of objects that match the specified element type
     * @throws ServiceException if the query fails
     * @see org.springframework.jdbc.core.JdbcTemplate#queryForList(String, Class)
     * @see com.gitee.qdbp.able.jdbc.utils.DbTools#mapToJavaBean(Map, Class)
     */
    <T> List<T> queryForList(SqlBuffer sb, Class<T> elementType) throws ServiceException;

    /**
     * Query given SQL to create a prepared statement from SQL and a
     * list of arguments to bind to the query, expecting a result list.
     * <p>The results will be mapped to a List (one entry for each row) of
     * Maps (one entry for each column, using the column name as the key).
     * Each element in the list will be of the form returned by this interface's
     * {@code queryForMap} methods.
     * @param sb SqlBuffer
     * @return a List that contains a Map per row
     * @throws ServiceException if the query fails
     * @see org.springframework.jdbc.core.JdbcTemplate#queryForList(String)
     */
    List<Map<String, Object>> queryForList(SqlBuffer sb) throws ServiceException;

    /**
     * Query given SQL to create a prepared statement from SQL and a
     * list of arguments to bind to the query, expecting a SqlRowSet.
     * <p>The results will be mapped to an SqlRowSet which holds the data in a
     * disconnected fashion. This wrapper will translate any SQLExceptions thrown.
     * <p>Note that that, for the default implementation, JDBC RowSet support needs to
     * be available at runtime: by default, Sun's {@code com.sun.rowset.CachedRowSetImpl}
     * class is used, which is part of JDK 1.5+ and also available separately as part of
     * Sun's JDBC RowSet Implementations download (rowset.jar).
     * @param sb SqlBuffer
     * @return a SqlRowSet representation (possibly a wrapper around a
     * {@code javax.sql.rowset.CachedRowSet})
     * @throws ServiceException if there is any problem executing the query
     * @see org.springframework.jdbc.core.JdbcTemplate#queryForRowSet(String)
     * @see org.springframework.jdbc.core.SqlRowSetResultSetExtractor
     * @see javax.sql.rowset.CachedRowSet
     */
    SqlRowSet queryForRowSet(SqlBuffer sb) throws ServiceException;

    /**
     * Issue an insert via a prepared statement, binding the given arguments.
     * @param sb SqlBuffer
     * @return the number of rows affected
     * @throws ServiceException if there is any problem issuing the insert
     */
    int insert(SqlBuffer sb) throws ServiceException;

    /**
     * Issue an insert via a prepared statement, binding the given arguments,
     * returning generated keys.
     * @param sb SqlBuffer
     * @param generatedKeyHolder KeyHolder that will hold the generated keys
     * @return the number of rows affected
     * @throws ServiceException if there is any problem issuing the insert
     * @see MapSqlParameterSource
     * @see org.springframework.jdbc.support.GeneratedKeyHolder
     */
    int insert(SqlBuffer sb, KeyHolder generatedKeyHolder) throws ServiceException;

    /**
     * Issue an update via a prepared statement, binding the given arguments.
     * @param sb SqlBuffer
     * @return the number of rows affected
     * @throws ServiceException if there is any problem issuing the update
     */
    int update(SqlBuffer sb) throws ServiceException;

    /**
     * Issue an delete via a prepared statement, binding the given arguments.
     * @param sb SqlBuffer
     * @return the number of rows affected
     * @throws ServiceException if there is any problem issuing the update
     */
    int delete(SqlBuffer sb) throws ServiceException;

    /**
     * Issue an batch insert via a prepared statement, binding the given arguments.
     * @param sb SqlBuffer
     * @return the number of rows affected
     * @throws ServiceException if there is any problem issuing the insert
     */
    int batchInsert(SqlBuffer sb) throws ServiceException;

    /**
     * Issue an batch update via a prepared statement, binding the given arguments.
     * @param sb SqlBuffer
     * @return the number of rows affected
     * @throws ServiceException if there is any problem issuing the update
     */
    int batchUpdate(SqlBuffer sb) throws ServiceException;
    
    /**
	 * 根据查询语句统计记录总数 (解析查询语句生成统计语句)
	 
     * @param querySql 查询语句
	 * @return 记录总数
     * @since 3.2.0
	 */
    int countByQuerySql(SqlBuffer querySql) throws ServiceException;

    /**
     * 执行SQL脚本<br>
     * 如: executeSqlScript("file:/home/sqls/create.tables.oracle.sql");<br>
     * 如: executeSqlScript("classpath:settings/sqls/create.tables.oracle.sql");<br>
     * 如: executeSqlScript("classpath:settings/sqls/create.tables.oracle.sql", Xxx.class);<br>
     * 
     * @param sqlFilePath SQL文件路径
     * @params classes 备选查找位置
     * @see PathTools#findResource(String, Class...)
     * @since 3.1.0
     */
    void executeSqlScript(String sqlFilePath, Class<?>... classes);

    /**
     * 执行SQL脚本
     * 
     * @param sqlFilePath SQL文件路径
     * @since 3.2.0
     */
    void executeSqlScript(URL sqlFilePath);

    /**
     * 获取SQL执行接口
     * 
     * @return SqlDao
     * @since 3.2.0
     */
    SqlDao getSqlDao();

    /**
     * Expose the classic Spring JdbcTemplate to allow invocation of
     * classic JDBC operations.
     */
    JdbcOperations getJdbcOperations();
}
