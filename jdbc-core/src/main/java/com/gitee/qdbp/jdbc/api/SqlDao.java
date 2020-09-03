package com.gitee.qdbp.jdbc.api;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.RowMapper;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;

/**
 * 执行SQL语句的处理接口<br>
 * SQL语句配置在SQL模板文件中, 系统启动时预加载到缓存, 使用时通过sqlId调用
 *
 * @author zhaohuihua
 * @version 20200903
 */
public interface SqlDao {

    /**
     * 查询对象
     * 
     * @param <T> 对象类型
     * @param sqlId SqlId
     * @param resultType 结果类型
     * @return 查询结果对象
     */
    <T> T findForObject(String sqlId, Class<T> resultType);

    /**
     * 查询对象
     * 
     * @param <T> 对象类型
     * @param sqlId SqlId
     * @param params 查询参数
     * @param resultType 结果类型
     * @return 查询结果对象
     */
    <T> T findForObject(String sqlId, Object params, Class<T> resultType);

    /**
     * 查询对象
     * 
     * @param <T> 对象类型
     * @param sqlId SqlId
     * @param rowMapper 结果转换类
     * @return 查询结果对象
     */
    <T> T findForObject(String sqlId, RowMapper<T> rowMapper);

    /**
     * 查询对象
     * 
     * @param <T> 对象类型
     * @param sqlId SqlId
     * @param params 查询参数
     * @param rowMapper 结果转换类
     * @return 查询结果对象
     */
    <T> T findForObject(String sqlId, Object params, RowMapper<T> rowMapper);

    /**
     * 查询数据, 结果为Map结构
     * 
     * @param sqlId SqlId
     * @return Map结果
     */
    Map<String, Object> findForMap(String sqlId);

    /**
     * 查询数据, 结果为Map结构
     * 
     * @param sqlId SqlId
     * @param params 查询参数
     * @return Map结果
     */
    Map<String, Object> findForMap(String sqlId, Object params);

    /**
     * 查询列表
     * 
     * @param <T> 对象类型
     * @param sqlId SqlId
     * @param resultType 结果类型
     * @return 查询结果列表
     */
    <T> List<T> listForObjects(String sqlId, Class<T> resultType);

    /**
     * 查询列表
     * 
     * @param <T> 对象类型
     * @param sqlId SqlId
     * @param params 查询参数
     * @param resultType 结果类型
     * @return 查询结果列表
     */
    <T> List<T> listForObjects(String sqlId, Object params, Class<T> resultType);

    /**
     * 查询列表
     * 
     * @param <T> 对象类型
     * @param sqlId SqlId
     * @param rowMapper 结果转换类
     * @return 查询结果列表
     */
    <T> List<T> listForObjects(String sqlId, RowMapper<T> rowMapper);

    /**
     * 查询列表
     * 
     * @param <T> 对象类型
     * @param sqlId SqlId
     * @param params 查询参数
     * @param rowMapper 结果转换类
     * @return 查询结果列表
     */
    <T> List<T> listForObjects(String sqlId, Object params, RowMapper<T> rowMapper);

    /**
     * 查询数据, 结果为Map列表
     * 
     * @param sqlId SqlId
     * @return Map列表
     */
    List<Map<String, Object>> listForMaps(String sqlId);

    /**
     * 查询数据, 结果为Map列表
     * 
     * @param sqlId SqlId
     * @param params 查询参数
     * @return Map列表
     */
    List<Map<String, Object>> listForMaps(String sqlId, Object params);

    /**
     * 执行插入语句
     * 
     * @param sqlId SqlId
     * @return 影响行数
     */
    int insert(String sqlId);

    /**
     * 执行插入语句
     * 
     * @param sqlId SqlId
     * @param params Sql参数
     * @return 影响行数
     */
    int insert(String sqlId, Object params);

    /**
     * 执行更新语句
     * 
     * @param sqlId SqlId
     * @return 影响行数
     */
    int update(String sqlId);

    /**
     * 执行更新语句
     * 
     * @param sqlId SqlId
     * @param params Sql参数
     * @return 影响行数
     */
    int update(String sqlId, Object params);

    /**
     * 执行删除语句
     * 
     * @param sqlId SqlId
     * @return 影响行数
     */
    int delete(String sqlId);

    /**
     * 执行删除语句
     * 
     * @param sqlId SqlId
     * @param params Sql参数
     * @return 影响行数
     */
    int delete(String sqlId, Object params);

    /**
     * 获取SQL内容
     * 
     * @param sqlId SqlId
     * @return SQL内容
     */
    SqlBuffer getSqlContent(String sqlId);

    /**
     * 获取SQL内容
     * 
     * @param sqlId SqlId
     * @param params Sql参数
     * @return SQL内容
     */
    SqlBuffer getSqlContent(String sqlId, Object params);
}
