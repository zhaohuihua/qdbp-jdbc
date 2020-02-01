package com.gitee.qdbp.jdbc.result;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import com.gitee.qdbp.able.jdbc.condition.TableJoin;
import com.gitee.qdbp.able.jdbc.condition.TableJoin.TableItem;
import com.gitee.qdbp.jdbc.model.AllFieldColumn;
import com.gitee.qdbp.jdbc.model.TablesFieldColumn;
import com.gitee.qdbp.jdbc.utils.DbTools;
import com.gitee.qdbp.jdbc.utils.ParseTools;
import com.gitee.qdbp.tools.utils.VerifyTools;

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

        // 1. 获取列名与字段名的对应关系
        AllFieldColumn<TablesFieldColumn> all = DbTools.parseToAllFieldColumn(tables);
        if (all == null || all.isEmpty()) {
            return null;
        }

        Map<String, Object> result = new HashMap<>(); // 结果容器
        Map<String, Map<String, Object>> subs = new HashMap<>(); // 子对象容器
        // 2. 根据TableJoin的resultField生成子Map对象
        String majorField = tables.getMajor().getResultField();
        if (VerifyTools.isNotBlank(majorField) && !majorField.equals("this")) {
            subs.put(majorField, new HashMap<String, Object>());
        }
        for (TableItem item : tables.getJoins()) {
            String itemField = item.getResultField();
            if (VerifyTools.isNotBlank(itemField) && !itemField.equals("this")) {
                subs.put(itemField, new HashMap<String, Object>());
            }
        }

        // 3. 根据列别名查找字段信息, 再找到resultField, 根据字段名填充数据
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            // 根据列别名查找字段信息
            String columnAlias = entry.getKey();
            TablesFieldColumn field = all.findByColumnAlias(columnAlias);
            if (field == null) {
                continue; // 正常情况下能查到结果集不可能找不到字段信息
            }
            // 获取resultField
            String resultField = field.getResultField();
            if (VerifyTools.isBlank(resultField)) {
                continue; // 如果没有指定resultField, 说明不需要保存结果
            }
            // 将字段名和字段值根据resultField填充至对应的子对象中
            // 如果resultField=this直接填充到主容器, 否则填充到指定的子容器
            if (resultField.equals("this")) {
                result.put(field.getFieldName(), entry.getValue());
            } else {
                subs.get(resultField).put(field.getFieldName(), entry.getValue());
            }
        }
        if (!subs.isEmpty()) {
            result.putAll(subs);
        }
        // 4. 利用fastjson工具进行Map到JavaBean的转换
        return ParseTools.mapToBean(result, resultType);
    }

}
