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
    public <T> T findForObject(String sqlId, Class<T> resultType) {
        return findForObject(sqlId, null, resultType);
    }

    @Override
    public <T> T findForObject(String sqlId, Object params, Class<T> resultType) {
        Map<String, Object> map = params == null ? null : beanToMap(params);
        SqlBuffer buffer = container.render(sqlId, map, dialect);
        return jdbc.queryForObject(buffer, resultType);
    }

    @Override
    public <T> T findForObject(String sqlId, RowMapper<T> rowMapper) {
        return findForObject(sqlId, null, rowMapper);
    }

    @Override
    public <T> T findForObject(String sqlId, Object params, RowMapper<T> rowMapper) {
        Map<String, Object> map = params == null ? null : beanToMap(params);
        SqlBuffer buffer = container.render(sqlId, map, dialect);
        return jdbc.queryForObject(buffer, rowMapper);
    }

    @Override
    public Map<String, Object> findForMap(String sqlId) {
        return findForMap(sqlId, null);
    }

    @Override
    public Map<String, Object> findForMap(String sqlId, Object params) {
        Map<String, Object> map = params == null ? null : beanToMap(params);
        SqlBuffer buffer = container.render(sqlId, map, dialect);
        return jdbc.queryForMap(buffer);
    }

    @Override
    public <T> List<T> listForObjects(String sqlId, Class<T> resultType) {
        return listForObjects(sqlId, null, resultType);
    }

    @Override
    public <T> List<T> listForObjects(String sqlId, Object params, Class<T> resultType) {
        Map<String, Object> map = params == null ? null : beanToMap(params);
        SqlBuffer buffer = container.render(sqlId, map, dialect);
        return jdbc.queryForList(buffer, resultType);
    }

    @Override
    public <T> List<T> listForObjects(String sqlId, RowMapper<T> rowMapper) {
        return listForObjects(sqlId, null, rowMapper);
    }

    @Override
    public <T> List<T> listForObjects(String sqlId, Object params, RowMapper<T> rowMapper) {
        Map<String, Object> map = params == null ? null : beanToMap(params);
        SqlBuffer buffer = container.render(sqlId, map, dialect);
        return jdbc.query(buffer, rowMapper);
    }

    @Override
    public List<Map<String, Object>> listForMaps(String sqlId) {
        return listForMaps(sqlId, (Object) null);
    }

    @Override
    public List<Map<String, Object>> listForMaps(String sqlId, Object params) {
        Map<String, Object> map = params == null ? null : beanToMap(params);
        SqlBuffer buffer = container.render(sqlId, map, dialect);
        return jdbc.queryForList(buffer);
    }

    @Override
    public <T> PageList<T> pageForObjects(String sqlId, Paging paging, Class<T> resultType) {
        return pageForObjects(sqlId, null, paging, resultType);
    }

    @Override
    public <T> PageList<T> pageForObjects(String sqlId, Object params, Paging paging, Class<T> resultType) {
        Map<String, Object> map = params == null ? null : beanToMap(params);
        SqlBuffer buffer = container.render(sqlId, map, dialect);

        // 先查询总数据量
        Integer total = paging.getTotal();
        if (paging.isNeedCount()) {
            total = jdbc.countByQuerySql(buffer);
            paging.setTotal(total);
        }
        // 再查询数据列表
        List<T> list;
        if (total != null && total == 0) {
            list = new ArrayList<T>(); // 已知无数据, 不需要再查询
        } else {
            SqlDialect dialect = jdbc.findSqlDialect();
            // 处理分页
            dialect.processPagingSql(buffer, paging);
            // 查询数据列表
            list = jdbc.queryForList(buffer, resultType);
        }
        return new PageList<>(list, total == null ? list.size() : total);
    }

    @Override
    public <T> PageList<T> pageForObjects(String sqlId, Paging paging, RowMapper<T> rowMapper) {
        return pageForObjects(sqlId, null, paging, rowMapper);
    }

    @Override
    public <T> PageList<T> pageForObjects(String sqlId, Object params, Paging paging, RowMapper<T> rowMapper) {
        Map<String, Object> map = params == null ? null : beanToMap(params);
        SqlBuffer buffer = container.render(sqlId, map, dialect);

        // 先查询总数据量
        Integer total = paging.getTotal();
        if (paging.isNeedCount()) {
            total = jdbc.countByQuerySql(buffer);
            paging.setTotal(total);
        }
        // 再查询数据列表
        List<T> list;
        if (total != null && total == 0) {
            list = new ArrayList<T>(); // 已知无数据, 不需要再查询
        } else {
            SqlDialect dialect = jdbc.findSqlDialect();
            // 处理分页
            dialect.processPagingSql(buffer, paging);
            // 查询数据列表
            list = jdbc.query(buffer, rowMapper);
        }
        return new PageList<>(list, total == null ? list.size() : total);
    }

    @Override
    public PageList<Map<String, Object>> pageForMaps(String sqlId, Paging paging) {
        return pageForMaps(sqlId, null, paging);
    }

    @Override
    public PageList<Map<String, Object>> pageForMaps(String sqlId, Object params, Paging paging) {
        Map<String, Object> map = params == null ? null : beanToMap(params);
        SqlBuffer buffer = container.render(sqlId, map, dialect);

        // 先查询总数据量
        Integer total = paging.getTotal();
        if (paging.isNeedCount()) {
            total = jdbc.countByQuerySql(buffer);
            paging.setTotal(total);
        }
        // 再查询数据列表
        List<Map<String, Object>> list;
        if (total != null && total == 0) {
            list = new ArrayList<Map<String, Object>>(); // 已知无数据, 不需要再查询
        } else {
            SqlDialect dialect = jdbc.findSqlDialect();
            // 处理分页
            dialect.processPagingSql(buffer, paging);
            // 查询数据列表
            list = jdbc.queryForList(buffer);
        }
        return new PageList<>(list, total == null ? list.size() : total);
    }

    @Override
    public int insert(String sqlId) {
        return insert(sqlId, null);
    }

    @Override
    public int insert(String sqlId, Object params) {
        Map<String, Object> map = params == null ? null : beanToMap(params);
        SqlBuffer buffer = container.render(sqlId, map, dialect);
        return jdbc.insert(buffer);
    }

    @Override
    public int update(String sqlId) {
        return update(sqlId, null);
    }

    @Override
    public int update(String sqlId, Object params) {
        Map<String, Object> map = params == null ? null : beanToMap(params);
        SqlBuffer buffer = container.render(sqlId, map, dialect);
        return jdbc.update(buffer);
    }

    @Override
    public int delete(String sqlId) {
        return delete(sqlId, null);
    }

    @Override
    public int delete(String sqlId, Object params) {
        Map<String, Object> map = params == null ? null : beanToMap(params);
        SqlBuffer buffer = container.render(sqlId, map, dialect);
        return jdbc.delete(buffer);
    }

    @Override
    public SqlBuffer getSqlContent(String sqlId) {
        return getSqlContent(sqlId, null);
    }

    @Override
    public SqlBuffer getSqlContent(String sqlId, Object params) {
        Map<String, Object> map = params == null ? null : beanToMap(params);
        return container.render(sqlId, map, dialect);
    }

}
