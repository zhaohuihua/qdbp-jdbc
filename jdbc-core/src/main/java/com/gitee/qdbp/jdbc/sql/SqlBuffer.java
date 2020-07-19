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
import com.gitee.qdbp.tools.utils.IndentTools;
import com.gitee.qdbp.tools.utils.StringTools;
import com.gitee.qdbp.tools.utils.VerifyTools;

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
    /** 快捷方式实例 **/
    private SqlBuilder shortcut;

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

    /** 返回当前实例的快捷方式实例 **/
    public SqlBuilder shortcut() {
        if (this.shortcut == null) {
            this.shortcut = new SqlBuilder(this);
        }
        return this.shortcut;
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
            DbRawValue raw = (DbRawValue) value;
            this.buffer.add(new RawValueItem(raw.toString()));
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
     * 省略哪一部分, 用startOmit()/endOmit()来标识起止位置
     * 
     * @return 返回当前SQL容器用于连写
     */
    public SqlBuffer startOmit() {
        this.buffer.add(new OmitItem(true));
        return this;
    }

    /**
     * 超级长的SQL在输出日志时可以省略掉一部分<br>
     * 省略哪一部分, 用startOmit()/endOmit()来标识起止位置
     * 
     * @return 返回当前SQL容器用于连写
     */
    public SqlBuffer endOmit() {
        this.buffer.add(new OmitItem(true));
        return this;
    }

    /**
     * 传入size和当前index, 自动计算, 插入省略标记<br>
     * 日志只取前3行+后3行; 因此在第3行后面开始省略, 在后3行之前结束省略<br>
     * 注意: 如果有换行符, 最好放在换行符后面
     * 
     * @param index 当前行数
     * @param count 总行数
     * @return 返回当前SQL容器用于连写
     */
    public SqlBuffer tryOmit(int index, int count) {
        if (count >= 7 && index == 3) {
            this.startOmit(); // 开始省略
        } else if (count >= 7 && index == count - 3) {
            this.endOmit(); // 结束省略
        }
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
        char[] tabs = IndentTools.getIndenTabs(size);
        for (Item item : this.buffer) {
            if (item instanceof StringItem) {
                StringItem stringItem = (StringItem) item;
                stringItem.indentAll(tabs);
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

    /**
     * 复制
     * 
     * @return 副本
     */
    public SqlBuffer copy() {
        SqlBuffer target = new SqlBuffer();
        this.copyTo(target);
        return target;
    }

    /**
     * 复制到另一个缓存容器中
     * 
     * @param target 目标容器
     */
    public void copyTo(SqlBuffer target) {
        for (Item item : this.buffer) {
            if (item instanceof StringItem) {
                StringItem stringItem = (StringItem) item;
                target.getLastStringItem().append(stringItem.getValue().toString());
            } else if (item instanceof VariableItem) {
                VariableItem variable = ((VariableItem) item);
                target.buffer.add(new VariableItem(target.index++, variable.getValue()));
            } else if (item instanceof RawValueItem) {
                RawValueItem rawItem = (RawValueItem) item;
                target.buffer.add(new RawValueItem(rawItem.getValue()));
            } else if (item instanceof OmitItem) {
                OmitItem omitItem = (OmitItem) item;
                target.buffer.add(new OmitItem(omitItem.enabled()));
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
        for (Item item : this.buffer) {
            if (item instanceof StringItem) {
                StringItem stringItem = (StringItem) item;
                sql.append(stringItem.getValue());
            } else if (item instanceof VariableItem) {
                sql.append(':').append(((VariableItem) item).getKey());
            } else if (item instanceof RawValueItem) {
                RawValueItem rawValueItem = (RawValueItem) item;
                sql.append(DbTools.resolveRawValue(rawValueItem.getValue(), dialect));
            } else if (item instanceof OmitItem) {
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
        for (Item item : this.buffer) {
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
        for (Item item : this.buffer) {
            if (item instanceof StringItem) {
                StringItem stringItem = (StringItem) item;
                sql.append(stringItem.getValue());
            } else if (item instanceof VariableItem) {
                VariableItem variable = ((VariableItem) item);
                String string = DbTools.variableToString(variable.value, dialect);
                sql.append(string);
            } else if (item instanceof RawValueItem) {
                RawValueItem rawValueItem = (RawValueItem) item;
                sql.append(DbTools.resolveRawValue(rawValueItem.getValue(), dialect));
            } else if (item instanceof OmitItem) {
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
        boolean omitEnabled = false;
        int charCount = 0;
        int lineCount = 0;
        for (Item item : this.buffer) {
            if (item instanceof StringItem) {
                StringItem stringItem = (StringItem) item;
                String stringValue = stringItem.getValue().toString();
                if (omitEnabled) {
                    charCount += stringValue == null ? 4 : stringValue.length();
                    lineCount += stringValue == null ? 0 : IndentTools.countNewlineChars(stringValue);
                } else {
                    sql.append(stringValue);
                }
            } else if (item instanceof VariableItem) {
                VariableItem variable = ((VariableItem) item);
                String stringValue = DbTools.variableToString(variable.value, dialect);
                if (omitEnabled) {
                    charCount += stringValue == null ? 4 : stringValue.length();
                    lineCount += stringValue == null ? 0 : IndentTools.countNewlineChars(stringValue);
                } else {
                    sql.append(tryCutStringOverlength(stringValue, valueLimit));
                    sql.append("/*").append(variable.getKey()).append("*/");
                }
            } else if (item instanceof RawValueItem) {
                RawValueItem rawValueItem = (RawValueItem) item;
                String stringValue = DbTools.resolveRawValue(rawValueItem.getValue(), dialect);
                if (omitEnabled) {
                    charCount += stringValue == null ? 4 : stringValue.length();
                    lineCount += stringValue == null ? 0 : IndentTools.countNewlineChars(stringValue);
                } else {
                    sql.append(stringValue);
                }
            } else if (item instanceof OmitItem) {
                OmitItem omitItem = (OmitItem) item;
                if (omitItem.enabled() != omitEnabled) {
                    if (!omitItem.enabled() && charCount > 0) {
                        // 插入省略信息
                        insertOmittedDetails(sql, charCount, lineCount);
                    }
                    lineCount = 0;
                    charCount = 0;
                    omitEnabled = omitItem.enabled();
                }
            } else {
                throw new UnsupportedOperationException("Unsupported item: " + item.getClass());
            }
        }
        return sql.toString();
    }

    protected void insertOmittedDetails(StringBuilder sql, int charCount, int lineCount) {
        // 计算省略掉的行数和字符数信息
        // /* 10000 chars, 100 lines are omitted here ... */
        StringBuilder msg = generateOmittedDetails(charCount, lineCount);
        // 在最后一个换行符之后插入省略信息
        IndentTools.insertMessageAfterLastNewline(sql, msg);
    }

    // 生成省略掉的行数和字符数详细描述
    // /* 10000 chars, 100 lines are omitted here ... */
    protected StringBuilder generateOmittedDetails(int charCount, int lineCount) {
        StringBuilder msg = new StringBuilder();
        msg.append("/*").append(' ');
        msg.append(charCount).append(' ').append("chars");
        if (lineCount > 0) {
            msg.append(',').append(' ').append(lineCount).append(' ').append("lines");
        }
        msg.append(' ').append("are omitted here").append(' ').append("...").append(' ').append("*/");
        return msg;
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
    
    protected List<Item> items() {
        return this.buffer;
    }

    /** 自动追加空格 **/
    protected SqlBuffer autoAppendWhitespace(String part) {
        VerifyTools.requireNonNull(part, "part");
        // 1. 空白后/符号后(右括号除外)不加
        // 2. 空白前/符号前不加
        // 3. 两个单词放一块时要加
        // -- '作为单词而不是符号, SELECT '0' AS price

        if (this.isEmpty() || part.isEmpty()) {
            return this;
        }
        // 左侧是除右括号以外的空白或符号, 或者右侧是空白或符号, 不需要加空格
        if (TextTools.endsWithSqlSymbol(this, ')') || TextTools.startsWithSqlSymbol(part)) {
            return this;
        }
        this.append(' ');

        // TODO
        // 运算符, 如果前面有空格, 则后面也加一个空格
        // 换行时, 前面的未闭合的左括号前加一个空格
        // 右括号前, 如果在本行找不到成对的左括号时, 加一个空格
        // 右括号后, 如果右括号前有空格, 后面也加一个空格
        return this;
    }

    /** 自动追加空格 **/
    protected SqlBuffer autoAppendWhitespace(SqlBuffer part) {
        VerifyTools.requireNonNull(part, "part");
        if (this.isEmpty() || part.isEmpty()) {
            return this;
        }
        // 左侧是除右括号以外的空白或符号, 或者右侧是空白或符号, 不需要加空格
        if (TextTools.endsWithSqlSymbol(this, ')') || TextTools.startsWithSqlSymbol(part)) {
            return this;
        }
        this.append(' ');
        return this;
    }

    /** 自动在前面添加空格 **/
    protected SqlBuffer autoPrependWhitespace(String part) {
        VerifyTools.requireNonNull(part, "part");
        if (this.isEmpty() || part.isEmpty()) {
            return this;
        }
        // 左侧是除右括号以外的空白或符号, 或者右侧是空白或符号, 不需要加空格
        if (TextTools.endsWithSqlSymbol(part, ')') || TextTools.startsWithSqlSymbol(this)) {
            return this;
        }
        this.prepend(' ');
        return this;
    }

    /** 自动在前面添加空格 **/
    protected SqlBuffer autoPrependWhitespace(SqlBuffer part) {
        VerifyTools.requireNonNull(part, "part");
        if (this.isEmpty() || part.isEmpty()) {
            return this;
        }
        // 左侧是除右括号以外的空白或符号, 或者右侧是空白或符号, 不需要加空格
        if (TextTools.endsWithSqlSymbol(part, ')') || TextTools.startsWithSqlSymbol(this)) {
            return this;
        }
        this.prepend(' ');
        return this;
    }

    /** 查找最后的缩进量 **/
    protected int findLastIndentSize() {
        return TextTools.findLastIndentSize(this);
    }

    /** 清除最后的文字后面的缩进空白(返回清除了几个缩进量) **/
    protected int clearTrailingIndentWhitespace() {
        return TextTools.clearTrailingIndentWhitespace(this);
    }

    protected static interface Item {
    }

    /**
     * 超级长的SQL在输出日志时可以省略掉一部分<br>
     * 省略哪一部分, 用OmitItem来标识起止位置
     *
     * @author zhaohuihua
     * @version 20200712
     */
    protected static class OmitItem implements Item, Serializable {

        /** SerialVersionUID **/
        private static final long serialVersionUID = 1L;
        private final boolean enabled;

        public OmitItem(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean enabled() {
            return enabled;
        }
    }

    protected static class RawValueItem implements Item, Serializable {

        /** SerialVersionUID **/
        private static final long serialVersionUID = 1L;
        private String value;

        public RawValueItem(String value) {
            VerifyTools.requireNonNull(value, "value");
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    protected static class StringItem implements Item, Serializable {

        /** SerialVersionUID **/
        private static final long serialVersionUID = 1L;

        private final StringBuilder value;

        public StringItem() {
            this.value = new StringBuilder();
        }

        public void append(char... chars) {
            this.value.append(chars);
        }

        public void append(String value) {
            VerifyTools.requireNonNull(value, "value");
            this.value.append(value);
        }

        public void append(String value, char suffix) {
            VerifyTools.requireNonNull(value, "value");
            this.value.append(value).append(suffix);
        }

        public void append(char prefix, String value) {
            VerifyTools.requireNonNull(value, "value");
            this.value.append(prefix).append(value);
        }

        public void append(char prefix, String value, char suffix) {
            VerifyTools.requireNonNull(value, "value");
            this.value.append(prefix).append(value).append(suffix);
        }

        public void prepend(char... chars) {
            this.value.insert(0, chars);
        }

        public void prepend(String value) {
            VerifyTools.requireNonNull(value, "value");
            this.value.insert(0, value);
        }

        public void prepend(String value, char suffix) {
            VerifyTools.requireNonNull(value, "value");
            this.value.insert(0, suffix).insert(0, value);
        }

        public void prepend(char prefix, String value) {
            VerifyTools.requireNonNull(value, "value");
            this.value.insert(0, value).insert(0, prefix);
        }

        public void prepend(char prefix, String value, char suffix) {
            VerifyTools.requireNonNull(value, "value");
            this.value.insert(0, suffix).insert(0, value).insert(0, prefix);
        }

        /** 缩进TAB(只在换行符后面增加TAB) **/
        public void indentAll(char[] tabs) {
            for (int i = value.length() - 1; i >= 0; i--) {
                if (value.charAt(i) == '\n') {
                    value.insert(i + 1, tabs);
                }
            }
        }

        public StringBuilder getValue() {
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
