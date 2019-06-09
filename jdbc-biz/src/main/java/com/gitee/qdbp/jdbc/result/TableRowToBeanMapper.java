package com.gitee.qdbp.jdbc.result;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import com.alibaba.fastjson.util.TypeUtils;
import com.gitee.qdbp.jdbc.model.AllFieldColumn;
import com.gitee.qdbp.jdbc.model.SimpleFieldColumn;
import com.gitee.qdbp.jdbc.utils.DbTools;

public class TableRowToBeanMapper<T> implements RowToBeanMapper<T> {

    private Class<T> resultType;
    private ColumnMapRowMapper mapper = new ColumnMapRowMapper();

    public TableRowToBeanMapper(Class<T> resultType) {
        this.resultType = resultType;
    }

    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        Map<String, Object> result = mapper.mapRow(rs, rowNum);

        // 1. 获取列名与字段名的对应关系
        AllFieldColumn<SimpleFieldColumn> all = DbTools.parseToAllFieldColumn(resultType);
        if (all == null || all.isEmpty()) {
            return null;
        }

        // 2. properties是列名与字段值的对应关系, 转换为字段名与字段值的对应关系
        Map<String, Object> fieldValues = new HashMap<String, Object>();
        for (Map.Entry<String, Object> entry : result.entrySet()) {
            String columnName = entry.getKey();
            SimpleFieldColumn field = all.findByColumn(columnName);
            if (field != null) {
                fieldValues.put(field.getFieldName(), entry.getValue());
            }
        }
        // 3. 利用fastjson工具进行Map到JavaObject的转换
        return TypeUtils.castToJavaBean(result, resultType);

    }

}
