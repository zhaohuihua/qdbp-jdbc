package com.gitee.qdbp.jdbc.plugins.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.gitee.qdbp.able.jdbc.condition.DbUpdate;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.able.jdbc.model.PkEntity;
import com.gitee.qdbp.jdbc.api.SqlBufferJdbcOperations;
import com.gitee.qdbp.jdbc.model.DbVersion;
import com.gitee.qdbp.jdbc.model.SimpleFieldColumn;
import com.gitee.qdbp.jdbc.plugins.BatchInsertExecutor;
import com.gitee.qdbp.jdbc.plugins.BatchUpdateExecutor;
import com.gitee.qdbp.jdbc.plugins.DbConditionConverter;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.build.CrudSqlBuilder;
import com.gitee.qdbp.jdbc.utils.DbTools;

/**
 * 逐一循环执行的批量处理接口实现类<br>
 * 所有数据库都支持, 但性能最差, 作为没有找到更优方案时的补救措施
 *
 * @author zhaohuihua
 * @version 20200706
 */
public class BatchOperateByForEachExecutor implements BatchInsertExecutor, BatchUpdateExecutor {

    @Override
    public boolean supports(DbVersion version) {
        return true; // 所有数据库都支持
    }

    @Override
    public List<String> inserts(List<PkEntity> entities, SqlBufferJdbcOperations jdbc, CrudSqlBuilder sqlBuilder) {
        List<String> ids = new ArrayList<>();
        for (PkEntity pe : entities) {
            SqlBuffer sb = sqlBuilder.buildInsertSql(pe.getEntity());
            jdbc.insert(sb);
            ids.add(pe.getPrimaryKey());
        }
        return ids;
    }

    @Override
    public int updates(List<PkEntity> entities, SqlBufferJdbcOperations jdbc,
            CrudSqlBuilder sqlBuilder) {
        // 查找主键(批量更新必须要有主键)
        SimpleFieldColumn pk = sqlBuilder.helper().getPrimaryKey();
        DbConditionConverter converter = DbTools.getDbConditionConverter();
        int rows = 0;
        for (PkEntity item : entities) {
            String pkValue = item.getPrimaryKey();
            Map<String, Object> entity = item.getEntity();
            // 生成主键过滤条件
            DbWhere where = new DbWhere();
            where.on(pk.getFieldName(), "=", pkValue);
            // entity转换为DbUpdate
            DbUpdate ud = converter.parseMapToDbUpdate(entity);
            // 生成SQL
            SqlBuffer sql = sqlBuilder.buildUpdateSql(ud, where);
            rows += jdbc.update(sql);
        }
        return rows;
    }

}
