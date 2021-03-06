package com.gitee.qdbp.jdbc.sql.parse;

import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.SqlBuilder;
import com.gitee.qdbp.jdbc.utils.DbTools;
import com.gitee.qdbp.staticize.exception.TagException;
import com.gitee.qdbp.staticize.publish.BaseContext;
import com.gitee.qdbp.staticize.utils.OgnlTools;
import com.gitee.qdbp.tools.utils.StringTools;

/**
 * 环境变量管理类
 *
 * @author zhaohuihua
 * @version 20200912
 * @since 3.2.0
 */
class SqlBufferContext extends BaseContext {

    /** 数据库方言处理接口 **/
    private final SqlDialect dialect;

    /** 内部构造函数 **/
    protected SqlBufferContext(SqlDialect dialect) {
        super(new SqlCachingWriter());
        this.dialect = dialect;
    }

    /** SQL语句 **/
    public SqlBuffer getSqlBuffer() {
        SqlCachingWriter writer = (SqlCachingWriter) this.getWriter();
        return writer.getContent();
    }

    /**
     * 根据表达式从环境变量中获取值<br>
     * ${xxx}, 拼写式参数<br>
     * #{xxx}, 预编译参数<br>
     *
     * @param prefix 表达式前缀类型
     * @param expression 表达式
     * @return 环境变量中的对象
     */
    @Override
    public Object doGetValue(String prefix, String expression) throws TagException {
        if ("#".equals(prefix)) { // 预编译参数
            Object value = OgnlTools.getValue(stack(), expression);
            return value == null ? null : new SqlBuffer().addVariable(value);
        } else if ("$".equals(prefix)) { // 拼写式参数
            if (expression.trim().startsWith("sql:")) {
                String exp = StringTools.removePrefix(expression.trim(), "sql:");
                Object value = OgnlTools.getValue(stack(), exp);
                if (value instanceof SqlBuffer) {
                    return ((SqlBuffer) value).getExecutableSqlString(dialect);
                } else if (value instanceof SqlBuilder) {
                    return ((SqlBuilder) value).out().getExecutableSqlString(dialect);
                } else {
                    return DbTools.variableToString(value, dialect);
                }
            } else {
                Object value = OgnlTools.getValue(stack(), expression);
                if (value == null) {
                    return null;
                } else if (value instanceof SqlBuffer) {
                    return ((SqlBuffer) value).getExecutableSqlString(dialect);
                } else if (value instanceof SqlBuilder) {
                    return ((SqlBuilder) value).out().getExecutableSqlString(dialect);
                } else {
                    return value;
                }
            }
        } else {
            throw new TagException("表达式前缀有误");
        }
    }
}
