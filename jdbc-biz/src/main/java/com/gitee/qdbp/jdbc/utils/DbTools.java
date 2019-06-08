package com.gitee.qdbp.jdbc.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.jdbc.core.JdbcOperations;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;
import com.gitee.qdbp.jdbc.condition.DbUpdate;
import com.gitee.qdbp.jdbc.condition.DbWhere;
import com.gitee.qdbp.jdbc.condition.TableJoin;
import com.gitee.qdbp.jdbc.condition.TableJoin.JoinItem;
import com.gitee.qdbp.jdbc.condition.TableJoin.TableItem;
import com.gitee.qdbp.jdbc.fields.AllFields;
import com.gitee.qdbp.jdbc.model.DbVersion;
import com.gitee.qdbp.jdbc.model.FieldColumn;
import com.gitee.qdbp.jdbc.model.PrimaryKey;
import com.gitee.qdbp.jdbc.plugins.DbPluginContainer;
import com.gitee.qdbp.jdbc.plugins.DbVersionFinder;
import com.gitee.qdbp.jdbc.plugins.ModelDataExecutor;
import com.gitee.qdbp.jdbc.plugins.ModelDataHandler;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.plugins.SqlFormatter;
import com.gitee.qdbp.jdbc.plugins.TableInfoScans;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.build.CrudSqlBuilder;
import com.gitee.qdbp.jdbc.sql.build.QuerySqlBuilder;
import com.gitee.qdbp.jdbc.sql.fragment.CrudFragmentHelper;
import com.gitee.qdbp.jdbc.sql.fragment.TableCrudFragmentHelper;
import com.gitee.qdbp.jdbc.sql.fragment.TableJoinFragmentHelper;
import com.gitee.qdbp.tools.utils.ConvertTools;
import com.gitee.qdbp.tools.utils.StringTools;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 数据库公共工具类
 *
 * @author 赵卉华
 * @version 190601
 */
public abstract class DbTools {

    /** Entity的表名缓存 **/
    private static Map<Class<?>, String> entityTableNameCache = new ConcurrentHashMap<>();

    /**
     * 获取实体业务执行接口
     * 
     * @param clazz
     * @return 实体业务执行类
     */
    public static ModelDataExecutor getModelDataExecutor(Class<?> clazz) {
        List<FieldColumn> columns = parseFieldColumns(clazz);
        if (VerifyTools.isBlank(columns)) {
            throw new IllegalArgumentException("columns is empty");
        }
        ModelDataHandler handler = DbPluginContainer.global.getModelDataHandler();
        return new ModelDataExecutor(columns, handler);
    }

    /**
     * 获取实体业务执行接口
     * 
     * @return 实体业务执行类
     */
    public static ModelDataExecutor getModelDataExecutor(TableJoin tables) {
        List<FieldColumn> columns = parseFieldColumns(tables);
        if (VerifyTools.isBlank(columns)) {
            throw new IllegalArgumentException("columns is empty");
        }
        ModelDataHandler handler = DbPluginContainer.global.getModelDataHandler();
        return new ModelDataExecutor(columns, handler);
    }

    public static CrudSqlBuilder getCrudSqlBuilder(Class<?> clazz) {
        CrudFragmentHelper sqlHelper = new TableCrudFragmentHelper(clazz);
        return new CrudSqlBuilder(sqlHelper);
    }

    public static QuerySqlBuilder getCrudSqlBuilder(TableJoin tables) {
        TableJoinFragmentHelper sqlHelper = new TableJoinFragmentHelper(tables);
        return new QuerySqlBuilder(sqlHelper);
    }

    /**
     * 获取数据库方言处理类
     * 
     * @return 方言处理类
     */
    public static SqlDialect getSqlDialect() {
        return DbPluginContainer.global.getSqlDialect();
    }

    /**
     * 格式化SQL语句
     * 
     * @param sql 待格式化的SQL语句
     * @param indent 缩进层数
     * @return 已格式化的SQL语句
     */
    public static String formatSql(String sql, int indent) {
        SqlFormatter formatter = DbPluginContainer.global.getSqlFormatter();
        return formatter.format(sql, indent);
    }

    /**
     * 格式化SQL语句
     * 
     * @param sql 待格式化的SQL语句
     * @param indent 缩进层数
     * @return 已格式化的SQL语句
     */
    public static String formatSql(SqlBuffer sql, int indent) {
        return formatSql(sql.toString(), indent);
    }

    /**
     * 查找数据库版本信息
     * 
     * @param jdbcOperations JDBC操作类
     * @return 数据库版本信息
     */
    public static DbVersion findDbVersion(JdbcOperations jdbcOperations) {
        DbVersionFinder finder = DbPluginContainer.global.getDbVersionFinder();
        return finder.findDbVersion(jdbcOperations);
    }

    /**
     * 扫描表名信息
     * 
     * @param clazz 类名
     * @return 表名
     */
    public static String parseTableName(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("clazz is null");
        }
        if (entityTableNameCache.containsKey(clazz)) {
            return entityTableNameCache.get(clazz);
        }

        TableInfoScans scans = DbPluginContainer.global.getTableInfoScans();
        String tableName = scans.scanTableName(clazz);
        entityTableNameCache.put(clazz, tableName);
        return tableName;
    }

    /** Entity的主键缓存 **/
    private static Map<Class<?>, PrimaryKey> entityPrimaryKeyCache = new ConcurrentHashMap<>();

    /**
     * 扫描获取主键
     * 
     * @param clazz 类名
     * @return 主键
     */
    public static PrimaryKey parsePrimaryKey(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("clazz is null");
        }
        if (entityPrimaryKeyCache.containsKey(clazz)) {
            return entityPrimaryKeyCache.get(clazz);
        }

        TableInfoScans scans = DbPluginContainer.global.getTableInfoScans();
        PrimaryKey pk = scans.scanPrimaryKey(clazz);
        entityPrimaryKeyCache.put(clazz, pk);
        return pk;
    }

    /** TableJoin的列名缓存 **/
    private static Map<String, List<FieldColumn>> joinColumnsCache = new ConcurrentHashMap<>();

    public static String buildCacheKey(TableJoin tables) {
        StringBuilder buffer = new StringBuilder();
        TableItem major = tables.getMajor();
        buffer.append(parseTableName(major.getTableType()));
        if (VerifyTools.isNotBlank(major.getTableAlias())) {
            buffer.append(' ').append(major.getTableAlias().toUpperCase());
        }
        List<JoinItem> joins = tables.getJoins();
        if (VerifyTools.isNotBlank(joins)) {
            for (JoinItem item : joins) {
                buffer.append('-').append(parseTableName(item.getTableType()));
                if (VerifyTools.isNotBlank(item.getTableAlias())) {
                    buffer.append(' ').append(item.getTableAlias().toUpperCase());
                }
            }
        }
        return buffer.toString();
    }

    private static List<FieldColumn> scanColumnList(TableItem table) {
        TableInfoScans scans = DbPluginContainer.global.getTableInfoScans();
        List<FieldColumn> fields = scans.scanColumnList(table.getTableType());
        String tableAlias = table.getTableAlias();
        if (VerifyTools.isNotBlank(tableAlias)) {
            for (FieldColumn item : fields) {
                item.setTableAlias(tableAlias);
            }
        }
        return fields;
    }

    /**
     * 扫描获取字段名和数据库列名的映射表
     * 
     * @param tables 表关联对象
     * @return AllFields: fieldName - columnName
     */
    public static List<FieldColumn> parseFieldColumns(TableJoin tables) {
        if (tables == null) {
            throw new IllegalArgumentException("tables is null");
        }
        String cacheKey = buildCacheKey(tables);
        if (joinColumnsCache.containsKey(cacheKey)) {
            return joinColumnsCache.get(cacheKey);
        }
        TableInfoScans scans = DbPluginContainer.global.getTableInfoScans();
        TableItem major = tables.getMajor();
        List<JoinItem> joins = tables.getJoins();
        List<FieldColumn> all = new ArrayList<>();
        { // 添加主表的字段
            List<FieldColumn> fields = scanColumnList(major);
            all.addAll(fields);
        }
        if (VerifyTools.isNotBlank(joins)) {
            // 添加关联表的字段
            for (JoinItem item : joins) {
                List<FieldColumn> fields = scans.scanColumnList(item.getTableType());
                all.addAll(fields);
            }
        }
        // 处理重名字段: 设置columnAlias
        // 1.先统计字段出现次数
        Map<String, Integer> countMaps = new HashMap<>();
        for (FieldColumn field : all) {
            String fieldName = field.getFieldName();
            if (countMaps.containsKey(fieldName)) {
                countMaps.put(fieldName, countMaps.get(fieldName) + 1);
            } else {
                countMaps.put(fieldName, 1);
            }
        }
        // 2.如果出现多次则设置columnAlias=tableAlias_columnName
        for (FieldColumn field : all) {
            String fieldName = field.getFieldName();
            if (countMaps.get(fieldName) > 1) {
                String columnAlias = StringTools.concat('_', field.getTableAlias(), field.getColumnName());
                field.setColumnAlias(columnAlias);
            }
        }
        joinColumnsCache.put(cacheKey, all);
        return all;
    }

    /**
     * 扫描获取字段名和数据库列名的映射表
     * 
     * @param tables 表关联对象
     * @return AllFields: fieldName - columnName
     */
    public static AllFields parseToAllFields(TableJoin tables) {
        return new AllFields(parseFieldColumns(tables));
    }

    /** Entity的列名缓存 **/
    private static Map<Class<?>, List<FieldColumn>> entityColumnsCache = new ConcurrentHashMap<>();

    /**
     * 扫描获取字段名和数据库列名的映射表
     * 
     * @param clazz 类型
     * @return ColumnInfo: fieldName - columnName
     */
    public static List<FieldColumn> parseFieldColumns(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("clazz is null");
        }
        if (entityColumnsCache.containsKey(clazz)) {
            return entityColumnsCache.get(clazz);
        }

        TableInfoScans scans = DbPluginContainer.global.getTableInfoScans();
        List<FieldColumn> all = scans.scanColumnList(clazz);
        entityColumnsCache.put(clazz, all);
        return all;
    }

    /**
     * 扫描获取字段名和数据库列名的映射表
     * 
     * @param clazz 类型
     * @return AllFields: fieldName - columnName
     */
    public static AllFields parseToAllFields(Class<?> clazz) {
        return new AllFields(parseFieldColumns(clazz));
    }

    /**
     * 扫描获取字段名和数据库列名的映射表
     * 
     * @param clazz 类型
     * @return map: fieldName - columnName
     */
    public static Map<String, String> parseFieldColumnMap(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }

        List<FieldColumn> columns = parseFieldColumns(clazz);
        return toFieldColumnMap(columns);
    }

    /**
     * 列表转换为Field-Column映射表
     * 
     * @param columns 字段列表信息
     * @return Field-Column映射表
     */
    public static Map<String, String> toFieldColumnMap(List<FieldColumn> columns) {
        if (columns == null) {
            return null;
        }
        Map<String, String> map = new LinkedHashMap<String, String>();
        for (FieldColumn item : columns) {
            map.put(item.getFieldName(), item.getColumnName());
        }
        return map;
    }

    /**
     * 扫描获取数据库列名和字段名的映射表
     * 
     * @param clazz 类型
     * @return map: columnName - fieldName
     */
    public static Map<String, String> parseColumnFieldMap(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }

        List<FieldColumn> columns = parseFieldColumns(clazz);
        return toColumnFieldMap(columns);
    }

    /**
     * 列表转换为Field-Column映射表
     * 
     * @param columns 字段列表信息
     * @return Column-Field映射表
     */
    public static Map<String, String> toColumnFieldMap(List<FieldColumn> columns) {
        if (columns == null) {
            return null;
        }
        Map<String, String> map = new LinkedHashMap<String, String>();
        for (FieldColumn item : columns) {
            map.put(item.getColumnName(), item.getFieldName());
        }
        return map;
    }

    /**
     * 将Java对象转换为Map, 只保留有列信息的字段
     * 
     * @param object Java对象
     * @return Map对象
     */
    public static Map<String, Object> beanToMap(Object object) {
        if (object == null) {
            return null;
        }
        Map<String, Object> map = doBeanToMap(object, true); // JsonTools.beanToMap(object, true);
        if (VerifyTools.isBlank(map)) {
            return map;
        }

        // 从bean.getClass()扫描获取列名与字段名的对应关系
        List<FieldColumn> columns = DbTools.parseFieldColumns(object.getClass());
        if (columns == null || columns.isEmpty()) {
            return null;
        }
        Map<String, String> fieldColumnMaps = DbTools.toFieldColumnMap(columns);

        // 只保留有列注解的字段
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (fieldColumnMaps.containsKey(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    private static Map<String, Object> doBeanToMap(Object object, boolean clearBlankValue) {
        if (object == null) {
            return null;
        }
        Map<String, Object> map = (JSONObject) JSON.toJSON(object);
        return clearBlankValue ? ConvertTools.clearBlankValue(map, false) : map;
    }

    /**
     * 将DB查询的ResultMap转换为Java对象<br>
     * result的key是数据表的列名, 会根据class的注解转换为字段名
     * 
     * @param result 数据
     * @param clazz 目标类型
     * @return Java对象
     * @author 赵卉华
     */
    public static <T> T resultToBean(Map<String, Object> result, Class<T> clazz) {
        if (result == null || clazz == null) {
            return null;
        }

        // 1. 从bean.getClass()通过注释获取列名与字段名的对应关系
        List<FieldColumn> columns = DbTools.parseFieldColumns(clazz);
        if (columns == null || columns.isEmpty()) {
            return null;
        }

        Map<String, String> columnFieldMaps = DbTools.toColumnFieldMap(columns);
        // 2. properties是列名与字段值的对应关系, 转换为字段名与字段值的对应关系
        Map<String, Object> fieldValues = new HashMap<String, Object>();
        for (Map.Entry<String, Object> entry : result.entrySet()) {
            String columnName = entry.getKey();
            if (columnFieldMaps.containsKey(columnName)) {
                String fieldName = columnFieldMaps.get(columnName);
                fieldValues.put(fieldName, entry.getValue());
            }
        }
        // 3. 利用fastjson工具进行Map到JavaObject的转换
        return TypeUtils.castToJavaBean(result, clazz);
    }

    /** 分页/排序对象的通用字段 **/
    private static List<String> COMMON_FIELDS = Arrays.asList("_", "extra", "offset", "pageSize", "skip", "rows",
        "page", "needCount", "paging", "ordering");
    /** 允许数组的字段名后缀 **/
    private static List<String> WHERE_ARRAY_FIELDS = Arrays.asList("In", "NotIn", "Between", "NotBetween");

    /**
     * 将Java对象转换为Where对象
     * 
     * @param entity Java对象
     * @return Where对象
     */
    public static DbWhere parseWhereFromEntity(Object entity) {
        if (entity == null) {
            return null;
        }
        Map<String, Object> map = (JSONObject) JSON.toJSON(entity);
        return DbWhere.from(map);
    }

    /**
     * 将Java对象转换为Update对象
     * 
     * @param entity Java对象
     * @return Update对象
     */
    public static DbUpdate parseUpdateFromEntity(Object entity) {
        if (entity == null) {
            return null;
        }
        Map<String, Object> map = (JSONObject) JSON.toJSON(entity);
        return DbUpdate.from(map);
    }

    /**
     * 从请求参数中构建Where对象<br>
     * 只会包含clazz注解中通过@JoyInColumn指定的字段名
     * 
     * @param params 请求参数
     * @param clazz 实体类
     * @return Where对象
     */
    public static <T> DbWhere parseWhereFromParams(Map<String, String[]> params, Class<T> clazz) {
        List<FieldColumn> columns = DbTools.parseFieldColumns(clazz);
        Map<String, String> fieldColumnMap = DbTools.toFieldColumnMap(columns);
        List<String> fieldNames = new ArrayList<String>(fieldColumnMap.keySet());
        Map<String, Object> map = parseMapWithWhitelist(params, fieldNames, WHERE_ARRAY_FIELDS);
        return DbWhere.from(map);
    }

    /**
     * 从请求参数中构建Where对象<br>
     * <pre>
     * 转换规则:
        fieldName$Equals(=), fieldName$NotEquals(!=), 
        fieldName$LessThen(<), fieldName$LessEqualsThen(<=), 
        fieldName$GreaterThen(>), fieldName$GreaterEqualsThen(>=), 
        fieldName$IsNull, fieldName$IsNotNull, 
        fieldName$Like, fieldName$NotLike, fieldName$Starts, fieldName$Ends, 
        fieldName$In, fieldName$NotIn, fieldName$Between
     * </pre>
     * 
     * @param params 请求参数
     * @param excludeDefault 是否排除默认的公共字段<br>
     *            extra, offset, pageSize, skip, rows, page, needCount, paging, orderings
     * @param excludeFields 排除的字段名, optional
     * @return Where对象
     */
    public static DbWhere parseWhereFromParams(Map<String, String[]> params, boolean excludeDefault,
            String... excludeFields) {
        List<String> realExcludeFields = new ArrayList<String>();
        if (excludeDefault) {
            realExcludeFields.addAll(COMMON_FIELDS);
        }
        if (VerifyTools.isNotBlank(excludeFields)) {
            for (String string : excludeFields) {
                realExcludeFields.add(string);
            }
        }
        Map<String, Object> map = parseMapWithBlacklist(params, realExcludeFields, WHERE_ARRAY_FIELDS);
        return DbWhere.from(map);
    }

    /**
     * 从请求参数中构建Update对象<br>
     * 只会包含clazz注解中通过@JoyInColumn指定的字段名 <pre>
     * 转换规则:
        fieldName 或 fieldName$Equals(=)
        fieldName$Add(增加值)
        fieldName$ToNull(转换为空)
     * </pre>
     * 
     * @param params 请求参数
     * @param clazz 实体类
     * @return Update对象
     */
    public static <T> DbUpdate parseUpdateFromParams(Map<String, String[]> params, Class<T> clazz) {
        List<FieldColumn> columns = DbTools.parseFieldColumns(clazz);
        Map<String, String> fieldColumnMap = DbTools.toFieldColumnMap(columns);
        List<String> fieldNames = new ArrayList<String>(fieldColumnMap.keySet());
        Map<String, Object> map = parseMapWithWhitelist(params, fieldNames, null);
        return DbUpdate.from(map);
    }

    /**
     * 从请求参数中构建Update对象
     * 
     * @param params 请求参数
     * @param excludeDefault 是否排除默认的公共字段<br>
     *            extra, offset, pageSize, skip, rows, page, needCount, paging, orderings
     * @param excludeFields 排除的字段名, optional
     * @return Update对象
     */
    public static DbUpdate parseUpdateFromParams(Map<String, String[]> params, boolean excludeDefault,
            String... excludeFields) {
        List<String> realExcludeFields = new ArrayList<String>();
        if (excludeDefault) {
            realExcludeFields.addAll(COMMON_FIELDS);
        }
        if (VerifyTools.isNotBlank(excludeFields)) {
            for (String string : excludeFields) {
                realExcludeFields.add(string);
            }
        }
        Map<String, Object> map = parseMapWithBlacklist(params, realExcludeFields, null);
        return DbUpdate.from(map);
    }

    /**
     * 将请求参数转换为Map对象
     * 
     * @param params 请求参数, required
     * @param excludeFields 排除的字段名, optional
     * @param allowArraySuffixes 允许数组的字段名后缀, optional
     * @return Map对象
     */
    public static Map<String, Object> parseMapWithBlacklist(Map<String, String[]> params, List<String> excludeFields,
            List<String> allowArraySuffixes) {
        if (params == null) {
            return null;
        }

        // 需要排除的字段名
        Map<String, Void> blacklistMap = new HashMap<String, Void>();
        if (VerifyTools.isNotBlank(excludeFields)) {
            for (String field : excludeFields) {
                blacklistMap.put(field, null);
            }
        }

        Map<String, Object> resultMap = new HashMap<String, Object>();
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            if (VerifyTools.isAnyBlank(entry.getKey(), entry.getValue())) {
                continue;
            }
            if (blacklistMap.containsKey(entry.getKey())) {
                continue;
            }
            String fieldName = entry.getKey();
            if (fieldName.endsWith("[]")) {
                fieldName = fieldName.substring(0, fieldName.length() - 2);
            }
            if (allowArraySuffixes != null && isEndsWith(fieldName, allowArraySuffixes)) {
                resultMap.put(fieldName, entry.getValue());
            } else {
                resultMap.put(fieldName, entry.getValue()[0]);
            }
        }
        return resultMap;
    }

    /**
     * 将请求参数转换为Map对象
     * 
     * @param params 请求参数, required
     * @param includeFields 有效的字段名, required
     * @param allowArraySuffixes 允许数组的字段名后缀, optional
     * @return Map对象
     */
    public static Map<String, Object> parseMapWithWhitelist(Map<String, String[]> params, List<String> includeFields,
            List<String> allowArraySuffixes) {
        if (params == null) {
            return null;
        }

        // 有效的字段名
        Map<String, Void> whitelistMap = new HashMap<String, Void>();
        if (VerifyTools.isNotBlank(includeFields)) {
            for (String field : includeFields) {
                whitelistMap.put(field, null);
            }
        }

        Map<String, Object> resultMap = new HashMap<String, Object>();
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            if (VerifyTools.isAnyBlank(entry.getKey(), entry.getValue())) {
                continue;
            }
            String fieldName = entry.getKey();
            if (fieldName.endsWith("[]")) {
                fieldName = fieldName.substring(0, fieldName.length() - 2);
            }
            String realFieldName = fieldName;
            int dollarLastIndex = fieldName.lastIndexOf('$');
            if (dollarLastIndex > 0) {
                realFieldName = fieldName.substring(0, dollarLastIndex);
            }
            if (!whitelistMap.containsKey(realFieldName)) {
                continue;
            }
            if (allowArraySuffixes != null && isEndsWith(fieldName, allowArraySuffixes)) {
                resultMap.put(fieldName, entry.getValue());
            } else {
                resultMap.put(fieldName, entry.getValue()[0]);
            }
        }
        return resultMap;
    }

    private static boolean isEndsWith(String fieldName, List<String> suffixes) {
        if (VerifyTools.isBlank(suffixes)) {
            return false;
        }
        for (String suffix : suffixes) {
            if (fieldName.endsWith('$' + suffix)) {
                return true;
            }
        }
        return false;
    }

}
