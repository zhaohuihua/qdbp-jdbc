package com.gitee.qdbp.jdbc.biz;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.gitee.qdbp.able.exception.ServiceException;
import com.gitee.qdbp.able.model.ordering.Ordering;
import com.gitee.qdbp.jdbc.api.EasyCrudDao;
import com.gitee.qdbp.jdbc.api.SqlBufferJdbcOperations;
import com.gitee.qdbp.jdbc.condition.DbField;
import com.gitee.qdbp.jdbc.condition.DbUpdate;
import com.gitee.qdbp.jdbc.condition.DbWhere;
import com.gitee.qdbp.jdbc.exception.DbErrorCode;
import com.gitee.qdbp.jdbc.model.PrimaryKey;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.result.FirstColumnMapper;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.build.CrudSqlBuilder;
import com.gitee.qdbp.jdbc.sql.fragment.CrudFragmentHelper;
import com.gitee.qdbp.jdbc.utils.DbTools;
import com.gitee.qdbp.tools.utils.ConvertTools;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 基础增删改查数据库操作
 *
 * @author 赵卉华
 * @version 190601
 */
public class EasyCrudDaoImpl<T> extends EasyTableQueryImpl<T> implements EasyCrudDao<T> {

    private static Logger log = LoggerFactory.getLogger(EasyCrudDaoImpl.class);

    private Class<T> clazz;

    EasyCrudDaoImpl(Class<T> clazz, SqlBufferJdbcOperations jdbcOperations) {
        super(clazz, DbTools.getCrudSqlBuilder(clazz), DbTools.getModelDataExecutor(clazz), jdbcOperations);
        this.clazz = clazz;
    }

    private CrudSqlBuilder builder() {
        return (CrudSqlBuilder) this.sqlBuilder;
    }

    @Override
    public T findById(String id) {
        VerifyTools.requireNotBlank(id, "id");
        PrimaryKey pk = builder().helper().getPrimaryKey();
        if (pk == null) {
            throw new UnsupportedOperationException("PrimaryKeyInfoNotFound, UnsupportedFindById, class=" + clazz);
        }
        String primaryField = pk.getFieldName();
        DbWhere where = new DbWhere();
        where.on(primaryField, "=", id);
        return this.find(where);
    }

    @Override
    public List<T> listChildren(String startCode, String codeField, String parentField, DbWhere where,
            List<Ordering> orderings) {
        DbWhere readyWhere = checkWhere(where);
        modelDataExecutor.fillTableWhereDataStatus(readyWhere);
        List<String> startCodes = ConvertTools.toList(startCode);
        return doListChildren(startCodes, codeField, parentField, readyWhere, orderings);
    }

    @Override
    public List<T> listChildren(List<String> startCodes, String codeField, String parentField, DbWhere where,
            List<Ordering> orderings) {
        DbWhere readyWhere = checkWhere(where);
        modelDataExecutor.fillTableWhereDataStatus(readyWhere);
        return doListChildren(startCodes, codeField, parentField, readyWhere, orderings);
    }

    private List<T> doListChildren(List<String> startCodes, String codeField, String parentField, DbWhere where,
            List<Ordering> orderings) throws ServiceException {
        CrudFragmentHelper sqlHelper = builder().helper();
        List<String> selectFields = sqlHelper.getFieldNames();
        SqlDialect dialect = DbTools.getSqlDialect();
        SqlBuffer buffer = dialect.buildFindChildrenSql(startCodes, codeField, parentField, selectFields, where,
            orderings, sqlHelper);
        return jdbc.queryForList(buffer, resultType);
    }

    @Override
    public List<String> listChildrenCodes(String startCode, String codeField, String parentField, DbWhere where,
            List<Ordering> orderings) {
        DbWhere readyWhere = checkWhere(where);
        modelDataExecutor.fillTableWhereDataStatus(readyWhere);
        List<String> startCodes = ConvertTools.toList(startCode);
        return doListChildrenCodes(startCodes, codeField, parentField, readyWhere, orderings);
    }

    @Override
    public List<String> listChildrenCodes(List<String> startCodes, String codeField, String parentField, DbWhere where,
            List<Ordering> orderings) {
        DbWhere readyWhere = checkWhere(where);
        modelDataExecutor.fillTableWhereDataStatus(readyWhere);
        return doListChildrenCodes(startCodes, codeField, parentField, readyWhere, orderings);
    }

    // ORACLE： START WITH {codeField} IN( {startCode} ) CONNECT BY PRIOR {codeField} = {parentField}
    // DB2: 使用WITH递归 
    // MYSQL 8.0+: 使用WITH RECURSIVE递归 
    // MYSQL 8.0-: 使用存储过程RECURSIVE_FIND_CHILDREN
    private List<String> doListChildrenCodes(List<String> startCodes, String codeField, String parentField,
            DbWhere where, List<Ordering> orderings) throws ServiceException {
        CrudFragmentHelper sqlHelper = builder().helper();
        Set<String> selectFields = ConvertTools.toSet(codeField);
        SqlDialect dialect = DbTools.getSqlDialect();
        SqlBuffer buffer = dialect.buildFindChildrenSql(startCodes, codeField, parentField, selectFields, where,
            orderings, sqlHelper);
        return jdbc.query(buffer, FIRST_COLUMN_STRING_MAPPER);
    }

    private static FirstColumnMapper<String> FIRST_COLUMN_STRING_MAPPER = new FirstColumnMapper<>(String.class);

    @Override
    public String insert(T entity, boolean fillCreateParams) throws ServiceException {
        VerifyTools.requireNotBlank(entity, "entity");
        Map<String, Object> readyEntity = DbTools.beanToMap(entity);
        return insert(readyEntity, fillCreateParams);
    }

    @Override
    public String insert(Map<String, Object> entity, boolean fillCreateParams) throws ServiceException {
        VerifyTools.requireNotBlank(entity, "entity");

        String tableName = builder().helper().getTableName();
        String id = null;
        // 查找主键
        PrimaryKey pk = builder().helper().getPrimaryKey();
        if (pk == null) {
            log.debug("PrimaryKeyInfoNotFound, class={}", clazz);
        } else if (VerifyTools.isNotBlank(entity.get(pk.getFieldName()))) {
            id = entity.get(pk.getFieldName()).toString();
        } else {
            // 生成主键
            entity.put(pk.getFieldName(), id = modelDataExecutor.generatePrimaryKeyCode(tableName));
        }
        modelDataExecutor.fillTableCreateDataStatus(entity);
        if (fillCreateParams) {
            modelDataExecutor.fillTableCreteParams(entity);
        }

        SqlBuffer buffer = builder().buildInsertSql(entity);

        jdbc.update(buffer);
        return id;
    }

    @Override
    public int update(T entity, boolean fillUpdateParams, boolean errorOnUnaffected) throws ServiceException {
        // 查找主键
        PrimaryKey pk = builder().helper().getPrimaryKey();
        if (pk == null) {
            throw new UnsupportedOperationException("PrimaryKeyInfoNotFound, UnsupportedUpdateById, class=" + clazz);
        }
        DbUpdate readyEntity = DbTools.parseUpdateFromEntity(entity);
        DbWhere where = new DbWhere();
        List<DbField> temp = readyEntity.fields(pk.getFieldName());
        String id = null;
        if (temp != null && !temp.isEmpty()) {
            Object fieldValue = temp.get(0).getFieldValue();
            id = fieldValue == null ? null : fieldValue.toString();
        }
        // 主键不能为空
        if (VerifyTools.isBlank(id)) {
            log.warn("PrimaryKeyValueIsBlank, CanNotExecuteUpdateById, class={}", clazz);
            throw new ServiceException(DbErrorCode.DB_PRIMARY_KEY_VALUE_IS_REQUIRED);
        }
        // 将主键从更新数据中移到查询条件中
        where.on(pk.getFieldName(), "=", id);
        readyEntity.remove(pk.getFieldName());

        VerifyTools.requireNotBlank(readyEntity, "readyEntity");

        modelDataExecutor.fillTableWhereDataStatus(where);
        if (fillUpdateParams) {
            modelDataExecutor.fillTableUpdateParams(readyEntity);
        }
        return this.doUpdate(readyEntity, where, errorOnUnaffected);
    }

    @Override
    public int update(T entity, DbWhere where, boolean fillUpdateParams, boolean errorOnUnaffected)
            throws ServiceException {
        DbUpdate readyEntity = DbTools.parseUpdateFromEntity(entity);
        if (readyEntity == null || readyEntity.isEmpty()) {
            throw new IllegalArgumentException("entity can't be empty");
        }
        DbWhere readyWhere = checkWhere(where);

        modelDataExecutor.fillTableWhereDataStatus(readyWhere);
        if (fillUpdateParams) {
            modelDataExecutor.fillTableUpdateParams(readyEntity);
        }
        return this.doUpdate(readyEntity, readyWhere, errorOnUnaffected);
    }

    @Override
    public int update(DbUpdate entity, DbWhere where, boolean fillUpdateParams, boolean errorOnUnaffected)
            throws ServiceException {
        if (entity == null || entity.isEmpty()) {
            throw new IllegalArgumentException("entity can't be empty");
        }
        DbWhere readyWhere = checkWhere(where);
        modelDataExecutor.fillTableWhereDataStatus(readyWhere);
        if (fillUpdateParams) {
            modelDataExecutor.fillTableUpdateParams(entity);
        }
        return this.doUpdate(entity, readyWhere, errorOnUnaffected);
    }

    private int doUpdate(DbUpdate readyEntity, DbWhere readyWhere, boolean errorOnUnaffected) throws ServiceException {
        SqlBuffer buffer = builder().buildUpdateSql(readyEntity, readyWhere);

        int rows = jdbc.update(buffer);

        if (rows == 0) {
            String desc = "Failed to update, affected rows is 0. ";
            if (errorOnUnaffected) {
                if (log.isErrorEnabled()) {
                    log.error(desc);
                }
                throw new ServiceException(DbErrorCode.DB_AFFECTED_ROWS_IS_ZERO);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(desc);
                }
            }
        }
        return rows;
    }

    @Override
    public int logicalDeleteByIds(List<String> ids, boolean fillUpdateParams, boolean errorOnUnaffected)
            throws ServiceException {
        VerifyTools.requireNotBlank(ids, "ids");
        PrimaryKey pk = builder().helper().getPrimaryKey();
        if (pk == null) {
            throw new UnsupportedOperationException("PrimaryKeyInfoNotFound, UnsupportedDeleteById, class=" + clazz);
        }

        String primaryField = pk.getFieldName();
        DbWhere where = new DbWhere();
        where.on(primaryField, "in", ids);
        modelDataExecutor.fillTableWhereDataStatus(where);
        return this.doDelete(where, true, fillUpdateParams, errorOnUnaffected);
    }

    @Override
    public int logicalDelete(T where, boolean fillUpdateParams, boolean errorOnUnaffected) throws ServiceException {
        DbWhere readyWhere = DbTools.parseWhereFromEntity(where);
        if (VerifyTools.isBlank(readyWhere)) {
            throw new IllegalArgumentException("where can't be empty, please use logicalDeleteAll()");
        }
        modelDataExecutor.fillTableWhereDataStatus(readyWhere);
        return this.doDelete(readyWhere, true, fillUpdateParams, errorOnUnaffected);
    }

    @Override
    public int logicalDelete(DbWhere where, boolean fillUpdateParams, boolean errorOnUnaffected)
            throws ServiceException {
        DbWhere readyWhere = checkWhere(where);
        modelDataExecutor.fillTableWhereDataStatus(readyWhere);
        return this.doDelete(readyWhere, true, fillUpdateParams, errorOnUnaffected);
    }

    @Override
    public int physicalDeleteByIds(List<String> ids, boolean errorOnUnaffected) throws ServiceException {
        VerifyTools.requireNotBlank(ids, "ids");
        PrimaryKey pk = builder().helper().getPrimaryKey();
        if (pk == null) {
            throw new UnsupportedOperationException("PrimaryKeyInfoNotFound, UnsupportedDeleteById, class=" + clazz);
        }

        String primaryField = pk.getFieldName();
        DbWhere where = new DbWhere();
        where.on(primaryField, "in", ids);
        return this.doDelete(where, true, false, errorOnUnaffected);
    }

    @Override
    public int physicalDelete(T where, boolean errorOnUnaffected) throws ServiceException {
        DbWhere readyWhere = DbTools.parseWhereFromEntity(where);
        if (VerifyTools.isBlank(readyWhere)) {
            throw new IllegalArgumentException("where can't be empty, please use physicalDeleteAll()");
        }
        return this.doDelete(readyWhere, true, false, errorOnUnaffected);
    }

    @Override
    public int physicalDelete(DbWhere where, boolean errorOnUnaffected) throws ServiceException {
        return this.doDelete(where, true, false, errorOnUnaffected);
    }

    private int doDelete(DbWhere readyWhere, boolean logical, boolean fillUpdateParams, boolean errorOnUnaffected)
            throws ServiceException {
        SqlBuffer buffer;
        if (logical) {
            if (modelDataExecutor.supportedTableLogicalDelete()) { // 支持逻辑删除
                DbUpdate ud = new DbUpdate();
                modelDataExecutor.fillTableLogicalDeleteDataStatus(ud);
                if (fillUpdateParams) {
                    modelDataExecutor.fillTableUpdateParams(ud);
                }
                buffer = builder().buildUpdateSql(ud, readyWhere);
            } else { // 不支持逻辑删除
                throw new ServiceException(DbErrorCode.UNSUPPORTED_LOGICAL_DELETE);
            }
        } else {
            buffer = builder().buildDeleteSql(readyWhere);
        }

        int rows = jdbc.update(buffer);

        if (rows == 0) {
            String desc = "Failed to delete, affected rows is 0. ";
            if (errorOnUnaffected) {
                if (log.isErrorEnabled()) {
                    log.error(desc);
                }
                throw new ServiceException(DbErrorCode.DB_AFFECTED_ROWS_IS_ZERO);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(desc);
                }
            }
        }
        return rows;
    }

}
