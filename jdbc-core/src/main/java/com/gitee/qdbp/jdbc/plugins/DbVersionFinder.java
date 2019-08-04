package com.gitee.qdbp.jdbc.plugins;

import javax.sql.DataSource;
import com.gitee.qdbp.jdbc.model.DbVersion;

/**
 * 数据库信息查询接口
 *
 * @author zhaohuihua
 * @version 190606
 */
public interface DbVersionFinder {

    DbVersion findDbVersion(DataSource datasource);

}
