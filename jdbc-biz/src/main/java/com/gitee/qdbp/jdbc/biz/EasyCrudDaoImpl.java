package com.gitee.qdbp.jdbc.biz;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.gitee.qdbp.able.beans.KeyValue;
import com.gitee.qdbp.able.exception.ServiceException;
import com.gitee.qdbp.able.model.ordering.OrderPaging;
import com.gitee.qdbp.able.model.ordering.Ordering;
import com.gitee.qdbp.able.model.paging.PageList;
import com.gitee.qdbp.able.model.paging.PartList;
import com.gitee.qdbp.jdbc.api.EasyCrudDao;
import com.gitee.qdbp.jdbc.api.SqlBufferJdbcOperations;
import com.gitee.qdbp.jdbc.condition.DbField;
import com.gitee.qdbp.jdbc.condition.DbUpdate;
import com.gitee.qdbp.jdbc.condition.DbWhere;
import com.gitee.qdbp.jdbc.condition.DbWhere.EmptyDbWhere;
import com.gitee.qdbp.jdbc.exception.DbErrorCode;
import com.gitee.qdbp.jdbc.model.PrimaryKey;
import com.gitee.qdbp.jdbc.plugins.ModelDataExecutor;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.result.FirstColumnMapper;
import com.gitee.qdbp.jdbc.result.KeyIntegerMapper;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.build.CrudSqlBuilder;
import com.gitee.qdbp.jdbc.sql.fragment.CrudFragmentHelper;
import com.gitee.qdbp.jdbc.utils.DbTools;
import com.gitee.qdbp.jdbc.utils.PagingQuery;
import com.gitee.qdbp.tools.utils.ConvertTools;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 基础增删改查数据库操作
 *
 * @author 赵卉华
 * @version 190601
 */
public class EasyCrudDaoImpl<T> implements EasyCrudDao<T> {

    private static Logger log = LoggerFactory.getLogger(EasyCrudDaoImpl.class);

    private Class<T> clazz;
    private CrudSqlBuilder sqlBuilder;
    private ModelDataExecutor modelDataExecutor;
    private SqlBufferJdbcOperations jdbc;

    EasyCrudDaoImpl(Class<T> clazz, CrudSqlBuilder sqlBuilder, ModelDataExecutor modelDataExecutor,
            SqlBufferJdbcOperations baseJdbcOperations) {
        this.clazz = clazz;
        this.sqlBuilder = sqlBuilder;
        this.modelDataExecutor = modelDataExecutor;
        this.jdbc = baseJdbcOperations;
    }

    @Override
    public T findById(String id) {
        if (VerifyTools.isBlank(id)) {
            throw new IllegalArgumentException("id is null");
        }
        PrimaryKey pk = sqlBuilder.helper().getPrimaryKey();
        if (pk == null) {
            throw new UnsupportedOperationException("PrimaryKeyInfoNotFound, UnsupportedFindById, class=" + clazz);
        }
        String primaryField = pk.getFieldName();
        DbWhere where = new DbWhere();
        where.on(primaryField, "=", id);
        return this.find(where);
    }

    @Override
    public T find(DbWhere where) {
        if (where == null || where.isEmpty()) {
            throw new IllegalArgumentException("where can't be empty");
        }
        modelDataExecutor.fillDataEffectiveFlag(where);
        SqlBuffer buffer = sqlBuilder.buildFindSql(where);
        Map<String, Object> map = jdbc.queryForMap(buffer);
        return map == null ? null : DbTools.resultToBean(map, clazz);
    }

    @Override
    public List<T> listAll() {
        return listAll(null);
    }

    @Override
    public List<T> listAll(List<Ordering> orderings) {
        DbWhere where = new DbWhere();
        modelDataExecutor.fillDataEffectiveFlag(where);
        SqlBuffer buffer = sqlBuilder.buildListSql(where, orderings);
        return jdbc.queryForList(buffer, clazz);
    }

    @Override
    public PageList<T> list(DbWhere where, OrderPaging odpg) {
        // DbWhere readyWhere = checkWhere(where); // 带分页查询列表, 允许条件为空, 因此不检查
        DbWhere readyWhere = where;
        if (where == null || where instanceof EmptyDbWhere) {
            readyWhere = new DbWhere();
        }
        modelDataExecutor.fillDataEffectiveFlag(readyWhere);

        // WHERE条件
        SqlBuffer wsb = sqlBuilder.helper().buildWhereSql(readyWhere);
        return this.doList(wsb, odpg);
    }

    private PageList<T> doList(SqlBuffer wsb, OrderPaging odpg) {
        SqlBuffer qsb = sqlBuilder.buildListSql(wsb, odpg.getOrderings());
        SqlBuffer csb = null;
        if (odpg.isPaging() && odpg.isNeedCount()) {
            csb = sqlBuilder.buildCountSql(wsb);
        }

        PartList<T> list = PagingQuery.queryForList(jdbc, qsb, csb, odpg, clazz);
        return list == null ? null : new PageList<T>(list, list.getTotal());
    }

    public <V> V findFieldValue(String fieldName, DbWhere where, Class<V> valueClazz) throws ServiceException {
        DbWhere readyWhere = checkWhere(where);
        modelDataExecutor.fillDataEffectiveFlag(readyWhere);
        List<V> list = doListFieldValues(fieldName, false, readyWhere, null, valueClazz);
        return VerifyTools.isBlank(list) ? null : list.get(0);
    }

    @Override
    public <V> List<V> listFieldValues(String fieldName, boolean distinct, DbWhere where, List<Ordering> orderings,
            Class<V> valueClazz) throws ServiceException {
        DbWhere readyWhere = checkWhere(where);
        modelDataExecutor.fillDataEffectiveFlag(readyWhere);
        return doListFieldValues(fieldName, distinct, readyWhere, null, valueClazz);
    }

    private <V> List<V> doListFieldValues(String fieldName, boolean distinct, DbWhere where, List<Ordering> orderings,
            Class<V> valueClazz) throws ServiceException {
        SqlBuffer buffer = sqlBuilder.buildListFieldValuesSql(fieldName, distinct, where, orderings);
        return jdbc.query(buffer, new FirstColumnMapper<>(valueClazz));
    }

    @Override
    public List<T> listChildren(String startCode, String codeField, String parentField, DbWhere where,
            List<Ordering> orderings) {
        DbWhere readyWhere = checkWhere(where);
        modelDataExecutor.fillDataEffectiveFlag(readyWhere);
        List<String> startCodes = ConvertTools.toList(startCode);
        return doListChildren(startCodes, codeField, parentField, readyWhere, orderings);
    }

    @Override
    public List<T> listChildren(List<String> startCodes, String codeField, String parentField, DbWhere where,
            List<Ordering> orderings) {
        DbWhere readyWhere = checkWhere(where);
        modelDataExecutor.fillDataEffectiveFlag(readyWhere);
        return doListChildren(startCodes, codeField, parentField, readyWhere, orderings);
    }

    private List<T> doListChildren(List<String> startCodes, String codeField, String parentField, DbWhere where,
            List<Ordering> orderings) throws ServiceException {
        CrudFragmentHelper sqlHelper = sqlBuilder.helper();
        List<String> selectFields = sqlHelper.getFieldNames();
        SqlDialect dialect = DbTools.getSqlDialect();
        SqlBuffer buffer = dialect.buildFindChildrenSql(startCodes, codeField, parentField, selectFields, where,
            orderings, sqlHelper);
        return jdbc.queryForList(buffer, clazz);
    }

    @Override
    public List<String> listChildrenCodes(String startCode, String codeField, String parentField, DbWhere where,
            List<Ordering> orderings) {
        DbWhere readyWhere = checkWhere(where);
        modelDataExecutor.fillDataEffectiveFlag(readyWhere);
        List<String> startCodes = ConvertTools.toList(startCode);
        return doListChildrenCodes(startCodes, codeField, parentField, readyWhere, orderings);
    }

    @Override
    public List<String> listChildrenCodes(List<String> startCodes, String codeField, String parentField, DbWhere where,
            List<Ordering> orderings) {
        DbWhere readyWhere = checkWhere(where);
        modelDataExecutor.fillDataEffectiveFlag(readyWhere);
        return doListChildrenCodes(startCodes, codeField, parentField, readyWhere, orderings);
    }

    // ORACLE： START WITH {codeField} IN( {startCode} ) CONNECT BY PRIOR {codeField} = {parentField}
    // DB2: 使用WITH递归 
    // MYSQL 8.0+: 使用WITH RECURSIVE递归 
    // MYSQL 8.0-: 使用存储过程RECURSIVE_FIND_CHILDREN
    private List<String> doListChildrenCodes(List<String> startCodes, String codeField, String parentField,
            DbWhere where, List<Ordering> orderings) throws ServiceException {
        CrudFragmentHelper sqlHelper = sqlBuilder.helper();
        Set<String> selectFields = ConvertTools.toSet(codeField);
        SqlDialect dialect = DbTools.getSqlDialect();
        SqlBuffer buffer = dialect.buildFindChildrenSql(startCodes, codeField, parentField, selectFields, where,
            orderings, sqlHelper);
        return jdbc.query(buffer, FIRST_COLUMN_STRING_MAPPER);
    }

    @Override
    public int count(DbWhere where) throws ServiceException {
        DbWhere readyWhere = checkWhere(where);
        modelDataExecutor.fillDataEffectiveFlag(readyWhere);
        return doCount(readyWhere);
    }

    private int doCount(DbWhere readyWhere) throws ServiceException {
        SqlBuffer buffer = sqlBuilder.buildCountSql(readyWhere);
        return jdbc.queryForObject(buffer, Integer.class);
    }

    @Override
    public Map<String, Integer> groupCount(String groupBy, DbWhere where) throws ServiceException {
        if (VerifyTools.isBlank(groupBy)) {
            throw new IllegalArgumentException("groupBy can't be blank");
        }
        DbWhere readyWhere = checkWhere(where);
        modelDataExecutor.fillDataEffectiveFlag(readyWhere);
        return this.doGroupCount(groupBy, readyWhere);
    }

    private Map<String, Integer> doGroupCount(String groupBy, DbWhere readyWhere) throws ServiceException {
        SqlBuffer buffer = sqlBuilder.buildGroupCountSql(groupBy, readyWhere);
        List<KeyValue<Integer>> list = jdbc.query(buffer, KEY_INTEGER_MAPPER);
        return KeyValue.toMap(list);
    }

    private static KeyIntegerMapper KEY_INTEGER_MAPPER = new KeyIntegerMapper();

    private static FirstColumnMapper<String> FIRST_COLUMN_STRING_MAPPER = new FirstColumnMapper<>(String.class);

    @Override
    public String insert(T entity, boolean fillCreateParams) throws ServiceException {
        if (VerifyTools.isBlank(entity)) {
            throw new IllegalArgumentException("entity can't be empty");
        }
        Map<String, Object> readyEntity = DbTools.beanToMap(entity);
        return insert(readyEntity, fillCreateParams);
    }

    @Override
    public String insert(Map<String, Object> entity, boolean fillCreateParams) throws ServiceException {
        if (VerifyTools.isBlank(entity)) {
            throw new IllegalArgumentException("entity can't be empty");
        }

        String tableName = sqlBuilder.helper().getTableName();
        String id = null;
        // 查找主键
        PrimaryKey pk = sqlBuilder.helper().getPrimaryKey();
        if (pk == null) {
            log.debug("PrimaryKeyInfoNotFound, class={}", clazz);
        } else if (VerifyTools.isNotBlank(entity.get(pk.getFieldName()))) {
            id = entity.get(pk.getFieldName()).toString();
        } else {
            // 生成主键
            entity.put(pk.getFieldName(), id = modelDataExecutor.generatePrimaryKeyCode(tableName));
        }
        modelDataExecutor.fillDataEffectiveFlag(entity);
        if (fillCreateParams) {
            modelDataExecutor.fillCreteParams(entity);
        }

        SqlBuffer buffer = sqlBuilder.buildInsertSql(entity);

        jdbc.update(buffer);
        return id;
    }

    @Override
    public int update(T entity, boolean fillUpdateParams, boolean errorOnUnaffected) throws ServiceException {
        // 查找主键
        PrimaryKey pk = sqlBuilder.helper().getPrimaryKey();
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

        if (VerifyTools.isBlank(readyEntity)) {
            throw new IllegalArgumentException("entity can't be empty");
        }

        modelDataExecutor.fillDataEffectiveFlag(where);
        if (fillUpdateParams) {
            modelDataExecutor.fillUpdateParams(readyEntity);
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

        modelDataExecutor.fillDataEffectiveFlag(readyWhere);
        if (fillUpdateParams) {
            modelDataExecutor.fillUpdateParams(readyEntity);
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
        modelDataExecutor.fillDataEffectiveFlag(readyWhere);
        if (fillUpdateParams) {
            modelDataExecutor.fillUpdateParams(entity);
        }
        return this.doUpdate(entity, readyWhere, errorOnUnaffected);
    }

    private int doUpdate(DbUpdate readyEntity, DbWhere readyWhere, boolean errorOnUnaffected) throws ServiceException {
        SqlBuffer buffer = sqlBuilder.buildUpdateSql(readyEntity, readyWhere);

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
        if (VerifyTools.isBlank(ids)) {
            throw new IllegalArgumentException("ids can't be empty");
        }
        PrimaryKey pk = sqlBuilder.helper().getPrimaryKey();
        if (pk == null) {
            throw new UnsupportedOperationException("PrimaryKeyInfoNotFound, UnsupportedDeleteById, class=" + clazz);
        }

        String primaryField = pk.getFieldName();
        DbWhere where = new DbWhere();
        where.on(primaryField, "in", ids);
        modelDataExecutor.fillDataEffectiveFlag(where);
        return this.doDelete(where, true, fillUpdateParams, errorOnUnaffected);
    }

    @Override
    public int logicalDelete(T where, boolean fillUpdateParams, boolean errorOnUnaffected) throws ServiceException {
        DbWhere readyWhere = DbTools.parseWhereFromEntity(where);
        if (VerifyTools.isBlank(readyWhere)) {
            throw new IllegalArgumentException("where can't be empty, please use logicalDeleteAll()");
        }
        modelDataExecutor.fillDataEffectiveFlag(readyWhere);
        return this.doDelete(readyWhere, true, fillUpdateParams, errorOnUnaffected);
    }

    @Override
    public int logicalDelete(DbWhere where, boolean fillUpdateParams, boolean errorOnUnaffected)
            throws ServiceException {
        DbWhere readyWhere = checkWhere(where);
        modelDataExecutor.fillDataEffectiveFlag(readyWhere);
        return this.doDelete(readyWhere, true, fillUpdateParams, errorOnUnaffected);
    }

    @Override
    public int physicalDeleteByIds(List<String> ids, boolean errorOnUnaffected) throws ServiceException {
        if (VerifyTools.isBlank(ids)) {
            throw new IllegalArgumentException("ids can't be empty");
        }
        PrimaryKey pk = sqlBuilder.helper().getPrimaryKey();
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
            if (modelDataExecutor.containsLogicalDeleteFlag()) { // 支持逻辑删除
                DbUpdate ud = new DbUpdate();
                modelDataExecutor.fillDataIneffectiveFlag(ud);
                if (fillUpdateParams) {
                    modelDataExecutor.fillUpdateParams(ud);
                }
                buffer = sqlBuilder.buildUpdateSql(ud, readyWhere);
            } else { // 不支持逻辑删除
                throw new ServiceException(DbErrorCode.UNSUPPORTED_LOGICAL_DELETE);
            }
        } else {
            buffer = sqlBuilder.buildDeleteSql(readyWhere);
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

    private DbWhere checkWhere(DbWhere where) {
        if (where == null || (where.isEmpty() && !(where instanceof EmptyDbWhere))) {
            throw new IllegalArgumentException("where can't be empty, please use DbWhere.NONE");
        } else if (where instanceof EmptyDbWhere) {
            return new DbWhere();
        } else {
            return where;
        }
    }

}
