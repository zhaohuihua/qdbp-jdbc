package com.gitee.qdbp.jdbc.result;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import com.alibaba.fastjson.util.TypeUtils;

/**
 * 获取第1列数据<br>
 * 注意, 有可能由于分页SQL的写法, 而导致想要的列并不在第1列<br>
 * 最好使用SingleColumnMapper代替, 明确指定列名<br>
 * 例如Oracle的分页(这样写第1列是R_N而不是ID):<pre>
    SELECT * FROM (
        SELECT ROWNUM R_N,T_T.* FROM (
            SELECT ID FROM tableName
        ) T_T WHERE ROWNUM <= endRow
    ) WHERE R_N > beginRow</pre>
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
