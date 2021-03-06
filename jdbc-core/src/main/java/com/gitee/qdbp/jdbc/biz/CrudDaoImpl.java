package com.gitee.qdbp.jdbc.biz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.gitee.qdbp.able.exception.ServiceException;
import com.gitee.qdbp.able.jdbc.condition.DbUpdate;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.able.jdbc.fields.Fields;
import com.gitee.qdbp.able.jdbc.fields.IncludeFields;
import com.gitee.qdbp.able.jdbc.model.PkEntity;
import com.gitee.qdbp.able.jdbc.ordering.Orderings;
import com.gitee.qdbp.able.result.ResultCode;
import com.gitee.qdbp.jdbc.api.CrudDao;
import com.gitee.qdbp.jdbc.api.SqlBufferJdbcOperations;
import com.gitee.qdbp.jdbc.exception.DbErrorCode;
import com.gitee.qdbp.jdbc.model.AllFieldColumn;
import com.gitee.qdbp.jdbc.model.DbVersion;
import com.gitee.qdbp.jdbc.model.FieldScene;
import com.gitee.qdbp.jdbc.model.SimpleFieldColumn;
import com.gitee.qdbp.jdbc.plugins.BatchInsertExecutor;
import com.gitee.qdbp.jdbc.plugins.BatchUpdateExecutor;
import com.gitee.qdbp.jdbc.plugins.DbConditionConverter;
import com.gitee.qdbp.jdbc.plugins.EntityDataStateFillStrategy;
import com.gitee.qdbp.jdbc.plugins.EntityFieldFillExecutor;
import com.gitee.qdbp.jdbc.plugins.EntityFieldFillStrategy;
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

    protected Class<T> beanClass;
    /** 批量执行时的大小限制(0为无限制) **/
    protected int defaultBatchSize = QdbcBootImpl.DEFAULT_BATCH_SIZE;

    CrudDaoImpl(Class<T> c, SqlBufferJdbcOperations jdbc) {
        super(newQuerySqlBuilder(c, jdbc), newEntityFieldFillExecutor(c), jdbc, newRowToBeanMapper(c));
        this.beanClass = c;
    }

    private static QuerySqlBuilder newQuerySqlBuilder(Class<?> clazz, SqlBufferJdbcOperations jdbc) {
        CrudFragmentHelper sqlHelper = new TableCrudFragmentHelper(clazz, jdbc.getSqlDialect());
        return new CrudSqlBuilder(sqlHelper);
    }

    private static EntityFieldFillExecutor newEntityFieldFillExecutor(Class<?> clazz) {
        AllFieldColumn<?> allFields = DbTools.parseAllFieldColumns(clazz);
        EntityFieldFillStrategy fieldFillStrategy = DbTools.getEntityFieldFillStrategy();
        EntityDataStateFillStrategy<?> dataStateFillStrategy = DbTools.getEntityDataStateFillStrategy();
        return new EntityFieldFillExecutor(allFields, fieldFillStrategy, dataStateFillStrategy);
    }

    private static <T> RowToBeanMapper<T> newRowToBeanMapper(Class<T> clazz) {
        MapToBeanConverter converter = DbTools.getMapToBeanConverter();
        return new TableRowToBeanMapper<>(clazz, converter);
    }

    @Override
    public CrudSqlBuilder getSqlBuilder() {
        return (CrudSqlBuilder) this.sqlBuilder;
    }

    @Override
    public T findById(String id) {
        VerifyTools.requireNotBlank(id, "id");
        SimpleFieldColumn pk = getSqlBuilder().helper().getPrimaryKey();
        if (pk == null) { // 没有找到主键字段
            String details = "UnsupportedFindById, class=" + beanClass.getName();
            throw new ServiceException(DbErrorCode.DB_PRIMARY_KEY_FIELD_IS_UNRESOLVED, details);
        }
        String primaryField = pk.getFieldName();
        DbWhere where = new DbWhere();
        where.on(primaryField, "=", id);
        return this.find(where);
    }

    @Override
    public List<T> listByIds(List<String> ids, Orderings orderings) throws ServiceException {
        VerifyTools.requireNotBlank(ids, "ids");
        return listByIds(Fields.ALL, ids, orderings);
    }

    @Override
    public List<T> listByIds(Fields fields, List<String> ids, Orderings orderings) throws ServiceException {
        SimpleFieldColumn pk = getSqlBuilder().helper().getPrimaryKey();
        if (pk == null) { // 没有找到主键字段
            String details = "UnsupportedListByIds, class=" + beanClass.getName();
            throw new ServiceException(DbErrorCode.DB_PRIMARY_KEY_FIELD_IS_UNRESOLVED, details);
        }
        String primaryField = pk.getFieldName();
        DbWhere where = new DbWhere();
        where.on(primaryField, "in", ids);
        return list(fields, where, orderings);
    }

    @Override
    public List<T> listChildren(String startCode, String codeField, String parentField, DbWhere filterWhere,
            DbWhere searchWhere, Orderings orderings) {
        DbWhere filter = checkWhere(filterWhere);
        DbWhere search = checkWhere(searchWhere);
        // 数据状态填充在filterWhere中
        entityFieldFillExecutor.fillQueryWhereDataState(filter, getMajorTableAlias());
        // 数据权限填充在searchWhere中
        entityFieldFillExecutor.fillQueryWhereParams(search, getMajorTableAlias());

        List<String> startCodes = ConvertTools.toList(startCode);
        return doListChildren(startCodes, codeField, parentField, Fields.ALL, filter, search, orderings, beanClass);
    }

    @Override
    public List<T> listChildren(List<String> startCodes, String codeField, String parentField, DbWhere filterWhere,
            DbWhere searchWhere, Orderings orderings) {
        DbWhere filter = checkWhere(filterWhere);
        DbWhere search = checkWhere(searchWhere);
        // 数据状态填充在filterWhere中
        entityFieldFillExecutor.fillQueryWhereDataState(filter, getMajorTableAlias());
        // 数据权限填充在searchWhere中
        entityFieldFillExecutor.fillQueryWhereParams(search, getMajorTableAlias());

        return doListChildren(startCodes, codeField, parentField, Fields.ALL, filter, search, orderings, beanClass);
    }

    // ORACLE： START WITH {codeField} IN( {startCode} ) CONNECT BY PRIOR {codeField} = {parentField}
    // DB2: 使用WITH递归 
    // MYSQL 8.0+: 使用WITH RECURSIVE递归 
    // MYSQL 8.0-: 使用存储过程RECURSIVE_LIST_CHILDREN_QUERY
    protected <R> List<R> doListChildren(List<String> startCodes, String codeField, String parentField,
            Fields selectFields, DbWhere filterWhere, DbWhere searchWhere, Orderings orderings, Class<R> resultType)
            throws ServiceException {
        CrudFragmentHelper sqlHelper = getSqlBuilder().helper();
        String codeColumn = sqlHelper.getColumnName(FieldScene.CONDITION, codeField);
        String parentColumn = sqlHelper.getColumnName(FieldScene.CONDITION, parentField);
        SqlBuffer selectColumns = sqlHelper.buildSelectFieldsSql(selectFields);

        Map<String, Object> params = new HashMap<>();
        params.put("codeColumn", codeColumn);
        params.put("parentColumn", parentColumn);
        params.put("tableName", sqlHelper.getTableName());
        params.put("selectColumns", selectColumns);
        params.put("startCodes", startCodes);
        if (filterWhere != null && !filterWhere.isEmpty()) {
            params.put("filterWhere", sqlHelper.buildWhereSql(filterWhere, false));
        }
        if (searchWhere != null && !searchWhere.isEmpty()) {
            params.put("searchWhere", sqlHelper.buildWhereSql(searchWhere, false));
        }
        if (orderings != null && !orderings.isEmpty()) {
            params.put("orderBy", sqlHelper.buildOrderBySql(orderings, false));
        }

        String sqlId = "recursive.list.children.query";
        return jdbc.getSqlDao().listForObjects(sqlId, params, resultType);
    }

    @Override
    public List<String> listChildrenCodes(String startCode, String codeField, String parentField, DbWhere filterWhere,
            DbWhere searchWhere, Orderings orderings) {
        DbWhere filter = checkWhere(filterWhere);
        DbWhere search = checkWhere(searchWhere);
        // 数据状态填充在filterWhere中
        entityFieldFillExecutor.fillQueryWhereDataState(filter, getMajorTableAlias());
        // 数据权限填充在searchWhere中
        entityFieldFillExecutor.fillQueryWhereParams(search, getMajorTableAlias());

        List<String> startCodes = ConvertTools.toList(startCode);
        return doListChildrenCodes(startCodes, codeField, parentField, filter, search, orderings);
    }

    @Override
    public List<String> listChildrenCodes(List<String> startCodes, String codeField, String parentField,
            DbWhere filterWhere, DbWhere searchWhere, Orderings orderings) {
        DbWhere filter = checkWhere(filterWhere);
        DbWhere search = checkWhere(searchWhere);
        // 数据状态填充在filterWhere中
        entityFieldFillExecutor.fillQueryWhereDataState(filter, getMajorTableAlias());
        // 数据权限填充在searchWhere中
        entityFieldFillExecutor.fillQueryWhereParams(search, getMajorTableAlias());

        return doListChildrenCodes(startCodes, codeField, parentField, filter, search, orderings);
    }

    protected List<String> doListChildrenCodes(List<String> startCodes, String codeField, String parentField,
            DbWhere filterWhere, DbWhere searchWhere, Orderings orderings) throws ServiceException {
        DbWhere filter = checkWhere(filterWhere);
        DbWhere search = checkWhere(searchWhere);
        // 数据状态填充在filterWhere中
        entityFieldFillExecutor.fillQueryWhereDataState(filter, getMajorTableAlias());
        // 数据权限填充在searchWhere中
        entityFieldFillExecutor.fillQueryWhereParams(search, getMajorTableAlias());

        IncludeFields fields = new IncludeFields(codeField);
        return doListChildren(startCodes, codeField, parentField, fields, filter, search, orderings, String.class);
    }

    protected static FirstColumnMapper<String> FIRST_COLUMN_STRING_MAPPER = new FirstColumnMapper<>(String.class);

    protected Map<String, Object> copmareMapDifference(Map<String, Object> original, Map<String, Object> current) {
        Map<String, Object> diff = new HashMap<>();
        for (Map.Entry<String, Object> entry : original.entrySet()) {
            if (!current.containsKey(entry.getKey())) {
                diff.put(entry.getKey(), null);
            }
        }
        for (Map.Entry<String, Object> entry : current.entrySet()) {
            if (VerifyTools.notEquals(entry.getValue(), original.get(entry.getKey()))) {
                diff.put(entry.getKey(), entry.getValue());
            }
        }
        return diff;
    }

    @Override
    public String insert(T entity) throws ServiceException {
        VerifyTools.requireNonNull(entity, "entity");
        return executeInsert(entity, true);
    }

    @Override
    public String insert(T entity, boolean fillCreateParams) throws ServiceException {
        VerifyTools.requireNonNull(entity, "entity");
        return executeInsert(entity, fillCreateParams);
    }

    @Override
    public String insert(Map<String, Object> entity) throws ServiceException {
        VerifyTools.requireNonNull(entity, "entity");
        return executeInsert(entity, true);
    }

    @Override
    public String insert(Map<String, Object> entity, boolean fillCreateParams) throws ServiceException {
        VerifyTools.requireNonNull(entity, "entity");
        return executeInsert(entity, fillCreateParams);
    }

    protected String executeInsert(Object object, boolean fillCreateParams) throws ServiceException {
        // 将实体类转换为map, 并执行实体业务数据填充
        PkEntity pe = convertAndFillCreateParams(object, fillCreateParams);
        String id = pe.getPrimaryKey();
        Map<String, Object> readyEntity = pe.getEntity();
        // 执行数据库插入
        SqlBuffer buffer = getSqlBuilder().buildInsertSql(readyEntity);
        jdbc.insert(buffer);
        return id;
    }

    @Override
    public List<String> inserts(List<?> entities) throws ServiceException {
        return inserts(entities, true, this.defaultBatchSize);
    }

    @Override
    public List<String> inserts(List<?> entities, boolean fillCreateParams) throws ServiceException {
        return inserts(entities, fillCreateParams, this.defaultBatchSize);
    }

    protected List<String> inserts(List<?> entities, boolean fillCreateParams, int batchSize) throws ServiceException {
        VerifyTools.requireNotBlank(entities, "entities");
        if (entities.size() == 1) {
            Object first = entities.get(0);
            String id = executeInsert(first, fillCreateParams);
            return ConvertTools.toList(id);
        } else if (batchSize <= 0 || entities.size() <= batchSize) {
            return doBatchInserts(entities, fillCreateParams);
        } else { // 分批导入
            List<String> ids = new ArrayList<>();
            List<Object> buffer = new ArrayList<>();
            for (int i = 1, size = entities.size(); i <= size; i++) {
                buffer.add(entities.get(i - 1));
                if (i % batchSize == 0 || i == size) {
                    List<String> batchIds = doBatchInserts(buffer, fillCreateParams);
                    ids.addAll(batchIds);
                    buffer.clear();
                }
            }
            return ids;
        }
    }

    protected List<String> doBatchInserts(List<?> entities, boolean fillCreateParams) throws ServiceException {
        List<PkEntity> contents = new ArrayList<>();
        for (Object item : entities) {
            // 将实体类转换为map, 并执行实体业务数据填充
            PkEntity pe = convertAndFillCreateParams(item, fillCreateParams);
            contents.add(pe);
        }

        DbVersion version = jdbc.getDbVersion();
        BatchInsertExecutor batchOperator = DbTools.getBatchInsertExecutor(version);
        // 执行批量数据库插入
        return batchOperator.inserts(contents, jdbc, getSqlBuilder());
    }

    @Override
    public int update(T entity) throws ServiceException {
        return this.update(entity, true, false);
    }

    @Override
    public int update(T entity, boolean fillUpdateParams, boolean errorOnUnaffected) throws ServiceException {
        VerifyTools.requireNonNull(entity, "entity");
        // 查找主键
        SimpleFieldColumn pk = getSqlBuilder().helper().getPrimaryKey();
        if (pk == null) { // 没有找到主键字段
            String details = "UnsupportedUpdateById, class=" + beanClass.getName();
            throw new ServiceException(DbErrorCode.DB_PRIMARY_KEY_FIELD_IS_UNRESOLVED, details);
        }
        PkEntity pe = convertAndFillUpdateParams(entity, fillUpdateParams);
        String pkValue = pe.getPrimaryKey();
        if (VerifyTools.isBlank(pkValue)) { // 主键值为空
            String details = "CanNotExecuteUpdateById, class=" + beanClass.getName();
            throw new ServiceException(DbErrorCode.DB_PRIMARY_KEY_VALUE_IS_REQUIRED, details);
        }
        // 实体类转换为DbUpdate
        DbConditionConverter converter = DbTools.getDbConditionConverter();
        DbUpdate ud = converter.parseMapToDbUpdate(pe.getEntity());
        // 生成主键限制条件
        DbWhere where = new DbWhere();
        where.on(pk.getFieldName(), "=", pkValue);
        entityFieldFillExecutor.fillUpdateWhereDataState(where);
        entityFieldFillExecutor.fillUpdateWhereParams(where);
        return this.doUpdate(ud, where, errorOnUnaffected);
    }

    @Override
    public int update(Map<String, Object> entity) throws ServiceException {
        return this.update(entity, true, false);
    }

    @Override
    public int update(Map<String, Object> entity, boolean fillUpdateParams, boolean errorOnUnaffected)
            throws ServiceException {
        VerifyTools.requireNonNull(entity, "entity");
        DbConditionConverter converter = DbTools.getDbConditionConverter();
        if (!entity.containsKey("where")) { // 没有where条件, 必须要有主键
            if (entity.isEmpty()) {
                String details = "NoFieldsThatBeUpdatedWereFound, class=" + beanClass.getName();
                throw new ServiceException(DbErrorCode.DB_ENTITY_MUST_NOT_BE_EMPTY, details);
            }
            // 查找主键
            SimpleFieldColumn pk = getSqlBuilder().helper().getPrimaryKey();
            if (pk == null) { // 没有找到主键字段
                String details = "UnsupportedUpdateById, class=" + beanClass.getName();
                throw new ServiceException(DbErrorCode.DB_PRIMARY_KEY_FIELD_IS_UNRESOLVED, details);
            }
            Object pkValue = entity.get(pk.getFieldName());
            if (VerifyTools.isBlank(pkValue)) { // 主键值为空
                String details = "CanNotExecuteUpdateById, class=" + beanClass.getName();
                throw new ServiceException(DbErrorCode.DB_PRIMARY_KEY_VALUE_IS_REQUIRED, details);
            }
            // 填充实体的修改参数(如修改人修改时间等)
            fillEntityUpdateParams(entity, fillUpdateParams);
            // 转换为DbUpdate对象
            DbUpdate ud = converter.parseMapToDbUpdate(entity);
            // 生成主键查询条件
            DbWhere where = new DbWhere();
            where.on(pk.getFieldName(), "=", pkValue);
            // 填充Where条件
            entityFieldFillExecutor.fillUpdateWhereDataState(where);
            entityFieldFillExecutor.fillUpdateWhereParams(where);
            return this.doUpdate(ud, where, errorOnUnaffected);
        } else { // 如果Update对象中带有where字段, 将where字段值转换为DbWhere对象
            Object whereValue = entity.remove("where");
            DbWhere where;
            if (whereValue == null) {
                where = null;
            } else if (whereValue instanceof DbWhere) {
                where = (DbWhere) whereValue;
            } else if (whereValue instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> mapValue = (Map<String, Object>) whereValue;
                where = converter.parseMapToDbWhere(mapValue);
            } else if (beanClass.isAssignableFrom(whereValue.getClass())) {
                @SuppressWarnings("unchecked")
                T entityValue = (T) whereValue;
                where = converter.convertBeanToDbWhere(entityValue);
            } else {
                // whereValue只支持DbWhere/map/T
                String details = "CanNotExecuteUpdate, class=" + beanClass.getName();
                throw new ServiceException(DbErrorCode.DB_UNSUPPORTED_WHERE_TYPE, details);
            }
            if (entity.isEmpty()) {
                String details = "NoFieldsThatBeUpdatedWereFound, class=" + beanClass.getName();
                throw new ServiceException(DbErrorCode.DB_ENTITY_MUST_NOT_BE_EMPTY, details);
            }
            // 填充实体的修改参数(如修改人修改时间等)
            fillEntityUpdateParams(entity, fillUpdateParams);
            // 转换为DbUpdate对象
            DbUpdate ud = converter.parseMapToDbUpdate(entity);
            // 检查Where条件
            DbWhere readyWhere = checkWhere(where);
            // 填充Where条件
            entityFieldFillExecutor.fillUpdateWhereDataState(where);
            entityFieldFillExecutor.fillUpdateWhereParams(where);
            return this.doUpdate(ud, readyWhere, errorOnUnaffected);
        }
    }

    @Override
    public int update(T entity, DbWhere where) throws ServiceException {
        return this.update(entity, where, true, false);
    }

    @Override
    public int update(T entity, DbWhere where, boolean fillUpdateParams, boolean errorOnUnaffected)
            throws ServiceException {
        VerifyTools.requireNonNull(entity, "entity");
        Map<String, Object> mapEntity = convertEntityAndFillUpdateParams(entity, fillUpdateParams);
        DbConditionConverter converter = DbTools.getDbConditionConverter();
        DbUpdate readyEntity = converter.parseMapToDbUpdate(mapEntity);
        if (readyEntity.isEmpty()) {
            String details = "NoFieldsThatBeUpdatedWereFound, class=" + beanClass.getName();
            throw new ServiceException(DbErrorCode.DB_ENTITY_MUST_NOT_BE_EMPTY, details);
        }

        DbWhere readyWhere = checkWhere(where);
        entityFieldFillExecutor.fillUpdateWhereDataState(readyWhere);
        entityFieldFillExecutor.fillUpdateWhereParams(readyWhere);
        return this.doUpdate(readyEntity, readyWhere, errorOnUnaffected);
    }

    @Override
    public int update(DbUpdate entity, DbWhere where) throws ServiceException {
        return this.update(entity, where, true, false);
    }

    @Override
    public int update(DbUpdate entity, DbWhere where, boolean fillUpdateParams, boolean errorOnUnaffected)
            throws ServiceException {
        VerifyTools.requireNotBlank(entity, "entity");
        DbWhere readyWhere = checkWhere(where);
        entityFieldFillExecutor.fillUpdateWhereDataState(readyWhere);
        entityFieldFillExecutor.fillUpdateWhereParams(readyWhere);
        // 不需要自动填充修改时的数据状态标记, 没有这样的业务场景
        // entityFieldFillExecutor.fillEntityUpdateDataState(entity);
        if (fillUpdateParams) {
            entityFieldFillExecutor.fillEntityUpdateParams(entity);
        }
        return this.doUpdate(entity, readyWhere, errorOnUnaffected);
    }

    protected int doUpdate(DbUpdate readyEntity, DbWhere readyWhere, boolean errorOnUnaffected)
            throws ServiceException {
        SqlBuffer buffer = getSqlBuilder().buildUpdateSql(readyEntity, readyWhere);

        int rows = jdbc.update(buffer);

        if (rows == 0 && errorOnUnaffected) {
            throw new ServiceException(DbErrorCode.DB_AFFECTED_ROWS_IS_ZERO);
        }
        return rows;
    }

    @Override
    public int updates(List<?> entities) throws ServiceException {
        return updates(entities, true, this.defaultBatchSize);
    }

    @Override
    public int updates(List<?> entities, boolean fillUpdateParams) throws ServiceException {
        return updates(entities, fillUpdateParams, this.defaultBatchSize);
    }

    protected int updates(List<?> entities, boolean fillUpdateParams, int batchSize) throws ServiceException {
        VerifyTools.requireNotBlank(entities, "entities");
        // 查找主键(批量更新必须要有主键)
        SimpleFieldColumn pk = getSqlBuilder().helper().getPrimaryKey();
        if (pk == null) { // 没有找到主键字段
            String details = "UnsupportedBatchUpdate, class=" + beanClass.getName();
            throw new ServiceException(DbErrorCode.DB_PRIMARY_KEY_FIELD_IS_UNRESOLVED, details);
        }
        if (entities.size() == 1) {
            Object first = entities.get(0);
            // 将实体类转换为map, 并执行实体业务数据填充
            PkEntity pe = convertAndFillUpdateParams(first, fillUpdateParams);
            String pkValue = pe.getPrimaryKey();
            if (VerifyTools.isBlank(pkValue)) { // 主键值为空
                String details = "UnsupportedBatchUpdate, class=" + beanClass.getName();
                throw new ServiceException(DbErrorCode.DB_PRIMARY_KEY_VALUE_IS_REQUIRED, details);
            }
            // 实体类转换为DbUpdate
            DbConditionConverter converter = DbTools.getDbConditionConverter();
            DbUpdate ud = converter.parseMapToDbUpdate(pe.getEntity());
            // 生成主键限制条件
            DbWhere where = new DbWhere();
            where.on(pk.getFieldName(), "=", pkValue);
            // 与批量处理的逻辑保持一致, 不限制数据状态
            // entityFieldFillExecutor.fillUpdateWhereDataState(where);
            // entityFieldFillExecutor.fillUpdateWhereParams(where);
            return this.doUpdate(ud, where, false);
        } else if (batchSize <= 0 || entities.size() <= batchSize) {
            return doBatchUpdates(entities, fillUpdateParams);
        } else { // 分批导入
            List<Object> buffer = new ArrayList<>();
            int rows = 0;
            for (int i = 1, size = entities.size(); i <= size; i++) {
                buffer.add(entities.get(i - 1));
                if (i % batchSize == 0 || i == size) {
                    rows += doBatchUpdates(buffer, fillUpdateParams);
                    buffer.clear();
                }
            }
            return rows;
        }
    }

    /**
     * 执行批量更新
     * 
     * @param entities 实体对象列表(只能是entity或map或IdEntity列表, 其他参数将会报错)
     * @param fillUpdateParams 是否自动填充更新参数(修改人/修改时间等)
     * @return 受影响行数
     * @throws ServiceException 操作失败
     */
    protected int doBatchUpdates(List<?> entities, boolean fillUpdateParams) throws ServiceException {
        List<PkEntity> contents = new ArrayList<>();
        for (Object item : entities) {
            // 将实体类转换为map, 并执行实体业务数据填充
            PkEntity pkud = convertAndFillUpdateParams(item, fillUpdateParams);
            contents.add(pkud);
        }
        DbVersion version = jdbc.getDbVersion();
        BatchUpdateExecutor batchOperator = DbTools.getBatchUpdateExecutor(version);
        // 执行批量数据库更新
        return batchOperator.updates(contents, jdbc, getSqlBuilder());
    }

    @Override
    public int logicalDeleteByIds(List<String> ids) throws ServiceException {
        return logicalDeleteByIds(ids, true, false);
    }

    @Override
    public int logicalDeleteByIds(List<String> ids, boolean fillUpdateParams, boolean errorOnUnaffected)
            throws ServiceException {
        VerifyTools.requireNotBlank(ids, "ids");
        SimpleFieldColumn pk = getSqlBuilder().helper().getPrimaryKey();
        if (pk == null) { // 没有找到主键字段
            String details = "UnsupportedDeleteById, class=" + beanClass.getName();
            throw new ServiceException(DbErrorCode.DB_PRIMARY_KEY_FIELD_IS_UNRESOLVED, details);
        }

        String primaryField = pk.getFieldName();
        DbWhere where = new DbWhere();
        where.on(primaryField, "in", ids);
        entityFieldFillExecutor.fillDeleteWhereDataState(where);
        entityFieldFillExecutor.fillDeleteWhereParams(where);
        return this.doDelete(where, false, fillUpdateParams, errorOnUnaffected);
    }

    @Override
    public int logicalDelete(T where) throws ServiceException {
        return logicalDelete(where, true, false);
    }

    @Override
    public int logicalDelete(T where, boolean fillUpdateParams, boolean errorOnUnaffected) throws ServiceException {
        if (where == null) {
            String details = "If you want to delete all records, please use DbWhere.NONE";
            throw new ServiceException(DbErrorCode.DB_WHERE_MUST_NOT_BE_EMPTY, details);
        }
        DbConditionConverter converter = DbTools.getDbConditionConverter();
        DbWhere beanWhere = converter.convertBeanToDbWhere(where);
        DbWhere readyWhere = checkWhere(beanWhere);
        entityFieldFillExecutor.fillDeleteWhereDataState(readyWhere);
        entityFieldFillExecutor.fillDeleteWhereParams(readyWhere);
        return this.doDelete(readyWhere, false, fillUpdateParams, errorOnUnaffected);
    }

    @Override
    public int logicalDelete(DbWhere where) throws ServiceException {
        return logicalDelete(where, true, false);
    }

    @Override
    public int logicalDelete(DbWhere where, boolean fillUpdateParams, boolean errorOnUnaffected)
            throws ServiceException {
        DbWhere readyWhere = checkWhere(where);
        entityFieldFillExecutor.fillDeleteWhereDataState(readyWhere);
        entityFieldFillExecutor.fillDeleteWhereParams(readyWhere);
        return this.doDelete(readyWhere, false, fillUpdateParams, errorOnUnaffected);
    }

    @Override
    public int physicalDeleteByIds(List<String> ids) throws ServiceException {
        return physicalDeleteByIds(ids, false);
    }

    @Override
    public int physicalDeleteByIds(List<String> ids, boolean errorOnUnaffected) throws ServiceException {
        VerifyTools.requireNotBlank(ids, "ids");
        SimpleFieldColumn pk = getSqlBuilder().helper().getPrimaryKey();
        if (pk == null) { // 没有找到主键字段
            String details = "UnsupportedDeleteById, class=" + beanClass.getName();
            throw new ServiceException(DbErrorCode.DB_PRIMARY_KEY_FIELD_IS_UNRESOLVED, details);
        }

        String primaryField = pk.getFieldName();
        DbWhere where = new DbWhere();
        where.on(primaryField, "in", ids);
        return this.doDelete(where, true, false, errorOnUnaffected);
    }

    @Override
    public int physicalDelete(T where) throws ServiceException {
        return physicalDelete(where, false);
    }

    @Override
    public int physicalDelete(T where, boolean errorOnUnaffected) throws ServiceException {
        if (where == null) {
            String details = "If you want to delete all records, please use DbWhere.NONE";
            throw new ServiceException(DbErrorCode.DB_WHERE_MUST_NOT_BE_EMPTY, details);
        }
        DbConditionConverter converter = DbTools.getDbConditionConverter();
        DbWhere beanWhere = converter.convertBeanToDbWhere(where);
        DbWhere readyWhere = checkWhere(beanWhere);
        return this.doDelete(readyWhere, true, false, errorOnUnaffected);
    }

    @Override
    public int physicalDelete(DbWhere where) throws ServiceException {
        return this.doDelete(where, true, false, false);
    }

    @Override
    public int physicalDelete(DbWhere where, boolean errorOnUnaffected) throws ServiceException {
        return this.doDelete(where, true, false, errorOnUnaffected);
    }

    protected int doDelete(DbWhere readyWhere, boolean physical, boolean fillUpdateParams, boolean errorOnUnaffected)
            throws ServiceException {
        int rows;
        if (physical) { // 物理删除
            SqlBuffer buffer = getSqlBuilder().buildDeleteSql(readyWhere);
            rows = jdbc.delete(buffer);
        } else { // 逻辑删除
            if (entityFieldFillExecutor.supportedTableLogicalDelete()) { // 支持逻辑删除
                DbUpdate ud = new DbUpdate();
                entityFieldFillExecutor.fillLogicalDeleteDataState(ud);
                if (fillUpdateParams) {
                    entityFieldFillExecutor.fillLogicalDeleteParams(ud);
                }
                SqlBuffer buffer = getSqlBuilder().buildUpdateSql(ud, readyWhere);
                rows = jdbc.update(buffer);
            } else { // 不支持逻辑删除
                String details = "UnsupportedLogicDelete, class=" + beanClass.getName();
                throw new ServiceException(DbErrorCode.DB_UNSUPPORTED_LOGICAL_DELETE, details);
            }
        }

        if (rows == 0 && errorOnUnaffected) {
            throw new ServiceException(DbErrorCode.DB_AFFECTED_ROWS_IS_ZERO);
        }
        return rows;
    }

    /**
     * 执行实体业务数据转换及创建参数填充<br>
     * 作为以下两个方法的入口, 参数只支持map和T两种:<br>
     * fillEntityCreateParams(Map, boolean)/convertEntityAndFillCreateParams(T, boolean)<br>
     * 
     * @param object 实体对象, 只支持map和T两种
     * @param fillCreateParams 是否自动填充创建参数
     * @return 实体类转换后的map和自动填充的主键ID
     */
    protected PkEntity convertAndFillCreateParams(Object object, boolean fillCreateParams) {
        if (object == null) {
            String details = "CanNotExecuteCreate, class=" + beanClass.getName();
            throw new ServiceException(DbErrorCode.DB_ENTITY_MUST_NOT_BE_EMPTY, details);
        }
        if (object instanceof PkEntity) {
            PkEntity pe = (PkEntity) object;
            Map<String, Object> entity = pe.getEntity();
            SimpleFieldColumn pk = getSqlBuilder().helper().getPrimaryKey();
            // 如果PkEntity里面的id不为空, 设置到entity中
            if (pk != null && VerifyTools.isNotBlank(pe.getPrimaryKey())) {
                entity.put(pk.getFieldName(), pe.getPrimaryKey());
            }
            if (entity.isEmpty()) {
                String details = "NoFieldsThatBeInsertedWereFound, class=" + beanClass.getName();
                throw new ServiceException(DbErrorCode.DB_ENTITY_MUST_NOT_BE_EMPTY, details);
            }
            // 填充主键/数据状态/创建人/创建时间等信息
            fillEntityCreateParams(entity, fillCreateParams);
            return pe;
        } else if (object instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> entity = (Map<String, Object>) object;
            if (entity.isEmpty()) {
                String details = "NoFieldsThatBeInsertedWereFound, class=" + beanClass.getName();
                throw new ServiceException(DbErrorCode.DB_ENTITY_MUST_NOT_BE_EMPTY, details);
            }
            String id = fillEntityCreateParams(entity, fillCreateParams);
            return new PkEntity(id, entity);
        } else if (beanClass.isAssignableFrom(object.getClass())) {
            @SuppressWarnings("unchecked")
            T entity = (T) object;
            return convertEntityAndFillCreateParams(entity, fillCreateParams);
        } else {
            String details = "CanNotExecuteCreate, class=" + beanClass.getName();
            throw new ServiceException(DbErrorCode.DB_UNSUPPORTED_ENTITY_TYPE, details);
        }
    }

    /**
     * 执行实体业务数据创建参数填充<br>
     * 1. 查找主键ID, 如果有主键字段且字段值为空, 就自动生成ID<br>
     * 2. 填充创建参数中的数据状态<br>
     * 3. 如果fillCreateParams=true, 则填充其他创建参数
     * 
     * @param entity 实体对象
     * @param fillCreateParams 是否自动填充创建参数
     * @return 填充或获取的主键ID
     */
    protected String fillEntityCreateParams(Map<String, Object> entity, boolean fillCreateParams) {
        String tableName = getSqlBuilder().helper().getTableName();
        // 查找主键
        String id;
        SimpleFieldColumn pk = getSqlBuilder().helper().getPrimaryKey();
        if (pk == null) {
            id = null;
        } else if (VerifyTools.isNotBlank(entity.get(pk.getFieldName()))) {
            id = entity.get(pk.getFieldName()).toString();
        } else {
            // 生成主键
            entity.put(pk.getFieldName(), id = entityFieldFillExecutor.generatePrimaryKeyCode(tableName));
        }
        entityFieldFillExecutor.fillEntityCreateDataState(entity);
        if (fillCreateParams) {
            entityFieldFillExecutor.fillEntityCreateParams(entity);
        }
        return id;
    }

    /**
     * 执行实体业务数据转换及创建参数填充<br>
     * 这里存在一个问题, 执行fillEntityCreateParams之后, 入参entity中的字段得不到更新<br>
     * 因此, 执行fillEntityCreateParams之前先记录下所有字段值原值<br>
     * 执行完fillEntityCreateParams之后比对字段差异, 将差异部分设置回入参entity中<br>
     * 比对差异是为了提升性能, 因为对entity操作需要用到反射, 而比对差异只是对map操作, 快很多<br>
     * 例如一个类有20个字段, fillEntityCreateParams只修改了2个, 比对差异可以避免对其他18个字段的反射操作<br>
     * 
     * @param entity 实体对象
     * @param fillCreateParams 是否自动填充创建参数
     * @return 实体类转换后的map和自动填充的主键ID
     */
    protected PkEntity convertEntityAndFillCreateParams(T entity, boolean fillCreateParams) {
        // 将对象转换为map
        DbConditionConverter conditionConverter = DbTools.getDbConditionConverter();
        Map<String, Object> readyEntity = conditionConverter.convertBeanToInsertMap(entity);
        if (readyEntity.isEmpty()) {
            String details = "NoFieldsThatBeInsertedWereFound, class=" + beanClass.getName();
            throw new ServiceException(DbErrorCode.DB_ENTITY_MUST_NOT_BE_EMPTY, details);
        }
        // 记录下所有字段值原值, 用于比较差异
        Map<String, Object> original = new HashMap<>();
        original.putAll(readyEntity);
        // 填充主键/数据状态/创建人/创建时间等信息
        String id = fillEntityCreateParams(readyEntity, fillCreateParams);
        // 比对修改后的字段值与原值的差异, 复制到原对象
        Map<String, Object> diff = copmareMapDifference(original, readyEntity);
        MapToBeanConverter mapToBeanConverter = DbTools.getMapToBeanConverter();
        mapToBeanConverter.fill(diff, entity);
        return new PkEntity(id, readyEntity);
    }

    /**
     * 执行实体业务数据转换及更新参数填充
     * 
     * @param object 实体对象, 只支持map/T/PkEntity, 其他类型将会报错<br>
     *            如果object是map, map中不能有where, 如果有,调这个方法之前要移除, 否则将会报错
     * @param fillUpdateParams 是否自动填充更新参数
     * @return 转换后的update和where
     */
    protected PkEntity convertAndFillUpdateParams(Object object, boolean fillUpdateParams) {
        if (object == null) {
            String details = "CanNotExecuteUpdate, EntityIsNull, class=" + beanClass.getName();
            throw new ServiceException(DbErrorCode.DB_ENTITY_MUST_NOT_BE_EMPTY, details);
        }
        // 查找主键
        SimpleFieldColumn pk = getSqlBuilder().helper().getPrimaryKey();
        if (pk == null) { // 没有找到主键字段
            String details = "UnsupportedBatchUpdate, class=" + beanClass.getName();
            throw new ServiceException(DbErrorCode.DB_PRIMARY_KEY_FIELD_IS_UNRESOLVED, details);
        }
        if (object instanceof PkEntity) {
            PkEntity pe = (PkEntity) object;
            String pkValue = pe.getPrimaryKey();
            // 主键值不能为空
            if (VerifyTools.isBlank(pkValue)) { // 主键值为空
                String details = "CanNotExecuteUpdateById, class=" + beanClass.getName();
                throw new ServiceException(DbErrorCode.DB_PRIMARY_KEY_VALUE_IS_REQUIRED, details);
            }
            Map<String, Object> entity = pe.getEntity();
            // 检查UpdateEntity
            checkUpdateEntity(entity);
            // 填充实体的修改参数(如修改人修改时间等)
            fillEntityUpdateParams(entity, fillUpdateParams);
            return pe;
        } else if (object instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> entity = (Map<String, Object>) object;
            // 检查UpdateEntity
            checkUpdateEntity(entity);
            Object pkValue = entity.get(pk.getFieldName());
            // 主键值不能为空
            if (VerifyTools.isBlank(pkValue)) { // 主键值为空
                String details = "CanNotExecuteUpdateById, class=" + beanClass.getName();
                throw new ServiceException(DbErrorCode.DB_PRIMARY_KEY_VALUE_IS_REQUIRED, details);
            }
            // 填充实体的修改参数(如修改人修改时间等)
            fillEntityUpdateParams(entity, fillUpdateParams);
            return new PkEntity(pkValue.toString(), entity);
        } else if (beanClass.isAssignableFrom(object.getClass())) {
            @SuppressWarnings("unchecked")
            T entity = (T) object;
            Map<String, Object> map = convertEntityAndFillUpdateParams(entity, fillUpdateParams);
            Object pkValue = map.get(pk.getFieldName());
            // 主键值不能为空
            if (VerifyTools.isBlank(pkValue)) { // 主键值为空
                String details = "CanNotExecuteUpdateById, class=" + beanClass.getName();
                throw new ServiceException(DbErrorCode.DB_PRIMARY_KEY_VALUE_IS_REQUIRED, details);
            }
            return new PkEntity(pkValue.toString(), map);
        } else {
            String details = "CanNotExecuteUpdate, class=" + beanClass.getName();
            throw new ServiceException(DbErrorCode.DB_UNSUPPORTED_ENTITY_TYPE, details);
        }
    }

    private void checkUpdateEntity(Map<String, Object> entity) {
        if (entity == null || entity.isEmpty()) {
            String details = "NoFieldsThatBeUpdatedWereFound, class=" + beanClass.getName();
            throw new ServiceException(DbErrorCode.DB_ENTITY_MUST_NOT_BE_EMPTY, details);
        }
        if (entity.containsKey("where")) {
            // entity里面不能有where, 如果有,调这个方法之前要移除, 否则将会报错
            // 因为批量更新的语法不支持每个ID带有不同的where条件
            String details = "Unsupported entity.where";
            throw new ServiceException(ResultCode.PARAMETER_VALUE_ERROR, details);
        }
    }

    /**
     * 执行实体业务数据更新参数填充
     * 
     * @param entity 实体对象
     * @param fillCreateParams 是否自动填充创建参数
     */
    protected void fillEntityUpdateParams(Map<String, Object> entity, boolean fillUpdateParams) {
        // 不需要自动填充修改时的数据状态标记, 没有这样的业务场景
        // entityFieldFillExecutor.fillEntityUpdateDataState(entity);
        if (fillUpdateParams) { // 填充单表修改参数(如修改人修改时间等)
            entityFieldFillExecutor.fillEntityUpdateParams(entity);
        }
    }

    /**
     * 执行实体业务数据更新参数填充
     * 
     * @param ud 待更新的内容
     * @param fillCreateParams 是否自动填充创建参数
     */
    protected void fillEntityUpdateParams(DbUpdate ud, boolean fillUpdateParams) {
        // 不需要自动填充修改时的数据状态标记, 没有这样的业务场景
        // entityFieldFillExecutor.fillEntityUpdateDataState(ud);
        if (fillUpdateParams) { // 填充单表修改参数(如修改人修改时间等)
            entityFieldFillExecutor.fillEntityUpdateParams(ud);
        }
    }

    /**
     * 执行实体业务数据创建参数填充<br>
     * 这里存在一个问题, 执行fillEntityUpdateParams之后, 入参entity中的字段得不到更新<br>
     * 因此, 执行fillEntityUpdateParams之前先记录下所有字段值原值<br>
     * 执行完fillEntityUpdateParams之后比对字段差异, 将差异部分设置回入参entity中<br>
     * 比对差异是为了提升性能, 因为对entity操作需要用到反射, 而比对差异只是对map操作, 快很多<br>
     * 例如一个类有20个字段, fillEntityUpdateParams只修改了2个, 比对差异可以避免对其他18个字段的反射操作<br>
     * 
     * @param entity 实体对象
     * @param fillUpdateParams 是否自动填充更新参数
     * @return 实体类转换后的map
     */
    protected Map<String, Object> convertEntityAndFillUpdateParams(T entity, boolean fillUpdateParams) {
        // 将对象转换为map
        DbConditionConverter conditionConverter = DbTools.getDbConditionConverter();
        Map<String, Object> readyEntity = conditionConverter.convertBeanToUpdateMap(entity);
        if (readyEntity.isEmpty()) {
            String details = "NoFieldsThatBeUpdatedWereFound, class=" + beanClass.getName();
            throw new ServiceException(DbErrorCode.DB_ENTITY_MUST_NOT_BE_EMPTY, details);
        }
        // 记录下所有字段值原值, 用于比较差异
        Map<String, Object> original = new HashMap<>();
        original.putAll(readyEntity);
        // 填充主键/数据状态/创建人/创建时间等信息
        fillEntityUpdateParams(readyEntity, fillUpdateParams);
        // 比对修改后的字段值与原值的差异, 复制到原对象
        Map<String, Object> diff = copmareMapDifference(original, readyEntity);
        MapToBeanConverter mapToBeanConverter = DbTools.getMapToBeanConverter();
        mapToBeanConverter.fill(diff, entity);
        return readyEntity;
    }

    /** 获取当前实例的Bean类型 **/
    public Class<T> getBeanClass() {
        return beanClass;
    }

    /** 批量执行时的大小限制(0为无限制) **/
    protected int getDefaultBatchSize() {
        return defaultBatchSize;
    }

    /** 批量执行时的大小限制(0为无限制) **/
    protected void setDefaultBatchSize(int batchSize) {
        this.defaultBatchSize = batchSize;
    }
}
