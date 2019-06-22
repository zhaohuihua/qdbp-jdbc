package com.gitee.qdbp.jdbc.result;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import com.gitee.qdbp.able.beans.KeyValue;

/**
 * KeyIntegerMapper<br>
 * 第1列是KEY, 第2列是Integer的结果集, 转换为KeyValue&lt;Integer&gt;
 *
 * @author zhaohuihua
 * @version 190607
 */
public class KeyIntegerMapper implements RowMapper<KeyValue<Integer>> {

    @Override
    public KeyValue<Integer> mapRow(ResultSet rs, int rowNum) throws SQLException {
        KeyValue<Integer> item = new KeyValue<>();
        item.setKey(rs.getString(1));
        item.setValue(rs.getInt(2));
        return item;
    }
}
