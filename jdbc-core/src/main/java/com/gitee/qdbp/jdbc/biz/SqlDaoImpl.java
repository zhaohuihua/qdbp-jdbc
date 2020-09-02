package com.gitee.qdbp.jdbc.biz;

import java.util.List;
import java.util.Map;
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

public class SqlDaoImpl implements SqlDao {

    protected SqlFragmentContainer container;
    protected SqlBufferJdbcOperations jdbc;
    protected SqlDialect dialect;

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
    public <T> T find(String sqlId, Class<T> resultType) {
        return find(sqlId, null, resultType);
    }

    @Override
    public <T> T find(String sqlId, Object params, Class<T> resultType) {
        Map<String, Object> map = params == null ? null : beanToMap(params);
        SqlBuffer buffer = container.render(sqlId, map, dialect);
        return jdbc.queryForObject(buffer, resultType);
    }

    @Override
    public <T> List<T> list(String sqlId, Class<T> resultType) {
        return list(sqlId, null, resultType);
    }

    @Override
    public <T> List<T> list(String sqlId, Object params, Class<T> resultType) {
        Map<String, Object> map = params == null ? null : beanToMap(params);
        SqlBuffer buffer = container.render(sqlId, map, dialect);
        return jdbc.queryForList(buffer, resultType);
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

}
