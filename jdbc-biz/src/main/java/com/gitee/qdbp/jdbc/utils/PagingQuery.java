package com.gitee.qdbp.jdbc.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.RowMapper;
import com.gitee.qdbp.able.jdbc.paging.Paging;
import com.gitee.qdbp.able.jdbc.paging.PartList;
import com.gitee.qdbp.jdbc.api.SqlBufferJdbcOperations;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;

/**
 * 分页查询工具类
 *
 * @author zhaohuihua
 * @version 190607
 */
public class PagingQuery {

    /**
     * 分页查询
     * 
     * @param jdbc JDBC查询接口
     * @param qsb 数据查询SQL语句
     * @param csb 总数统计SQL语句
     * @param paging 分页条件
     * @return a PartList that contains a Map per row and total rows.
     * @throws DataAccessException if the query fails
     */
    public static PartList<Map<String, Object>> queryForList(SqlBufferJdbcOperations jdbc, SqlBuffer qsb, SqlBuffer csb,
            Paging paging) throws DataAccessException {
        if (!paging.isPaging()) { // 不分页
            List<Map<String, Object>> list = jdbc.query(qsb, new ColumnMapRowMapper());
            return list == null ? null : new PartList<>(list, list.size());
        } else { // 分页
            // 先查询总数据量
            Integer total = null;
            if (paging.isNeedCount()) {
                total = jdbc.queryForObject(csb, Integer.class);
            }
            // 再查询数据列表
            List<Map<String, Object>> list;
            if (total != null && total == 0) {
                list = new ArrayList<>(); // 已知无数据, 不需要再查询
            } else {
                SqlDialect dialect = DbTools.getSqlDialect();
                // 处理分页
                dialect.processPagingSql(qsb, paging);
                // 查询数据列表
                list = jdbc.query(qsb, new ColumnMapRowMapper());
            }
            return new PartList<>(list, total == null ? list.size() : total);
        }
    }

    /**
     * 分页查询
     * 
     * @param qsb 数据查询SQL语句
     * @param csb 总数统计SQL语句
     * @param paging 分页条件
     * @param elementType 结果成员类型
     * @return a PartList that contains a Map per row and total rows.
     * @throws DataAccessException if the query fails
     */
    public static <T> PartList<T> queryForList(SqlBufferJdbcOperations jdbc, SqlBuffer qsb, SqlBuffer csb,
            Paging paging, Class<T> elementType) throws DataAccessException {
        if (!paging.isPaging()) { // 不分页
            List<T> list = jdbc.queryForList(qsb, elementType);
            return list == null ? null : new PartList<>(list, list.size());
        } else { // 分页
            // 先查询总数据量
            Integer total = null;
            if (paging.isNeedCount()) {
                total = jdbc.queryForObject(csb, Integer.class);
            }
            // 再查询数据列表
            List<T> list;
            if (total != null && total == 0) {
                list = new ArrayList<T>(); // 已知无数据, 不需要再查询
            } else {
                SqlDialect dialect = DbTools.getSqlDialect();
                // 处理分页
                dialect.processPagingSql(qsb, paging);
                // 查询数据列表
                list = jdbc.queryForList(qsb, elementType);
            }
            return new PartList<>(list, total == null ? list.size() : total);
        }
    }

    /**
     * 分页查询
     * 
     * @param qsb 数据查询SQL语句
     * @param csb 总数统计SQL语句
     * @param paging 分页条件
     * @param rowMapper 结果转换接口
     * @return a PartList that contains a Map per row and total rows.
     * @throws DataAccessException if the query fails
     */
    public static <T> PartList<T> queryForList(SqlBufferJdbcOperations jdbc, SqlBuffer qsb, SqlBuffer csb,
            Paging paging, RowMapper<T> rowMapper) throws DataAccessException {
        if (!paging.isPaging()) { // 不分页
            List<T> list = jdbc.query(qsb, rowMapper);
            return list == null ? null : new PartList<>(list, list.size());
        } else { // 分页
            // 先查询总数据量
            Integer total = null;
            if (paging.isNeedCount()) {
                total = jdbc.queryForObject(csb, Integer.class);
            }
            // 再查询数据列表
            List<T> list;
            if (total != null && total == 0) {
                list = new ArrayList<T>(); // 已知无数据, 不需要再查询
            } else {
                SqlDialect dialect = DbTools.getSqlDialect();
                // 处理分页
                dialect.processPagingSql(qsb, paging);
                // 查询数据列表
                list = jdbc.query(qsb, rowMapper);
            }
            return new PartList<>(list, total == null ? list.size() : total);
        }
    }
}
