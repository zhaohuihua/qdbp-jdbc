package com.gitee.qdbp.jdbc.biz;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import com.alibaba.fastjson.util.TypeUtils;
import com.gitee.qdbp.able.beans.KeyValue;
import com.gitee.qdbp.able.exception.ServiceException;
import com.gitee.qdbp.able.model.ordering.OrderPaging;
import com.gitee.qdbp.able.model.ordering.Ordering;
import com.gitee.qdbp.able.model.paging.PartList;
import com.gitee.qdbp.jdbc.api.BaseCrudDao;
import com.gitee.qdbp.jdbc.api.SqlBufferJdbcOperations;
import com.gitee.qdbp.jdbc.condition.DbField;
import com.gitee.qdbp.jdbc.condition.DbUpdate;
import com.gitee.qdbp.jdbc.condition.DbWhere;
import com.gitee.qdbp.jdbc.condition.DbWhere.EmptyDbWhere;
import com.gitee.qdbp.jdbc.exception.DbErrorCode;
import com.gitee.qdbp.jdbc.model.PrimaryKey;
import com.gitee.qdbp.jdbc.plugins.ModelDataExecutor;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.SqlBuilder;
import com.gitee.qdbp.jdbc.utils.DbTools;
import com.gitee.qdbp.tools.utils.ConvertTools;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 基础增删改查数据库操作
 *
 * @author 赵卉华
 * @version 190601
 */
public class BaseCrudDaoImpl<T> implements BaseCrudDao<T> {

    private static Logger log = LoggerFactory.getLogger(BaseCrudDaoImpl.class);

    private Class<T> clazz;
    private SqlBuilder sqlBuilder;
    private ModelDataExecutor modelDataExecutor;
    private SqlBufferJdbcOperations baseJdbcOperations;

    BaseCrudDaoImpl(Class<T> clazz, SqlBuilder sqlBuilder, ModelDataExecutor modelDataExecutor,
            SqlBufferJdbcOperations baseJdbcOperations) {
        this.clazz = clazz;
        this.sqlBuilder = sqlBuilder;
        this.modelDataExecutor = modelDataExecutor;
        this.baseJdbcOperations = baseJdbcOperations;
    }

    @Override
    public T findById(String id) {
        if (VerifyTools.isBlank(id)) {
            throw new IllegalArgumentException("id is null");
        }

        String primaryField = sqlBuilder.getPrimaryKey().getFieldName();
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
        SqlBuffer buffer = new SqlBuffer();
        // SELECT ... FROM
        buffer.append("SELECT").append(' ', sqlBuilder.buildFieldsSql());
        buffer.append(' ', "FROM").append(' ', sqlBuilder.getTableName());
        // WHERE ...
        buffer.append(' ', sqlBuilder.buildWhereSql(where));

        Map<String, Object> map = baseJdbcOperations.queryForMap(buffer);
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
        SqlBuffer buffer = new SqlBuffer();
        // SELECT ... FROM
        buffer.append("SELECT");
        buffer.append(' ', sqlBuilder.buildFieldsSql());
        buffer.append(' ', "FROM").append(' ', sqlBuilder.getTableName());
        // WHERE ...
        buffer.append(' ', sqlBuilder.buildWhereSql(where));
        if (VerifyTools.isNotBlank(orderings)) {
            buffer.append(' ', sqlBuilder.buildOrderBySql(orderings));
        }

        return baseJdbcOperations.queryForList(buffer, clazz);
    }

    @Override
    public PartList<T> list(DbWhere where, OrderPaging odpg) {
        // DbWhere readyWhere = checkWhere(where); // 带分页查询列表, 允许条件为空, 因此不检查
        DbWhere readyWhere = where;
        if (where == null || where instanceof EmptyDbWhere) {
            readyWhere = new DbWhere();
        }
        modelDataExecutor.fillDataEffectiveFlag(readyWhere);

        // WHERE条件
        SqlBuffer wsb = sqlBuilder.buildWhereSql(readyWhere);
        return this.doList(wsb, odpg);
    }

    private PartList<T> doList(SqlBuffer wsb, OrderPaging odpg) {
        SqlBuffer qsb = new SqlBuffer();
        // SELECT ... FROM
        qsb.append("SELECT").append(' ', sqlBuilder.buildFieldsSql());
        qsb.append(' ', "FROM").append(' ', sqlBuilder.getTableName());
        // WHERE ...
        qsb.append(' ', wsb);

        // ORDER BY ...
        List<Ordering> orderings = odpg.getOrderings();
        if (VerifyTools.isNotBlank(orderings)) {
            qsb.append(' ', sqlBuilder.buildOrderBySql(orderings));
        }

        SqlBuffer csb = new SqlBuffer();
        if (odpg.isPaging() && odpg.isNeedCount()) {
            // SELECT COUNT(*) FROM
            csb.append("SELECT").append(' ', "COUNT(*)").append(' ', "FROM").append(' ', sqlBuilder.getTableName());
            // WHERE ...
            csb.append(' ', wsb);
        }

        return baseJdbcOperations.queryForList(qsb, csb, odpg, sqlBuilder.getDialect(), clazz);
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
        SqlBuffer buffer = new SqlBuffer();
        // SELECT ... FROM
        buffer.append("SELECT", ' ');
        if (distinct) {
            buffer.append("DISTINCT", ' ');
        }
        buffer.append(sqlBuilder.buildFieldsSql(fieldName));
        buffer.append(' ', "FROM").append(' ', sqlBuilder.getTableName());
        // WHERE ...
        buffer.append(' ', sqlBuilder.buildWhereSql(where));
        // ORDER BY ...
        if (VerifyTools.isNotBlank(orderings)) {
            buffer.append(' ', sqlBuilder.buildOrderBySql(orderings));
        }
        return baseJdbcOperations.query(buffer, new FirstColumnMapper<>(valueClazz));
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
        Set<String> selectFields = sqlBuilder.getFieldColumnMap().keySet();
        final SqlBuffer buffer = sqlBuilder.getDialect().buildFindChildrenSql(startCodes, codeField, parentField,
            selectFields, where, orderings, sqlBuilder);
        return baseJdbcOperations.queryForList(buffer, clazz);
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
        Set<String> selectFields = ConvertTools.toSet(codeField);
        SqlBuffer buffer = sqlBuilder.getDialect().buildFindChildrenSql(startCodes, codeField, parentField,
            selectFields, where, orderings, sqlBuilder);
        return baseJdbcOperations.query(buffer, FIRST_COLUMN_STRING_MAPPER);
    }

    @Override
    public int count(DbWhere where) throws ServiceException {
        DbWhere readyWhere = checkWhere(where);
        modelDataExecutor.fillDataEffectiveFlag(readyWhere);
        return doCount(readyWhere);
    }

    private int doCount(DbWhere readyWhere) throws ServiceException {
        SqlBuffer buffer = new SqlBuffer();
        // SELECT ...
        buffer.append("SELECT").append(' ', "COUNT(*)").append(' ', "FROM").append(' ', sqlBuilder.getTableName());
        // WHERE ...
        buffer.append(' ', sqlBuilder.buildWhereSql(readyWhere));

        return baseJdbcOperations.queryForObject(buffer, Integer.class);
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

        // 字段列表
        SqlBuffer fields = sqlBuilder.buildFieldsSql(groupBy);

        SqlBuffer buffer = new SqlBuffer();
        // SELECT ... FROM
        buffer.append("SELECT");
        buffer.append(' ', fields).append(',').append("COUNT(*)");
        buffer.append(' ', "FROM").append(' ', sqlBuilder.getTableName());
        // WHERE ...
        buffer.append(' ', sqlBuilder.buildWhereSql(readyWhere));
        // GROUP BY ...
        buffer.append(' ', "GROUP BY").append(' ', fields);

        List<KeyValue<Integer>> list = baseJdbcOperations.query(buffer, KEY_INTEGER_MAPPER);
        return KeyValue.toMap(list);
    }

    private static KeyIntegerMapper KEY_INTEGER_MAPPER = new KeyIntegerMapper();

    public static class KeyIntegerMapper implements RowMapper<KeyValue<Integer>> {

        @Override
        public KeyValue<Integer> mapRow(ResultSet rs, int rowNum) throws SQLException {
            KeyValue<Integer> item = new KeyValue<>();
            item.setKey(rs.getString(1));
            item.setValue(rs.getInt(2));
            return item;
        }
    }

    private static FirstColumnMapper<String> FIRST_COLUMN_STRING_MAPPER = new FirstColumnMapper<>(String.class);

    /**
     * 获取第1列数据
     *
     * @author zhaohuihua
     * @version 190601
     */
    public static class FirstColumnMapper<T> implements RowMapper<T> {

        private Class<T> clazz;

        public FirstColumnMapper(Class<T> clazz) {
            this.clazz = clazz;
        }

        @Override
        public T mapRow(ResultSet rs, int rowNum) throws SQLException {
            // DruidPooledResultSet.getObject() SQLFeatureNotSupportedException
            // return rs.getObject(1, clazz);
            Object object = rs.getObject(1);
            return TypeUtils.castToJavaBean(object, clazz);
        }
    }

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

        String tableName = sqlBuilder.getTableName();
        String id = null;
        // 通过注解查找主键生成方式
        PrimaryKey pk = sqlBuilder.getPrimaryKey();
        if (pk == null) {
            log.debug("PrimaryKeyInfoNotFound, class={}", entity.getClass());
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

        Set<String> fieldNames = entity.keySet();
        SqlBuffer valuesSqlBuffer = sqlBuilder.buildInsertValuesSql(entity);
        SqlBuffer fieldsSqlBuffer = sqlBuilder.buildFieldsSql(fieldNames);

        SqlBuffer buffer = new SqlBuffer();
        // INSERT INTO (...)
        buffer.append("INSERT INTO").append(' ', tableName).append(' ');
        buffer.append('(');
        buffer.append(fieldsSqlBuffer);
        buffer.append(')');
        // VALUES (...)
        buffer.append(' ', "VALUES", ' ').append('(');
        buffer.append(valuesSqlBuffer);
        buffer.append(')');

        baseJdbcOperations.update(buffer);
        return id;
    }

    @Override
    public int update(T entity, boolean fillUpdateParams, boolean errorOnUnaffected) throws ServiceException {
        DbUpdate readyEntity = DbTools.parseUpdateFromEntity(entity);

        DbWhere where = new DbWhere();
        // 查找主键
        PrimaryKey pk = sqlBuilder.getPrimaryKey();
        if (pk == null) {
            log.debug("PrimaryKeyAnnotationNotFound, class={}", entity.getClass());
            throw new ServiceException(DbErrorCode.DB_PRIMARY_KEY_FIELD_NOT_FOUND);
        }
        List<DbField> temp = readyEntity.fields(pk.getFieldName());
        String id = null;
        if (temp != null && !temp.isEmpty()) {
            Object fieldValue = temp.get(0).getFieldValue();
            id = fieldValue == null ? null : fieldValue.toString();
        }
        // 主键不能为空
        if (VerifyTools.isBlank(id)) {
            log.debug("PrimaryKeyValueIsBlank, class={}", entity.getClass());
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
        SqlBuffer buffer = new SqlBuffer();
        buffer.append("UPDATE").append(' ', sqlBuilder.getTableName());
        buffer.append(' ', sqlBuilder.buildUpdateSetSql(readyEntity));

        if (VerifyTools.isNotBlank(readyWhere)) {
            buffer.append(' ', sqlBuilder.buildWhereSql(readyWhere));
        }

        int rows = baseJdbcOperations.update(buffer);

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

        String primaryField = sqlBuilder.getPrimaryKey().getFieldName();
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

        String primaryField = sqlBuilder.getPrimaryKey().getFieldName();
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
        SqlBuffer buffer = new SqlBuffer();
        if (logical) {
            if (modelDataExecutor.containsLogicalDeleteFlag()) { // 支持逻辑删除
                DbUpdate ud = new DbUpdate();
                modelDataExecutor.fillDataIneffectiveFlag(ud);
                if (fillUpdateParams) {
                    modelDataExecutor.fillUpdateParams(ud);
                }
                buffer.append("UPDATE").append(' ', sqlBuilder.getTableName());
                buffer.append(' ', sqlBuilder.buildUpdateSetSql(ud));
            } else { // 不支持逻辑删除
                throw new ServiceException(DbErrorCode.UNSUPPORTED_LOGICAL_DELETE);
            }
        } else {
            buffer.append("DELETE").append(' ', "FROM").append(' ', sqlBuilder.getTableName());
        }
        buffer.append(' ', sqlBuilder.buildWhereSql(readyWhere));

        int rows = baseJdbcOperations.update(buffer);

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
