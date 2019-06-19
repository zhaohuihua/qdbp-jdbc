package com.gitee.qdbp.jdbc.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.jdbc.core.JdbcOperations;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gitee.qdbp.able.jdbc.condition.DbUpdate;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.able.jdbc.condition.TableJoin;
import com.gitee.qdbp.able.jdbc.condition.TableJoin.JoinItem;
import com.gitee.qdbp.able.jdbc.condition.TableJoin.TableItem;
import com.gitee.qdbp.able.jdbc.utils.FieldTools;
import com.gitee.qdbp.jdbc.model.AllFieldColumn;
import com.gitee.qdbp.jdbc.model.DbVersion;
import com.gitee.qdbp.jdbc.model.TablesFieldColumn;
import com.gitee.qdbp.jdbc.model.PrimaryKeyFieldColumn;
import com.gitee.qdbp.jdbc.model.SimpleFieldColumn;
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
        AllFieldColumn<?> allFields = parseToAllFieldColumn(clazz);
        if (allFields.isEmpty()) {
            throw new IllegalArgumentException("fields is empty");
        }
        ModelDataHandler handler = DbPluginContainer.global.getModelDataHandler();
        return new ModelDataExecutor(allFields, handler);
    }

    /**
     * 获取实体业务执行接口
     * 
     * @return 实体业务执行类
     */
    public static ModelDataExecutor getModelDataExecutor(TableJoin tables) {
        AllFieldColumn<?> allFields = parseToAllFieldColumn(tables);
        if (allFields.isEmpty()) {
            throw new IllegalArgumentException("fields is empty");
        }
        ModelDataHandler handler = DbPluginContainer.global.getModelDataHandler();
        return new ModelDataExecutor(allFields, handler);
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
    private static Map<Class<?>, PrimaryKeyFieldColumn> entityPrimaryKeyCache = new ConcurrentHashMap<>();

    /**
     * 扫描获取主键
     * 
     * @param clazz 类名
     * @return 主键
     */
    public static PrimaryKeyFieldColumn parsePrimaryKey(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("clazz is null");
        }
        if (entityPrimaryKeyCache.containsKey(clazz)) {
            return entityPrimaryKeyCache.get(clazz);
        }

        TableInfoScans scans = DbPluginContainer.global.getTableInfoScans();
        PrimaryKeyFieldColumn pk = scans.scanPrimaryKey(clazz);
        entityPrimaryKeyCache.put(clazz, pk);
        return pk;
    }

    /** TableJoin的列名缓存 **/
    private static Map<String, List<TablesFieldColumn>> joinColumnsCache = new ConcurrentHashMap<>();

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

    private static List<TablesFieldColumn> scanColumnList(TableItem table) {
        TableInfoScans scans = DbPluginContainer.global.getTableInfoScans();
        List<SimpleFieldColumn> fields = scans.scanColumnList(table.getTableType());
        String tableAlias = table.getTableAlias();
        String resultField = table.getResultField();
        List<TablesFieldColumn> result = new ArrayList<>(fields.size());
        for (SimpleFieldColumn item : fields) {
            TablesFieldColumn copied = item.to(TablesFieldColumn.class);
            copied.setTableAlias(tableAlias);
            copied.setResultField(resultField);
            result.add(copied);
        }
        return result;
    }

    /**
     * 扫描获取字段名和数据库列名的映射表
     * 
     * @param tables 表关联对象
     * @return AllFields: fieldName - columnName
     */
    public static List<TablesFieldColumn> parseFieldColumns(TableJoin tables) {
        if (tables == null) {
            throw new IllegalArgumentException("tables is null");
        }
        String cacheKey = buildCacheKey(tables);
        if (joinColumnsCache.containsKey(cacheKey)) {
            return joinColumnsCache.get(cacheKey);
        }
        TableItem major = tables.getMajor();
        List<JoinItem> joins = tables.getJoins();
        List<TablesFieldColumn> all = new ArrayList<>();
        { // 添加主表的字段
            List<TablesFieldColumn> fields = scanColumnList(major);
            all.addAll(fields);
        }
        if (VerifyTools.isNotBlank(joins)) {
            // 添加关联表的字段
            for (JoinItem item : joins) {
                List<TablesFieldColumn> fields = scanColumnList(item);
                all.addAll(fields);
            }
        }
        // 处理重名字段: 设置columnAlias
        // 1.先统计字段出现次数
        Map<String, Integer> countMaps = new HashMap<>();
        for (SimpleFieldColumn field : all) {
            String fieldName = field.getFieldName();
            if (countMaps.containsKey(fieldName)) {
                countMaps.put(fieldName, countMaps.get(fieldName) + 1);
            } else {
                countMaps.put(fieldName, 1);
            }
        }
        // 2.如果出现多次则设置columnAlias=tableAlias_columnName
        for (TablesFieldColumn field : all) {
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
     * @return AllFieldColumn: fieldName - columnName
     */
    public static AllFieldColumn<TablesFieldColumn> parseToAllFieldColumn(TableJoin tables) {
        List<TablesFieldColumn> fields = parseFieldColumns(tables);
        return new AllFieldColumn<>(fields);
    }

    /** Entity的列名缓存 **/
    private static Map<Class<?>, List<SimpleFieldColumn>> entityColumnsCache = new ConcurrentHashMap<>();

    /**
     * 扫描获取字段名和数据库列名的映射表
     * 
     * @param clazz 类型
     * @return AllFields: fieldName - columnName
     */
    public static List<SimpleFieldColumn> parseFieldColumns(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("clazz is null");
        }
        if (entityColumnsCache.containsKey(clazz)) {
            return entityColumnsCache.get(clazz);
        }

        TableInfoScans scans = DbPluginContainer.global.getTableInfoScans();
        List<SimpleFieldColumn> all = scans.scanColumnList(clazz);
        entityColumnsCache.put(clazz, all);
        return all;
    }

    /**
     * 扫描获取字段名和数据库列名的映射表
     * 
     * @param tables 表关联对象
     * @return AllFieldColumn: fieldName - columnName
     */
    public static AllFieldColumn<SimpleFieldColumn> parseToAllFieldColumn(Class<?> clazz) {
        List<SimpleFieldColumn> fields = parseFieldColumns(clazz);
        return new AllFieldColumn<>(fields);
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
        AllFieldColumn<?> allFields = parseToAllFieldColumn(object.getClass());
        if (allFields == null || allFields.isEmpty()) {
            return null;
        }
        List<String> fieldNames = allFields.getFieldNames();

        // 只保留有列信息的字段
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (FieldTools.contains(fieldNames, entry.getKey())) {
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
        AllFieldColumn<?> allFields = parseToAllFieldColumn(clazz);
        List<String> fieldNames = allFields.getFieldNames();
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
        AllFieldColumn<?> allFields = parseToAllFieldColumn(clazz);
        List<String> fieldNames = allFields.getFieldNames();
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
            if (FieldTools.contains(excludeFields, realFieldName)) {
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
            if (!FieldTools.contains(includeFields, realFieldName)) {
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
