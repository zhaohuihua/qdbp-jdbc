package com.gitee.qdbp.jdbc.biz;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.gitee.qdbp.able.exception.ServiceException;
import com.gitee.qdbp.able.jdbc.base.DbCondition;
import com.gitee.qdbp.able.jdbc.condition.DbField;
import com.gitee.qdbp.able.jdbc.condition.DbUpdate;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.able.jdbc.ordering.Ordering;
import com.gitee.qdbp.jdbc.api.CrudDao;
import com.gitee.qdbp.jdbc.api.SqlBufferJdbcOperations;
import com.gitee.qdbp.jdbc.exception.DbErrorCode;
import com.gitee.qdbp.jdbc.model.AllFieldColumn;
import com.gitee.qdbp.jdbc.model.PrimaryKeyFieldColumn;
import com.gitee.qdbp.jdbc.plugins.DbConditionConverter;
import com.gitee.qdbp.jdbc.plugins.EntityFillExecutor;
import com.gitee.qdbp.jdbc.plugins.EntityFillHandler;
import com.gitee.qdbp.jdbc.plugins.MapToBeanConverter;
import com.gitee.qdbp.jdbc.result.FirstColumnMapper;
import com.gitee.qdbp.jdbc.result.RowToBeanMapper;
import com.gitee.qdbp.jdbc.result.TableRowToBeanMapper;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.build.CrudSqlBuilder;
import com.gitee.qdbp.jdbc.sql.build.QuerySqlBuilder;
import com.gitee.qdbp.jdbc.sql.fragment.CrudFragmentHelper;
import com.gitee.qdbp.jdbc.sql.fragment.TableCrudFragmentHelper;
import com.gitee.qdbp.jdbc.utils.DbTools;
import com.gitee.qdbp.tools.utils.ConvertTools;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 基础增删改查数据库操作
 *
 * @author 赵卉华
 * @version 190601
 */
public class CrudDaoImpl<T> extends BaseQueryerImpl<T> implements CrudDao<T> {

    private static Logger log = LoggerFactory.getLogger(CrudDaoImpl.class);

    private Class<T> clazz;

    CrudDaoImpl(Class<T> c, SqlBufferJdbcOperations jdbc) {
        super(newQuerySqlBuilder(c, jdbc), newEntityFillExecutor(c), jdbc, newRowToBeanMapper(c));
        this.clazz = c;
    }

    private static QuerySqlBuilder newQuerySqlBuilder(Class<?> clazz, SqlBufferJdbcOperations jdbc) {
        CrudFragmentHelper sqlHelper = new TableCrudFragmentHelper(clazz, jdbc.findSqlDialect());
        return new CrudSqlBuilder(sqlHelper);
    }

    private static EntityFillExecutor newEntityFillExecutor(Class<?> clazz) {
        AllFieldColumn<?> allFields = DbTools.parseToAllFieldColumn(clazz);
        EntityFillHandler handler = DbTools.getEntityFillHandler();
        return new EntityFillExecutor(allFields, handler);
    }

    private static <T> RowToBeanMapper<T> newRowToBeanMapper(Class<T> clazz) {
        MapToBeanConverter converter = DbTools.getMapToBeanConverter();
        return new TableRowToBeanMapper<>(clazz, converter);
    }

    private CrudSqlBuilder builder() {
        return (CrudSqlBuilder) this.sqlBuilder;
    }

    @Override
    public T findById(String id) {
        VerifyTools.requireNotBlank(id, "id");
        PrimaryKeyFieldColumn pk = builder().helper().getPrimaryKey();
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
        entityFillExecutor.fillTableWhereDataStatus(readyWhere);
        List<String> startCodes = ConvertTools.toList(startCode);
        return doListChildren(startCodes, codeField, parentField, readyWhere, orderings);
    }

    @Override
    public List<T> listChildren(List<String> startCodes, String codeField, String parentField, DbWhere where,
            List<Ordering> orderings) {
        DbWhere readyWhere = checkWhere(where);
        entityFillExecutor.fillTableWhereDataStatus(readyWhere);
        return doListChildren(startCodes, codeField, parentField, readyWhere, orderings);
    }

    private List<T> doListChildren(List<String> startCodes, String codeField, String parentField, DbWhere where,
            List<Ordering> orderings) throws ServiceException {
        CrudFragmentHelper sqlHelper = builder().helper();
        List<String> selectFields = sqlHelper.getFieldNames();
        SqlBuffer buffer = dialect.buildFindChildrenSql(startCodes, codeField, parentField, selectFields, where,
            orderings, sqlHelper);
        return jdbc.query(buffer, this.rowToBeanMapper);
    }

    @Override
    public List<String> listChildrenCodes(String startCode, String codeField, String parentField, DbWhere where,
            List<Ordering> orderings) {
        DbWhere readyWhere = checkWhere(where);
        entityFillExecutor.fillTableWhereDataStatus(readyWhere);
        List<String> startCodes = ConvertTools.toList(startCode);
        return doListChildrenCodes(startCodes, codeField, parentField, readyWhere, orderings);
    }

    @Override
    public List<String> listChildrenCodes(List<String> startCodes, String codeField, String parentField, DbWhere where,
            List<Ordering> orderings) {
        DbWhere readyWhere = checkWhere(where);
        entityFillExecutor.fillTableWhereDataStatus(readyWhere);
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
        SqlBuffer buffer = dialect.buildFindChildrenSql(startCodes, codeField, parentField, selectFields, where,
            orderings, sqlHelper);
        return jdbc.query(buffer, FIRST_COLUMN_STRING_MAPPER);
    }

    private static FirstColumnMapper<String> FIRST_COLUMN_STRING_MAPPER = new FirstColumnMapper<>(String.class);

    @Override
    public String insert(T entity, boolean fillCreateParams) throws ServiceException {
        VerifyTools.requireNonNull(entity, "entity");
        DbConditionConverter converter = DbTools.getDbConditionConverter();
        Map<String, Object> readyEntity = converter.convertBeanToInsertMap(entity);
        return insert(readyEntity, fillCreateParams);
    }

    @Override
    public String insert(Map<String, Object> entity, boolean fillCreateParams) throws ServiceException {
        VerifyTools.requireNotBlank(entity, "entity");

        String tableName = builder().helper().getTableName();
        String id = null;
        // 查找主键
        PrimaryKeyFieldColumn pk = builder().helper().getPrimaryKey();
        if (pk == null) {
            log.debug("PrimaryKeyInfoNotFound, class={}", clazz);
        } else if (VerifyTools.isNotBlank(entity.get(pk.getFieldName()))) {
            id = entity.get(pk.getFieldName()).toString();
        } else {
            // 生成主键
            entity.put(pk.getFieldName(), id = entityFillExecutor.generatePrimaryKeyCode(tableName));
        }
        entityFillExecutor.fillTableCreateDataStatus(entity);
        if (fillCreateParams) {
            entityFillExecutor.fillTableCreteParams(entity);
        }

        SqlBuffer buffer = builder().buildInsertSql(entity);

        jdbc.update(buffer);
        return id;
    }

    @Override
    public List<String> insert(List<?> entities, boolean fillCreateParams) throws ServiceException {
        VerifyTools.requireNotBlank(entities, "entities");

        // TODO 目前是循环保存的, 以后改为批量保存
        List<String> ids = new ArrayList<>();
        for (Object item : entities) {
            if (item instanceof Map) {
                @SuppressWarnings("unchecked")
                String id = insert((Map<String, Object>) item, fillCreateParams);
                ids.add(id);
            } else {
                @SuppressWarnings("unchecked")
                String id = insert((T) item, fillCreateParams);
                ids.add(id);
            }
        }
        return ids;
    }

    @Override
    public int update(T entity, boolean fillUpdateParams, boolean errorOnUnaffected) throws ServiceException {
        VerifyTools.requireNonNull(entity, "entity");
        // 查找主键
        PrimaryKeyFieldColumn pk = builder().helper().getPrimaryKey();
        if (pk == null) {
            throw new UnsupportedOperationException("PrimaryKeyInfoNotFound, UnsupportedUpdateById, class=" + clazz);
        }
        DbConditionConverter converter = DbTools.getDbConditionConverter();
        DbUpdate readyEntity = converter.convertBeanToDbUpdate(entity);
        // 将主键从更新数据中移到查询条件中
        DbWhere where = new DbWhere();
        // 从更新条件中查找主键值并删除主键
        String pkValue = getValueAndRemoveField(readyEntity, pk.getFieldName());
        // 主键不能为空
        if (VerifyTools.isBlank(pkValue)) {
            log.warn("PrimaryKeyValueIsBlank, CanNotExecuteUpdateById, class={}", clazz);
            throw new ServiceException(DbErrorCode.DB_PRIMARY_KEY_VALUE_IS_REQUIRED);
        }
        where.on(pk.getFieldName(), "=", pkValue);
        if (readyEntity.isEmpty()) {
            throw new IllegalArgumentException("entity must not be empty");
        }

        entityFillExecutor.fillTableWhereDataStatus(where);
        if (fillUpdateParams) {
            entityFillExecutor.fillTableUpdateParams(readyEntity);
        }
        return this.doUpdate(readyEntity, where, errorOnUnaffected);
    }

    private String getValueAndRemoveField(DbUpdate ud, String fieldName) {
        // 从更新条件中删除主键
        List<DbCondition> removed = ud.remove(fieldName);
        // 从已删除的条件中查找最后一个主键的值
        // 更新的字段名一般不会重复, 但非要重复也不会报错: 
        // update TABLE set FIELD=1,FIELD=2 where ... (以最后一个生效)
        String pkValue = null;
        if (removed != null && !removed.isEmpty()) {
            for (DbCondition item : removed) {
                if (item instanceof DbField) {
                    Object fieldValue = ((DbField) item).getFieldValue();
                    String stringValue = fieldValue == null ? null : fieldValue.toString();
                    if (VerifyTools.isNotBlank(stringValue)) {
                        pkValue = stringValue;
                    }
                }
            }
        }
        return pkValue;
    }

    @Override
    public int update(T entity, DbWhere where, boolean fillUpdateParams, boolean errorOnUnaffected)
            throws ServiceException {
        VerifyTools.requireNonNull(entity, "entity");
        DbConditionConverter converter = DbTools.getDbConditionConverter();
        DbUpdate readyEntity = converter.convertBeanToDbUpdate(entity);
        if (readyEntity.isEmpty()) {
            throw new IllegalArgumentException("entity must not be empty");
        }

        DbWhere readyWhere = checkWhere(where);
        entityFillExecutor.fillTableWhereDataStatus(readyWhere);
        if (fillUpdateParams) {
            entityFillExecutor.fillTableUpdateParams(readyEntity);
        }
        return this.doUpdate(readyEntity, readyWhere, errorOnUnaffected);
    }

    @Override
    public int update(DbUpdate entity, DbWhere where, boolean fillUpdateParams, boolean errorOnUnaffected)
            throws ServiceException {
        VerifyTools.requireNotBlank(entity, "entity");
        DbWhere readyWhere = checkWhere(where);
        entityFillExecutor.fillTableWhereDataStatus(readyWhere);
        if (fillUpdateParams) {
            entityFillExecutor.fillTableUpdateParams(entity);
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
        PrimaryKeyFieldColumn pk = builder().helper().getPrimaryKey();
        if (pk == null) {
            throw new UnsupportedOperationException("PrimaryKeyInfoNotFound, UnsupportedDeleteById, class=" + clazz);
        }

        String primaryField = pk.getFieldName();
        DbWhere where = new DbWhere();
        where.on(primaryField, "in", ids);
        entityFillExecutor.fillTableWhereDataStatus(where);
        return this.doDelete(where, true, fillUpdateParams, errorOnUnaffected);
    }

    @Override
    public int logicalDelete(T where, boolean fillUpdateParams, boolean errorOnUnaffected) throws ServiceException {
        if (where == null) {
            throw new IllegalArgumentException("where must not be null, please use logicalDeleteAll()");
        }
        DbConditionConverter converter = DbTools.getDbConditionConverter();
        DbWhere readyWhere = converter.convertBeanToDbWhere(where);
        if (VerifyTools.isBlank(readyWhere)) {
            throw new IllegalArgumentException("where must not be empty, please use logicalDeleteAll()");
        }
        entityFillExecutor.fillTableWhereDataStatus(readyWhere);
        return this.doDelete(readyWhere, true, fillUpdateParams, errorOnUnaffected);
    }

    @Override
    public int logicalDelete(DbWhere where, boolean fillUpdateParams, boolean errorOnUnaffected)
            throws ServiceException {
        DbWhere readyWhere = checkWhere(where);
        entityFillExecutor.fillTableWhereDataStatus(readyWhere);
        return this.doDelete(readyWhere, true, fillUpdateParams, errorOnUnaffected);
    }

    @Override
    public int physicalDeleteByIds(List<String> ids, boolean errorOnUnaffected) throws ServiceException {
        VerifyTools.requireNotBlank(ids, "ids");
        PrimaryKeyFieldColumn pk = builder().helper().getPrimaryKey();
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
        if (where == null) {
            throw new IllegalArgumentException("where must not be null, please use physicalDeleteAll()");
        }
        DbConditionConverter converter = DbTools.getDbConditionConverter();
        DbWhere readyWhere = converter.convertBeanToDbWhere(where);
        if (VerifyTools.isBlank(readyWhere)) {
            throw new IllegalArgumentException("where must not be empty, please use physicalDeleteAll()");
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
            if (entityFillExecutor.supportedTableLogicalDelete()) { // 支持逻辑删除
                DbUpdate ud = new DbUpdate();
                entityFillExecutor.fillTableLogicalDeleteDataStatus(ud);
                if (fillUpdateParams) {
                    entityFillExecutor.fillTableUpdateParams(ud);
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
