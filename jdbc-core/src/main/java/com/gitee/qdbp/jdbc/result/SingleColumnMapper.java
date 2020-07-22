package com.gitee.qdbp.jdbc.result;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import com.alibaba.fastjson.util.TypeUtils;

/**
 * 获取单列数据
 *
 * @author zhaohuihua
 * @version 20200722
 */
public class SingleColumnMapper<T> implements RowMapper<T> {

    private String columnName;
    private Class<T> clazz;

    public SingleColumnMapper(String columnName, Class<T> clazz) {
        this.columnName = columnName;
        this.clazz = clazz;
    }

    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        Object object = rs.getObject(columnName);
        return TypeUtils.castToJavaBean(object, clazz);
    }
}
