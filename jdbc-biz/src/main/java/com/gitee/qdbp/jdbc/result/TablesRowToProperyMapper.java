package com.gitee.qdbp.jdbc.result;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import com.gitee.qdbp.jdbc.condition.TableJoin;

/**
 * 多个表关联的结果集保存到JavaBean对应子对象的转换类<br>
 * 对于SYS_USER,SYS_USER_ROLE,SYS_ROLE这样的关联查询<br>
 * 新建一个结果类, 有SysUser user, SysUserRole userRole, SysRole role三个字段(子对象), 分别保存来自三个表的查询结果!<br>
 * 如果查询结果不需要关注SYS_USER_ROLE这个关联表, 也可以建SysUser user, SysRole role两个字段(子对象)的类来保存查询结果<br>
 * 实现思路:<br>
 * TableJoin有参数resultField, 用于指定表数据保存至结果类的哪个字段(子对象)<br>
 * 生成的查询语句的查询字段, 对于重名字段加上表别名作为前缀, 生成列别名, 如U_ID, U_REMARK, UR_ID, UR_REMARK, R_ID, R_REMARK<br>
 * 查询结果根据列别名找到字段名和表别名; 再根据表别名找到resultField, 根据字段名填充数据<br>
 *
 * @author zhaohuihua
 * @version 190617
 */
public class TablesRowToProperyMapper<T> implements RowToBeanMapper<T> {

    private TableJoin tables;
    private Class<T> resultType;
    private ColumnMapRowMapper mapper = new ColumnMapRowMapper();

    public TablesRowToProperyMapper(TableJoin tables, Class<T> resultType) {
        this.tables = tables;
        this.resultType = resultType;
    }

    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        Map<String, Object> map = mapper.mapRow(rs, rowNum);
        throw new UnsupportedOperationException("Not yet completed");
    }

}
