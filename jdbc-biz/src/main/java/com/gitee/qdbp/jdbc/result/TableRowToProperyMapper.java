package com.gitee.qdbp.jdbc.result;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.RowMapper;
import com.gitee.qdbp.jdbc.condition.TableJoin;

public class TableRowToProperyMapper<T> implements RowToBeanMapper<T> {

    private TableJoin tables;
    private Class<T> resultType;
    private ColumnMapRowMapper mapper = new ColumnMapRowMapper();

    public TableRowToProperyMapper(TableJoin tables, Class<T> resultType) {
        this.tables = tables;
        this.resultType = resultType;
    }

    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        Map<String, Object> map = mapper.mapRow(rs, rowNum);

    }

}
