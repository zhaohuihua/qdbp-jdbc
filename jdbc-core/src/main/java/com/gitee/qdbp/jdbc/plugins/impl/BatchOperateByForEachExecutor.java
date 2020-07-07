package com.gitee.qdbp.jdbc.plugins.impl;

import java.util.ArrayList;
import java.util.List;
import com.gitee.qdbp.able.jdbc.condition.DbUpdate;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.able.jdbc.model.PkEntity;
import com.gitee.qdbp.able.jdbc.model.PkUpdate;
import com.gitee.qdbp.jdbc.api.SqlBufferJdbcOperations;
import com.gitee.qdbp.jdbc.model.DbVersion;
import com.gitee.qdbp.jdbc.model.PrimaryKeyFieldColumn;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.build.CrudSqlBuilder;

/**
 * 逐一循环执行的批量处理接口实现类<br>
 * 所有数据库都支持, 但性能最差, 作为没有找到更优方案时的补救措施
 *
 * @author zhaohuihua
 * @version 20200706
 */
public class BatchOperateByForEachExecutor extends BaseBatchOperateExecutor {

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
    public int updates(List<PkUpdate> contents, DbWhere commonWhere, SqlBufferJdbcOperations jdbc,
            CrudSqlBuilder sqlBuilder) {
        // 查找主键(批量更新必须要有主键)
        PrimaryKeyFieldColumn pk = sqlBuilder.helper().getPrimaryKey();
        int rows = 0;
        for (PkUpdate pkud : contents) {
            String pkValue = pkud.getPrimaryKey();
            DbUpdate entity = pkud.getUpdate();
            // 从公共条件中复制过滤条件
            DbWhere where = commonWhere.copy();
            // 将主键加入到过滤条件中
            where.on(pk.getFieldName(), "=", pkValue);
            // 生成SQL
            SqlBuffer sql = sqlBuilder.buildUpdateSql(entity, where);
            rows += jdbc.update(sql);
        }
        return rows;
    }

}
