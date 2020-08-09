package com.gitee.qdbp.jdbc.support;

import java.sql.SQLException;
import java.util.Properties;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import com.alibaba.druid.pool.DruidDataSource;
import com.gitee.qdbp.able.exception.ServiceException;
import com.gitee.qdbp.jdbc.api.QdbcBoot;
import com.gitee.qdbp.jdbc.api.SqlBufferJdbcOperations;
import com.gitee.qdbp.jdbc.biz.QdbcBootImpl;
import com.gitee.qdbp.jdbc.biz.SqlBufferJdbcTemplate;
import com.gitee.qdbp.jdbc.exception.DbErrorCode;
import com.gitee.qdbp.jdbc.plugins.DbPluginContainer;

/**
 * QdbcBoot工具类<br>
 * <br>
 * 通过QdbcBootTools提供的是一个默认版本<br>
 * 未提供的特性包括:<br>
 * • TableInfoScans.commonFieldResolver: 不会将公共字段放在查询列表的最后<br>
 * • EntityFieldFillStrategy.entityFillBizResolver: 不会自动填充创建人/创建时间等业务参数<br>
 * • EntityDataStateFillStrategy: 不会自动填充数据状态, 不支持逻辑删除<br>
 * 需要通过DbPluginContainer.defaults()针对具体项目进行定制配置。<br>
 *
 * @author zhaohuihua
 * @version 20200808
 * @see DbPluginContainer#checkAndSetDefaultPorperty(DbPluginContainer)
 */
public class QdbcBootTools {

    /**
     * 构造数据源
     * 
     * @param jdbcUrl 数据库连接地址
     * @param urlKey URL的key
     * @return 数据源
     */
    public static DruidDataSource buildDataSource(String jdbcUrl) {
        AutoDruidDataSource datasource = new AutoDruidDataSource();
        datasource.setConfig(jdbcUrl);
        try {
            datasource.init();
            return datasource;
        } catch (SQLException e) {
            datasource.close();
            throw new ServiceException(DbErrorCode.DB_DATA_SOURCE_INIT_ERROR, e);
        }
    }

    /**
     * 构造数据源
     * 
     * @param properties 配置信息
     * @param urlKey URL的key
     * @return 数据源
     */
    public static DruidDataSource buildDataSource(Properties properties, String urlKey) {
        String jdbcUrl = properties.getProperty(urlKey);
        AutoDruidDataSource datasource = new AutoDruidDataSource();
        datasource.setProperties(properties);
        datasource.setConfig(jdbcUrl);
        try {
            datasource.init();
            return datasource;
        } catch (SQLException e) {
            datasource.close();
            throw new ServiceException(DbErrorCode.DB_DATA_SOURCE_INIT_ERROR, e);
        }
    }

    /**
     * 根据数据库构造QdbcBoot
     * 
     * @param datasource 数据源
     * @return QdbcBoot
     */
    public static QdbcBoot buildByDataSource(DataSource datasource) {
        JdbcTemplate jdbcOperations = new JdbcTemplate(datasource);
        return buildByJdbcOperations(jdbcOperations);
    }

    /**
     * 根据JdbcOperations构造QdbcBoot
     * 
     * @param jdbcOperations JdbcOperations
     * @return QdbcBoot
     */
    public static QdbcBoot buildByJdbcOperations(JdbcOperations jdbcOperations) {
        NamedParameterJdbcOperations operations = new NamedParameterJdbcTemplate(jdbcOperations);
        return buildByJdbcOperations(operations);
    }

    /**
     * 根据JdbcOperations构造QdbcBoot
     * 
     * @param jdbcOperations NamedParameterJdbcOperations
     * @return QdbcBoot
     */
    public static QdbcBoot buildByJdbcOperations(NamedParameterJdbcOperations jdbcOperations) {
        SqlBufferJdbcOperations options = new SqlBufferJdbcTemplate(jdbcOperations);
        return new QdbcBootImpl(options);
    }
}
