package com.gitee.qdbp.jdbc.result;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import com.alibaba.fastjson.util.TypeUtils;

/**
 * 获取第1列数据
 *
 * @author zhaohuihua
 * @version 190601
 */
public class FirstColumnMapper<T> implements RowMapper<T> {

    private Class<T> clazz;

    public FirstColumnMapper(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        // DruidPooledResultSet.getObject() SQLFeatureNotSupportedException
        // return rs.getObject(1, clazz);
        Object object = rs.getObject(1);
        return TypeUtils.castToJavaBean(object, clazz);
    }
}
