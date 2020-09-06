package com.gitee.qdbp.jdbc.sql.parse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.SqlBuilder;
import com.gitee.qdbp.jdbc.utils.DbTools;
import com.gitee.qdbp.staticize.common.IContext;
import com.gitee.qdbp.staticize.exception.TagException;
import com.gitee.qdbp.staticize.utils.OgnlTools;
import com.gitee.qdbp.staticize.utils.TagUtils;

/**
 * 环境变量管理类
 *
 * @author zhaohuihua
 * @version 140523
 */
class SqlBufferContext implements IContext {

    /** 数据库方言处理接口 **/
    private final SqlDialect dialect;
    /** SQL语句缓存 **/
    private final SqlBuilder builder;
    /** 预置环境变量 **/
    private final Map<String, Object> preset;
    /** 值栈环境变量, 用于实现子标签的变量在子标签关闭后不覆盖父标签的变量 **/
    private final Map<String, Object> stack;
    /** 类的导入信息, key=&#64;类名简称, value=类名全称 **/
    private final Map<String, String> imports;

    /** 内部构造函数 **/
    protected SqlBufferContext(SqlDialect dialect) {
        this.dialect = dialect;
        this.builder = new SqlBuilder();
        this.preset = new HashMap<>();
        this.stack = new HashMap<>();
        this.imports = new HashMap<>();
    }

    /** SQL语句 **/
    public SqlBuffer getSqlBuffer() {
        return this.builder.out();
    }

    /**
     * 获取预置环境变量容器, 通过#{xxx}取值
     *
     * @return 预置环境变量容器
     */
    @Override
    public Map<String, Object> preset() {
        return preset;
    }

    /**
     * 获取值栈环境变量容器, 通过${xxx}取值
     *
     * @return 值栈环境变量容器
     */
    @Override
    public Map<String, Object> stack() {
        return stack;
    }

    /**
     * 增加类的导入信息
     * 
     * @param classFullName 类名全称, 如: com.qdbp.xxx.EntityTools
     */
    public void addImportClass(String classFullName) throws TagException {
        try {
            Class.forName(classFullName);
        } catch (ClassNotFoundException e) {
            throw new TagException("Class not found: " + classFullName);
        }
        String shortName;
        int dotIndex = classFullName.lastIndexOf('.');
        if (dotIndex < 0) {
            shortName = '@' + classFullName;
        } else {
            shortName = '@' + classFullName.substring(dotIndex + 1);
        }
        this.imports.put(shortName, classFullName);
    }

    /**
     * 获取类的导入信息
     * 
     * @param shortName &#64;类名简称, 如: &#64;EntityTools
     * @return fullName 类名全称, 如: com.qdbp.xxx.EntityTools
     */
    public String getImportClass(String shortName) {
        return this.imports.get(shortName);
    }

    /**
     * 根据表达式从环境变量中获取值<br>
     * ${xxx}, 从值栈环境变量中获取值, 如${title}, ${image.src}<br>
     * #{xxx}, 从预置环境变量中获取值, 如#{site.id}, #{column.title}<br>
     * &nbsp;&nbsp;#{site}=站点信息, <br>
     * &nbsp;&nbsp;#{column}=栏目信息<br>
     *
     * @author zhaohuihua
     * @param expression 表达式
     * @return 环境变量中的对象
     */
    @Override
    public Object getValue(String expression) throws TagException {
        if (expression == null || expression.trim().length() == 0) {
            throw new TagException("表达式不能为空");
        }

        expression = expression.trim();
        Matcher matcher = TagUtils.EXPRESSION.matcher(expression);
        if (!matcher.matches()) {
            throw new TagException("表达式有误");
        }

        String prefix = matcher.group(1);
        String key = matcher.group(2);

        if ("#".equals(prefix)) { // 预编译参数
            Object value = OgnlTools.getValue(stack, key);
            return new SqlBuffer().addVariable(value);
        } else if ("$".equals(prefix)) { // 拼写式参数
            Object value = OgnlTools.getValue(stack, key);
            if (value instanceof SqlBuffer) {
                return ((SqlBuffer) value).getExecutableSqlString(dialect);
            } else if (value instanceof SqlBuilder) {
                return ((SqlBuilder) value).out().getExecutableSqlString(dialect);
            } else {
                return DbTools.variableToString(value, dialect);
            }
        } else {
            throw new TagException("表达式前缀有误");
        }
    }

    @Override
    public void write(Object value) throws IOException {
        if (value == null) {
            return;
        }
        if (value instanceof String) {
            this.builder.ad((String) value);
        } else if (value instanceof Character) {
            this.builder.ad((Character) value);
        } else if (value instanceof SqlBuffer) {
            this.builder.ad((SqlBuffer) value);
        } else if (value instanceof SqlBuilder) {
            this.builder.ad((SqlBuilder) value);
        } else {
            throw new IllegalArgumentException("UnsupportedArgumentType: " + value.getClass());
        }
    }
}
