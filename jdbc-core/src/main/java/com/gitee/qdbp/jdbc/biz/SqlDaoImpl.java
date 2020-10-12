package com.gitee.qdbp.jdbc.biz;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.RowMapper;
import com.gitee.qdbp.able.jdbc.paging.PageList;
import com.gitee.qdbp.able.jdbc.paging.Paging;
import com.gitee.qdbp.jdbc.api.SqlBufferJdbcOperations;
import com.gitee.qdbp.jdbc.api.SqlDao;
import com.gitee.qdbp.jdbc.plugins.BeanToMapConverter;
import com.gitee.qdbp.jdbc.plugins.MapToBeanConverter;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.result.RowToBeanMapper;
import com.gitee.qdbp.jdbc.result.TableRowToBeanMapper;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.parse.SqlFragmentContainer;
import com.gitee.qdbp.jdbc.utils.DbTools;

/**
 * 执行SQL语句的处理类<br>
 * SQL语句配置在SQL模板文件中, 系统启动时预加载到缓存, 使用时通过sqlId调用
 *
 * @author zhaohuihua
 * @version 20200903
 * @since 3.2.0
 */
public class SqlDaoImpl implements SqlDao {

    protected SqlFragmentContainer container;
    protected SqlBufferJdbcOperations jdbc;
    protected SqlDialect dialect;

    public SqlDaoImpl(SqlFragmentContainer container, SqlBufferJdbcOperations jdbcOperations) {
        this.container = container;
        this.jdbc = jdbcOperations;
        this.dialect = jdbcOperations.findSqlDialect();
    }

    protected <T> RowToBeanMapper<T> newRowToBeanMapper(Class<T> clazz) {
        MapToBeanConverter converter = DbTools.getMapToBeanConverter();
        return new TableRowToBeanMapper<>(clazz, converter);
    }

    protected Map<String, Object> beanToMap(Object bean) {
        BeanToMapConverter beanToMapConverter = DbTools.getBeanToMapConverter();
        // deep=false: 不需要递归转换; 字段是实体类的不需要转换
        // clearBlankValue=true: 清理空值
        return beanToMapConverter.convert(bean, false, true);
    }

    @Override
    public <T> T findForObject(String sqlId, Object params, Class<T> resultType) {
        Map<String, Object> map = params == null ? null : beanToMap(params);
        SqlBuffer sql = container.render(sqlId, map, dialect);
        return jdbc.queryForObject(sql, resultType);
    }

    @Override
    public <T> T findForObject(String sqlId, Object params, RowMapper<T> rowMapper) {
        Map<String, Object> map = params == null ? null : beanToMap(params);
        SqlBuffer sql = container.render(sqlId, map, dialect);
        return jdbc.queryForObject(sql, rowMapper);
    }

    @Override
    public Map<String, Object> findForMap(String sqlId, Object params) {
        Map<String, Object> map = params == null ? null : beanToMap(params);
        SqlBuffer sql = container.render(sqlId, map, dialect);
        return jdbc.queryForMap(sql);
    }

    @Override
    public <T> List<T> listForObjects(String sqlId, Object params, Class<T> resultType) {
        Map<String, Object> map = params == null ? null : beanToMap(params);
        SqlBuffer sql = container.render(sqlId, map, dialect);
        return jdbc.queryForList(sql, resultType);
    }

    @Override
    public <T> List<T> listForObjects(String sqlId, Object params, RowMapper<T> rowMapper) {
        Map<String, Object> map = params == null ? null : beanToMap(params);
        SqlBuffer sql = container.render(sqlId, map, dialect);
        return jdbc.query(sql, rowMapper);
    }

    @Override
    public List<Map<String, Object>> listForMaps(String sqlId, Object params) {
        Map<String, Object> map = params == null ? null : beanToMap(params);
        SqlBuffer sql = container.render(sqlId, map, dialect);
        return jdbc.queryForList(sql);
    }

    @Override
    public <T> PageList<T> pageForObjects(String sqlId, Object params, Paging paging, Class<T> resultType) {
        Map<String, Object> map = params == null ? null : beanToMap(params);
        SqlBuffer sql = container.render(sqlId, map, dialect);

        // 先查询总数据量
        Integer total = paging.getTotal();
        if (paging.isNeedCount()) {
            total = jdbc.countByQuerySql(sql);
            paging.setTotal(total);
        }
        // 再查询数据列表
        List<T> list;
        if (total != null && total == 0) {
            list = new ArrayList<T>(); // 已知无数据, 不需要再查询
        } else {
            SqlDialect dialect = jdbc.findSqlDialect();
            // 处理分页
            dialect.processPagingSql(sql, paging);
            // 查询数据列表
            list = jdbc.queryForList(sql, resultType);
        }
        return new PageList<>(list, total == null ? list.size() : total);
    }

    @Override
    public <T> PageList<T> pageForObjects(String sqlId, Object params, Paging paging, RowMapper<T> rowMapper) {
        Map<String, Object> map = params == null ? null : beanToMap(params);
        SqlBuffer sql = container.render(sqlId, map, dialect);

        // 先查询总数据量
        Integer total = paging.getTotal();
        if (paging.isNeedCount()) {
            total = jdbc.countByQuerySql(sql);
            paging.setTotal(total);
        }
        // 再查询数据列表
        List<T> list;
        if (total != null && total == 0) {
            list = new ArrayList<T>(); // 已知无数据, 不需要再查询
        } else {
            SqlDialect dialect = jdbc.findSqlDialect();
            // 处理分页
            dialect.processPagingSql(sql, paging);
            // 查询数据列表
            list = jdbc.query(sql, rowMapper);
        }
        return new PageList<>(list, total == null ? list.size() : total);
    }

    @Override
    public PageList<Map<String, Object>> pageForMaps(String sqlId, Object params, Paging paging) {
        Map<String, Object> map = params == null ? null : beanToMap(params);
        SqlBuffer sql = container.render(sqlId, map, dialect);

        // 先查询总数据量
        Integer total = paging.getTotal();
        if (paging.isNeedCount()) {
            total = jdbc.countByQuerySql(sql);
            paging.setTotal(total);
        }
        // 再查询数据列表
        List<Map<String, Object>> list;
        if (total != null && total == 0) {
            list = new ArrayList<Map<String, Object>>(); // 已知无数据, 不需要再查询
        } else {
            SqlDialect dialect = jdbc.findSqlDialect();
            // 处理分页
            dialect.processPagingSql(sql, paging);
            // 查询数据列表
            list = jdbc.queryForList(sql);
        }
        return new PageList<>(list, total == null ? list.size() : total);
    }

    @Override
    public <T> PageList<T> pageForObjects(String queryId, String countId, Object params, Paging paging,
            Class<T> resultType) {
        Map<String, Object> map = params == null ? null : beanToMap(params);
        SqlBuffer querySql = container.render(queryId, map, dialect);

        // 先查询总数据量
        Integer total = paging.getTotal();
        if (paging.isNeedCount()) {
            SqlBuffer countSql = container.render(countId, map, dialect);
            total = jdbc.queryForObject(countSql, Integer.class);
            paging.setTotal(total);
        }
        // 再查询数据列表
        List<T> list;
        if (total != null && total == 0) {
            list = new ArrayList<T>(); // 已知无数据, 不需要再查询
        } else {
            SqlDialect dialect = jdbc.findSqlDialect();
            // 处理分页
            dialect.processPagingSql(querySql, paging);
            // 查询数据列表
            list = jdbc.queryForList(querySql, resultType);
        }
        return new PageList<>(list, total == null ? list.size() : total);
    }

    @Override
    public <T> PageList<T> pageForObjects(String queryId, String countId, Object params, Paging paging,
            RowMapper<T> rowMapper) {
        Map<String, Object> map = params == null ? null : beanToMap(params);
        SqlBuffer querySql = container.render(queryId, map, dialect);

        // 先查询总数据量
        Integer total = paging.getTotal();
        if (paging.isNeedCount()) {
            SqlBuffer countSql = container.render(countId, map, dialect);
            total = jdbc.queryForObject(countSql, Integer.class);
            paging.setTotal(total);
        }
        // 再查询数据列表
        List<T> list;
        if (total != null && total == 0) {
            list = new ArrayList<T>(); // 已知无数据, 不需要再查询
        } else {
            SqlDialect dialect = jdbc.findSqlDialect();
            // 处理分页
            dialect.processPagingSql(querySql, paging);
            // 查询数据列表
            list = jdbc.query(querySql, rowMapper);
        }
        return new PageList<>(list, total == null ? list.size() : total);
    }

    @Override
    public PageList<Map<String, Object>> pageForMaps(String queryId, String countId, Object params, Paging paging) {
        Map<String, Object> map = params == null ? null : beanToMap(params);
        SqlBuffer querySql = container.render(queryId, map, dialect);

        // 先查询总数据量
        Integer total = paging.getTotal();
        if (paging.isNeedCount()) {
            SqlBuffer countSql = container.render(countId, map, dialect);
            total = jdbc.queryForObject(countSql, Integer.class);
            paging.setTotal(total);
        }
        // 再查询数据列表
        List<Map<String, Object>> list;
        if (total != null && total == 0) {
            list = new ArrayList<Map<String, Object>>(); // 已知无数据, 不需要再查询
        } else {
            SqlDialect dialect = jdbc.findSqlDialect();
            // 处理分页
            dialect.processPagingSql(querySql, paging);
            // 查询数据列表
            list = jdbc.queryForList(querySql);
        }
        return new PageList<>(list, total == null ? list.size() : total);
    }

    @Override
    public int insert(String sqlId, Object params) {
        Map<String, Object> map = params == null ? null : beanToMap(params);
        SqlBuffer sql = container.render(sqlId, map, dialect);
        return jdbc.insert(sql);
    }

    @Override
    public int update(String sqlId, Object params) {
        Map<String, Object> map = params == null ? null : beanToMap(params);
        SqlBuffer sql = container.render(sqlId, map, dialect);
        return jdbc.update(sql);
    }

    @Override
    public int delete(String sqlId, Object params) {
        Map<String, Object> map = params == null ? null : beanToMap(params);
        SqlBuffer sql = container.render(sqlId, map, dialect);
        return jdbc.delete(sql);
    }

    @Override
    public boolean existSqlTemplate(String sqlId) {
        return container.exist(sqlId, dialect.getDbVersion().getDbType());
    }

    @Override
    public SqlBuffer getSqlContent(String sqlId, Object params) {
        Map<String, Object> map = params == null ? null : beanToMap(params);
        return container.render(sqlId, map, dialect);
    }

}
