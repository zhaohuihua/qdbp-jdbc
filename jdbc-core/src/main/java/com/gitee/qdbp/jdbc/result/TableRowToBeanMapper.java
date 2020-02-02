package com.gitee.qdbp.jdbc.result;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import com.gitee.qdbp.jdbc.model.AllFieldColumn;
import com.gitee.qdbp.jdbc.model.SimpleFieldColumn;
import com.gitee.qdbp.jdbc.plugins.MapToBeanConverter;
import com.gitee.qdbp.jdbc.utils.DbTools;

/**
 * 单表查询结果集行数据到JavaBean的转换处理类<br>
 * ResultSet是列名与字段值的对应关系, 需要转换为字段名与字段值的对应关系
 *
 * @author zhaohuihua
 * @version 190617
 */
public class TableRowToBeanMapper<T> implements RowToBeanMapper<T> {

    private Class<T> resultType;
    private MapToBeanConverter converter;
    private ColumnMapRowMapper mapper = new ColumnMapRowMapper();

    public TableRowToBeanMapper(Class<T> resultType, MapToBeanConverter converter) {
        this.resultType = resultType;
        this.converter = converter;
    }

    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        Map<String, Object> map = mapper.mapRow(rs, rowNum);

        // 1. 获取列名与字段名的对应关系
        AllFieldColumn<SimpleFieldColumn> all = DbTools.parseToAllFieldColumn(resultType);
        if (all == null || all.isEmpty()) {
            return null;
        }

        // 2. ResultSet是列名与字段值的对应关系, 转换为字段名与字段值的对应关系
        Map<String, Object> result = new HashMap<String, Object>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String columnName = entry.getKey();
            SimpleFieldColumn field = all.findByColumnAlias(columnName);
            if (field != null) {
                result.put(field.getFieldName(), entry.getValue());
            }
        }
        // 3. 利用工具类进行Map到JavaBean的转换
        return converter.mapToBean(result, resultType);
    }

}
