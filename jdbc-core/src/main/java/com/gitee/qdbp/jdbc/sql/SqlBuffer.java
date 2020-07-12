package com.gitee.qdbp.jdbc.sql;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.gitee.qdbp.able.jdbc.model.DbFieldName;
import com.gitee.qdbp.able.jdbc.model.DbRawValue;
import com.gitee.qdbp.jdbc.model.DbVersion;
import com.gitee.qdbp.jdbc.model.MainDbType;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.utils.DbTools;
import com.gitee.qdbp.tools.utils.StringTools;

/**
 * SQL容器
 *
 * @author zhaohuihua
 * @version 190601
 */
public class SqlBuffer implements Serializable {

    /** SerialVersionUID **/
    private static final long serialVersionUID = 1L;

    /** 占位符当前序号 **/
    private int index;
    /** SQL缓存容器 **/
    private List<Item> buffer;

    /** 构造函数 **/
    public SqlBuffer() {
        this.index = 0;
        this.buffer = new ArrayList<>();
    }

    /** 构造函数 **/
    public SqlBuffer(String sql) {
        this.index = 0;
        this.buffer = new ArrayList<>();
        this.append(sql);
    }

    /** 构造函数 **/
    public SqlBuffer(SqlBuffer sql) {
        this.index = 0;
        this.buffer = new ArrayList<>();
        this.append(sql);
    }

    /**
     * 将指定SQL片段追加到SQL后面
     * 
     * @param part SQL片段
     * @return 返回当前SQL容器用于连写
     */
    public SqlBuffer append(String part) {
        StringItem si = getLastStringItem();
        si.append(part);
        return this;
    }

    /**
     * 将指定SQL片段追加到SQL后面
     * 
     * @param part SQL片段
     * @return 返回当前SQL容器用于连写
     */
    public SqlBuffer append(char... part) {
        StringItem si = getLastStringItem();
        si.append(part);
        return this;
    }

    /**
     * 将指定SQL片段追加到SQL后面
     * 
     * @param parts SQL片段
     * @param suffix 后缀
     * @return 返回当前SQL容器用于连写
     */
    public SqlBuffer append(String part, char suffix) {
        StringItem si = getLastStringItem();
        si.append(part, suffix);
        return this;
    }

    /**
     * 将指定SQL片段追加到SQL后面
     * 
     * @param prefix 前缀
     * @param part SQL片段
     * @return 返回当前SQL容器用于连写
     */
    public SqlBuffer append(char prefix, String part) {
        StringItem si = getLastStringItem();
        si.append(prefix, part);
        return this;
    }

    /**
     * 将指定SQL片段追加到SQL后面
     * 
     * @param prefix 前缀
     * @param part SQL片段
     * @param suffix 后缀
     * @return 返回当前SQL容器用于连写
     */
    public SqlBuffer append(char prefix, String part, char suffix) {
        StringItem si = getLastStringItem();
        si.append(prefix, part, suffix);
        return this;
    }

    /**
     * 将指定SQL片段增加到SQL语句最前面
     * 
     * @param part SQL片段
     * @return 返回当前SQL容器用于连写
     */
    public SqlBuffer prepend(String part) {
        StringItem si = getFirstStringItem();
        si.prepend(part);
        return this;
    }

    /**
     * 将指定SQL片段增加到SQL语句最前面
     * 
     * @param part SQL片段
     * @return 返回当前SQL容器用于连写
     */
    public SqlBuffer prepend(char... part) {
        StringItem si = getFirstStringItem();
        si.prepend(part);
        return this;
    }

    /**
     * 将指定SQL片段增加到SQL语句最前面
     * 
     * @param parts SQL片段
     * @param suffix 后缀
     * @return 返回当前SQL容器用于连写
     */
    public SqlBuffer prepend(String part, char suffix) {
        StringItem si = getFirstStringItem();
        si.prepend(part, suffix);
        return this;
    }

    /**
     * 将指定SQL片段增加到SQL语句最前面
     * 
     * @param prefix 前缀
     * @param part SQL片段
     * @return 返回当前SQL容器用于连写
     */
    public SqlBuffer prepend(char prefix, String part) {
        StringItem si = getFirstStringItem();
        si.prepend(prefix, part);
        return this;
    }

    /**
     * 将指定SQL片段增加到SQL语句最前面
     * 
     * @param prefix 前缀
     * @param part SQL片段
     * @param suffix 后缀
     * @return 返回当前SQL容器用于连写
     */
    public SqlBuffer prepend(char prefix, String part, char suffix) {
        StringItem si = getFirstStringItem();
        si.prepend(prefix, part, suffix);
        return this;
    }

    /**
     * 将指定SQL片段追加到SQL语句后面
     * 
     * @param another SQL片段
     * @return 返回当前SQL容器用于连写
     */
    public SqlBuffer append(SqlBuffer another) {
        if (another != null && !another.isEmpty()) {
            another.copyTo(this);
        }
        return this;
    }

    /**
     * 将指定SQL片段追加到SQL语句后面
     * 
     * @param prefix 前缀
     * @param another SQL片段
     * @return 返回当前SQL容器用于连写
     */
    public SqlBuffer append(char prefix, SqlBuffer another) {
        if (another != null && !another.isEmpty()) {
            this.append(prefix);
            another.copyTo(this);
        }
        return this;
    }

    /**
     * 将指定SQL片段增加到SQL语句最前面
     * 
     * @param another SQL片段
     * @return 返回当前SQL容器用于连写
     */
    public SqlBuffer prepend(SqlBuffer another) {
        if (another != null && !another.isEmpty()) {
            // 复制到临时缓冲区
            SqlBuffer temp = another.copy();
            // 序号递增, 空出前面的位置
            this.raiseIndex(another.index);
            // 增加到SQL语句最前面
            this.buffer.addAll(0, temp.buffer);
        }
        return this;
    }

    /**
     * 将指定SQL片段增加到SQL语句最前面
     * 
     * @param another SQL片段
     * @param suffix 后缀
     * @return 返回当前SQL容器用于连写
     */
    public SqlBuffer prepend(SqlBuffer another, char suffix) {
        if (another != null && !another.isEmpty()) {
            this.prepend(suffix);
            // 序号递增, 空出前面的位置
            this.raiseIndex(another.index);
            // 增加到SQL语句最前面
            this.buffer.addAll(0, another.buffer);
        }
        return this;
    }

    private StringItem getLastStringItem() {
        Item last = this.buffer.isEmpty() ? null : this.buffer.get(this.buffer.size() - 1);
        if (last instanceof StringItem) {
            return (StringItem) last;
        } else {
            StringItem si = new StringItem();
            this.buffer.add(si);
            return si;
        }
    }

    private StringItem getFirstStringItem() {
        Item first = this.buffer.isEmpty() ? null : this.buffer.get(0);
        if (first instanceof StringItem) {
            return (StringItem) first;
        } else {
            StringItem si = new StringItem();
            this.buffer.add(0, si);
            return si;
        }
    }

    /** 序号递增, 空出前面的位置 **/
    protected void raiseIndex(int offset) {
        if (offset == 0) {
            return;
        }
        this.index += offset;
        for (Item item : this.buffer) {
            if (item instanceof VariableItem) {
                VariableItem variable = ((VariableItem) item);
                variable.index += offset;
            }
        }
    }

    /**
     * 增加变量, 同时将变量以占位符追加到SQL语句中
     * 
     * @param value 变量
     * @return 返回当前SQL容器用于连写
     */
    public SqlBuffer addVariable(Object value) {
        if (value instanceof SqlBuffer) {
            append((SqlBuffer) value);
        } else if (value instanceof DbRawValue) {
            append(value.toString());
        } else if (value instanceof DbFieldName) {
            // append(value.toString());
            // 缺少环境数据, 无法将字段名转换为列名
            throw new IllegalArgumentException("CanNotSupportedVariableType: DbFieldName");
        } else {
            this.buffer.add(new VariableItem(index++, value));
        }
        return this;
    }

    public boolean isEmpty() {
        return buffer.isEmpty();
    }

    /**
     * 超级长的SQL在输出日志时可以省略掉一部分<br>
     * 省略哪一部分, 用ellipsis(true/false)来标识起止位置
     *
     * @author zhaohuihua
     * @version 20200712
     */
    public SqlBuffer ellipsis(boolean enabled) {
        this.buffer.add(new EllipsisItem(enabled));
        return this;
    }

    /** 所有内容缩进1个TAB **/
    public SqlBuffer indentAll() {
        return indentAll(1, true);
    }

    /**
     * 所有内容缩进n个TAB
     * 
     * @param size 缩进多少个TAB
     * @param leading 开头要不要缩进
     * @return 返回当前SQL容器用于连写
     */
    public SqlBuffer indentAll(int size, boolean leading) {
        if (size <= 0 || this.buffer.isEmpty()) {
            return this;
        }
        char[] tabs = getTabs(size);
        for (Item item : this.buffer) {
            if (item instanceof StringItem) {
                StringItem stringItem = (StringItem) item;
                stringItem.indent(tabs);
            }
        }
        if (leading) {
            Item first = this.buffer.get(0);
            if (first instanceof StringItem) {
                StringItem stringItem = (StringItem) first;
                stringItem.prepend(tabs);
            } else {
                this.prepend(tabs);
            }
        }
        return this;
    }

    private char[] getTabs(int size) {
        if (size == 1) {
            return new char[] { '\t' };
        } else {
            char[] tabs = new char[size];
            for (int i = 0; i < size; i++) {
                tabs[i] = '\t';
            }
            return tabs;
        }
    }

    /**
     * 复制
     * 
     * @return 副本
     */
    public SqlBuffer copy() {
        SqlBuffer temp = new SqlBuffer();
        this.copyTo(temp);
        return temp;
    }

    /**
     * 复制到另一个缓存容器中
     * 
     * @param target 目标容器
     */
    public void copyTo(SqlBuffer target) {
        for (Item item : this.buffer) {
            if (item instanceof VariableItem) {
                VariableItem variable = ((VariableItem) item);
                target.addVariable(variable.value);
            } else if (item instanceof StringItem) {
                StringItem stringItem = (StringItem) item;
                target.append(stringItem.value.toString());
            } else {
                throw new UnsupportedOperationException("Unsupported item: " + item.getClass());
            }
        }
    }

    /** 获取预编译SQL语句 **/
    public String getPreparedSqlString(DbVersion version) {
        return getPreparedSqlString(DbTools.buildSqlDialect(version));
    }

    /** 获取预编译SQL语句 **/
    public String getPreparedSqlString(SqlDialect dialect) {
        StringBuilder sql = new StringBuilder();
        for (Object item : this.buffer) {
            if (item instanceof StringItem) {
                StringItem stringItem = (StringItem) item;
                sql.append(stringItem.getValue());
            } else if (item instanceof VariableItem) {
                sql.append(':').append(((VariableItem) item).getKey());
            } else if (item instanceof RawValueItem) {
                RawValueItem rawValueItem = (RawValueItem) item;
                sql.append(DbTools.resolveRawValue(rawValueItem.getRawValue(), dialect));
            } else if (item instanceof EllipsisItem) {
                continue;
            } else {
                throw new UnsupportedOperationException("Unsupported item: " + item.getClass());
            }
        }
        return sql.toString();
    }

    /** 获取预编译SQL参数 **/
    public Map<String, Object> getPreparedVariables(DbVersion version) {
        return getPreparedVariables(DbTools.buildSqlDialect(version));
    }

    /** 获取预编译SQL参数 **/
    public Map<String, Object> getPreparedVariables(SqlDialect dialect) {
        Map<String, Object> map = new HashMap<>();
        for (Object item : this.buffer) {
            if (item instanceof VariableItem) {
                VariableItem variable = ((VariableItem) item);
                map.put(variable.getKey(), DbTools.variableToDbValue(variable.getValue(), dialect));
            }
        }
        return map;
    }

    /** 获取可执行SQL语句(预编译参数替换为拼写式参数) **/
    public String getExecutableSqlString(DbVersion version) {
        return getExecutableSqlString(DbTools.buildSqlDialect(version));
    }

    /** 获取可执行SQL语句(预编译参数替换为拼写式参数) **/
    public String getExecutableSqlString(SqlDialect dialect) {
        StringBuilder sql = new StringBuilder();
        for (Object item : this.buffer) {
            if (item instanceof StringItem) {
                StringItem stringItem = (StringItem) item;
                sql.append(stringItem.getValue());
            } else if (item instanceof VariableItem) {
                VariableItem variable = ((VariableItem) item);
                String string = DbTools.variableToString(variable.value, dialect);
                sql.append(string);
            } else if (item instanceof RawValueItem) {
                RawValueItem rawValueItem = (RawValueItem) item;
                sql.append(DbTools.resolveRawValue(rawValueItem.getRawValue(), dialect));
            } else if (item instanceof EllipsisItem) {
                continue;
            } else {
                throw new UnsupportedOperationException("Unsupported item: " + item.getClass());
            }
        }
        return sql.toString();
    }

    /** 获取用于日志输出的SQL语句(预编译参数替换为拼写式参数)(如果参数值长度超过100会被截断) **/
    public String getLoggingSqlString(DbVersion version) {
        return getLoggingSqlString(DbTools.buildSqlDialect(version));
    }

    /** 获取用于日志输出的SQL语句(预编译参数替换为拼写式参数)(如果参数值长度超过100会被截断) **/
    public String getLoggingSqlString(SqlDialect dialect) {
        int valueLimit = 100;
        StringBuilder sql = new StringBuilder();
        boolean enableEllipsis = false;
        int lineCount = 0;
        int charCount = 0;
        for (Object item : this.buffer) {
            if (item instanceof StringItem) {
                StringItem stringItem = (StringItem) item;
                String stringValue = stringItem.getValue().toString();
                if (enableEllipsis) {
                    charCount += stringValue == null ? 4 : stringValue.length();
                    lineCount += stringValue == null ? 0 : countNewLineChars(stringValue);
                } else {
                    sql.append(stringValue);
                }
            } else if (item instanceof VariableItem) {
                VariableItem variable = ((VariableItem) item);
                String stringValue = DbTools.variableToString(variable.value, dialect);
                if (enableEllipsis) {
                    charCount += stringValue == null ? 4 : stringValue.length();
                    lineCount += stringValue == null ? 0 : countNewLineChars(stringValue);
                } else {
                    sql.append(tryCutStringOverlength(stringValue, valueLimit));
                    sql.append("/*").append(variable.getKey()).append("*/");
                }
            } else if (item instanceof RawValueItem) {
                RawValueItem rawValueItem = (RawValueItem) item;
                String stringValue = DbTools.resolveRawValue(rawValueItem.getRawValue(), dialect);
                if (enableEllipsis) {
                    charCount += stringValue == null ? 4 : stringValue.length();
                    lineCount += stringValue == null ? 0 : countNewLineChars(stringValue);
                } else {
                    sql.append(stringValue);
                }
            } else if (item instanceof EllipsisItem) {
                EllipsisItem ellipsisItem = (EllipsisItem) item;
                if (ellipsisItem.enabled() != enableEllipsis) {
                    if (!ellipsisItem.enabled() && (charCount > 0 || lineCount > 1)) {
                        // 输出省略掉的行数和字符数
                        StringBuffer msg = new StringBuffer();
                        if (lineCount > 1) {
                            msg.append("line: ").append(lineCount);
                        }
                        if (charCount > 0) {
                            if (msg.length() > 0) {
                                msg.append(',').append(' ');
                            }
                            msg.append("chars: ").append(charCount);
                        }
                        sql.append('\n').append('\t').append('\t').append(' ');
                        sql.append("...").append(' ').append('(').append(msg).append(')').append('\n');
                    }
                    lineCount = 0;
                    charCount = 0;
                    enableEllipsis = ellipsisItem.enabled();
                }
            } else {
                throw new UnsupportedOperationException("Unsupported item: " + item.getClass());
            }
        }
        return sql.toString();
    }

    /** 统计文本中有多少个换行符 **/
    private int countNewLineChars(String string) {
        int count = 0;
        int size = string == null ? 0 : string.length();
        for (int i = 0; i < size; i++) {
            if (string.charAt(i) == '\n') {
                count++;
            }
        }
        return count;
    }

    /** 尝试截短字符串 **/
    protected static String tryCutStringOverlength(String string, int limit) {
        if (limit <= 0) {
            return string;
        }
        if (!string.startsWith("'") || !string.endsWith("'")) {
            return string;
        }
        int length = string.length() - 2;
        if (length <= limit) {
            return string;
        }
        string = string.substring(1, string.length() - 1);
        string = StringTools.ellipsis(string, limit) + '(' + length + ')';
        return "'" + string + "'";
    }

    public String toString() {
        SqlDialect dialect = DbTools.buildSqlDialect(new DbVersion(MainDbType.Oracle));
        return getLoggingSqlString(dialect);
    }

    protected static interface Item {
    }

    /**
     * 超级长的SQL在输出日志时可以省略掉一部分<br>
     * 省略哪一部分, 用EllipsisItem来标识起止位置
     *
     * @author zhaohuihua
     * @version 20200712
     */
    protected static class EllipsisItem implements Item, Serializable {

        /** SerialVersionUID **/
        private static final long serialVersionUID = 1L;
        private final boolean enabled;

        public EllipsisItem(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean enabled() {
            return enabled;
        }
    }

    protected static class RawValueItem implements Item, Serializable {

        /** SerialVersionUID **/
        private static final long serialVersionUID = 1L;
        private String rawValue;

        public RawValueItem(String rawValue) {
            this.rawValue = rawValue;
        }

        public String getRawValue() {
            return rawValue;
        }
    }

    protected static class StringItem implements Item, Serializable {

        /** SerialVersionUID **/
        private static final long serialVersionUID = 1L;

        private final StringBuffer value;

        public StringItem() {
            this.value = new StringBuffer();
        }

        public void append(char... chars) {
            this.value.append(chars);
        }

        public void append(String value) {
            this.value.append(value);
        }

        public void append(String value, char suffix) {
            this.value.append(value).append(suffix);
        }

        public void append(char prefix, String value) {
            this.value.append(prefix).append(value);
        }

        public void append(char prefix, String value, char suffix) {
            this.value.append(prefix).append(value).append(suffix);
        }

        public void prepend(char... chars) {
            this.value.insert(0, chars);
        }

        public void prepend(String value) {
            this.value.insert(0, value);
        }

        public void prepend(String value, char suffix) {
            this.value.insert(0, suffix).insert(0, value);
        }

        public void prepend(char prefix, String value) {
            this.value.insert(0, value).insert(0, prefix);
        }

        public void prepend(char prefix, String value, char suffix) {
            this.value.insert(0, suffix).insert(0, value).insert(0, prefix);
        }

        /** 缩进TAB(只在换行符后面增加TAB) **/
        public void indent(char[] tabs) {
            for (int i = value.length() - 1; i >= 0; i--) {
                if (value.charAt(i) == '\n') {
                    value.insert(i + 1, tabs);
                }
            }
        }

        public StringBuffer getValue() {
            return this.value;
        }

        public String toString() {
            return this.value.toString();
        }
    }

    protected static class VariableItem implements Item, Serializable {

        /** SerialVersionUID **/
        private static final long serialVersionUID = 1L;

        private int index;
        private Object value;

        public VariableItem(int index, Object value) {
            this.index = index;
            this.value = value;
        }

        public String getKey() {
            return "$" + (index + 1);
        }

        public Object getValue() {
            return this.value;
        }

        public String toString() {
            return this.getKey();
        }
    }
}
