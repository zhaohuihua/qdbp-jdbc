package com.gitee.qdbp.jdbc.sql;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import com.gitee.qdbp.able.jdbc.model.DbFieldName;
import com.gitee.qdbp.able.jdbc.model.DbRawValue;
import com.gitee.qdbp.jdbc.model.DbVersion;
import com.gitee.qdbp.jdbc.model.MainDbType;
import com.gitee.qdbp.jdbc.model.OmitStrategy;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.utils.DbTools;
import com.gitee.qdbp.tools.utils.ConvertTools;
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
        this();
        this.append(sql);
    }

    /** 构造函数 **/
    public SqlBuffer(SqlBuffer sql) {
        this();
        this.append(sql);
    }

    /** 构造函数 **/
    protected SqlBuffer(SqlBuilder shortcut) {
        this();
        this.shortcut = shortcut;
    }

    /** 返回当前实例的快捷方式实例 **/
    public SqlBuilder shortcut() {
        if (this.shortcut == null) {
            this.shortcut = new SqlBuilder(this);
        }
        return this.shortcut;
    }

    /** 清除内容 **/
    public void clear() {
        this.buffer.clear();
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
        if (value == null) {
            this.buffer.add(new VariableItem(index++, value));
        } else if (value instanceof SqlBuffer) {
            append((SqlBuffer) value);
        } else if (value instanceof SqlBuilder) {
            append(((SqlBuilder) value).out());
        } else if (value instanceof DbRawValue) {
            DbRawValue raw = (DbRawValue) value;
            this.buffer.add(new RawValueItem(raw.toString()));
        } else if (value instanceof Collection) {
            this.addVariables(new ArrayList<>((Collection<?>) value));
        } else if (value.getClass().isArray()) {
            this.addVariables(ConvertTools.parseList(value));
        } else if (value instanceof DbFieldName) {
            // this.append(value.toString());
            // 缺少环境数据, 无法将字段名转换为列名
            throw new IllegalArgumentException("CanNotSupportedVariableType: DbFieldName");
        } else {
            this.buffer.add(new VariableItem(index++, value));
        }
        return this;
    }

    private void addVariables(List<?> values) {
        List<?> items = SqlTools.duplicateRemoval(values);
        OmitStrategy omits = DbTools.getOmitSizeConfig("qdbc.in.sql.omitStrategy", "50:5");
        for (int i = 0, count = items.size(); i < count; i++) {
            Object value = items.get(i);
            if (i > 0) {
                this.append(',');
            }
            if (omits.getMinSize() > 0 && count > omits.getMinSize()) {
                this.tryOmit(i, count, omits.getKeepSize());
            }
            this.buffer.add(new VariableItem(index++, value));
        }
    }

    /** 删除左右两则的空白字符 **/
    public SqlBuffer trim() {
        trimLeft();
        trimRight();
        return this;
    }

    /** 删除左侧空白 **/
    private void trimLeft() {
        if (buffer.isEmpty()) {
            return;
        }
        for (int i = 0, last = buffer.size() - 1; i <= last; i++) {
            Item item = buffer.get(i);
            if (item instanceof StringItem) {
                StringItem stringItem = (StringItem) item;
                int size = stringItem.trimLeft();
                if (size == 0) {
                    continue;
                }
            }
            return;
        }
    }

    /** 删除右侧空白 **/
    private void trimRight() {
        if (buffer.isEmpty()) {
            return;
        }
        for (int i = buffer.size() - 1; i >= 0; i--) {
            Item item = buffer.get(i);
            if (item instanceof StringItem) {
                StringItem stringItem = (StringItem) item;
                int size = stringItem.trimRight();
                if (size == 0) {
                    continue;
                }
            }
            return;
        }
    }

    /**
     * 在第1个非空字符前插入前缀<br>
     * 删除prefixOverrides, 插入prefix<br>
     * 如果内容全部为空, 将不会做任何处理, 返回false<br>
     * 如果prefix为空, 又没有找到prefixOverrides, 也会返回false
     * 
     * @param prefix 待插入的前缀
     * @param prefixOverrides 待替换的前缀, 不区分大小写, 支持以|拆分的多个前缀, 如AND|OR
     * @return 是否有变更
     */
    public boolean insertPrefix(String prefix, String prefixOverrides) {
        if (buffer.isEmpty() || VerifyTools.isAllBlank(prefix, prefixOverrides)) {
            return false;
        }
        for (int i = 0, last = buffer.size() - 1; i <= last; i++) {
            Item item = buffer.get(i);
            if (item instanceof StringItem) {
                StringItem stringItem = (StringItem) item;
                boolean changed = stringItem.insertPrefix(prefix, prefixOverrides);
                if (changed) {
                    return true;
                }
            } else if (item instanceof VariableItem || item instanceof RawValueItem) {
                if (VerifyTools.isBlank(prefix)) {
                    return false;
                }
                StringItem element = new StringItem();
                element.append(prefix);
                if (!SqlTextTools.endsWithSqlSymbol(prefix, ')')) {
                    element.append(' ');
                }
                buffer.add(i, element);
                return true;
            } else if (item instanceof OmitItem) {
                continue;
            } else {
                throw new UnsupportedOperationException("Unsupported item: " + item.getClass());
            }
        }
        return false;
    }

    /**
     * 在最后1个非空字符后插入后缀<br>
     * 删除suffixOverrides, 插入suffix<br>
     * 如果内容全部为空, 将不会做任何处理, 返回false<br>
     * 如果suffix为空, 又没有找到suffixOverrides, 也会返回false
     * 
     * @param suffix 待插入的后缀
     * @param suffixOverrides 待替换的后缀, 不区分大小写, 支持以|拆分的多个前缀, 如AND|OR
     * @return 是否有变更
     */
    public boolean insertSuffix(String suffix, String suffixOverrides) {
        if (buffer.isEmpty() || VerifyTools.isAllBlank(suffix, suffixOverrides)) {
            return false;
        }
        for (int i = buffer.size() - 1; i >= 0; i--) {
            Item item = buffer.get(i);
            if (item instanceof StringItem) {
                StringItem stringItem = (StringItem) item;
                boolean changed = stringItem.insertSuffix(suffix, suffixOverrides);
                if (changed) {
                    return true;
                }
            } else if (item instanceof VariableItem || item instanceof RawValueItem) {
                if (VerifyTools.isBlank(suffix)) {
                    return false;
                }
                StringItem element = new StringItem();
                if (!SqlTextTools.startsWithSqlSymbol(suffix)) {
                    element.append(' ');
                }
                element.append(suffix);
                buffer.add(i + 1, element);
                return true;
            } else if (item instanceof OmitItem) {
                continue;
            } else {
                throw new UnsupportedOperationException("Unsupported item: " + item.getClass());
            }
        }
        return false;
    }

    public boolean isEmpty() {
        return buffer.isEmpty();
    }

    public boolean isBlank() {
        if (buffer.isEmpty()) {
            return true;
        }
        for (Item item : buffer) {
            if (item instanceof StringItem) {
                StringItem stringItem = (StringItem) item;
                String stringValue = stringItem.getValue().toString();
                if (stringValue.trim().length() > 0) {
                    return false;
                }
            } else if (item instanceof VariableItem) {
                return false;
            } else if (item instanceof RawValueItem) {
                RawValueItem rawValueItem = (RawValueItem) item;
                String stringValue = rawValueItem.getValue();
                if (stringValue.trim().length() > 0) {
                    return false;
                }
            } else if (item instanceof OmitItem) {
                continue;
            } else {
                throw new UnsupportedOperationException("Unsupported item: " + item.getClass());
            }
        }
        return true;
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
        this.buffer.add(new OmitItem(false));
        return this;
    }

    /**
     * 传入size和当前index, 自动计算, 插入省略标记<br>
     * 日志只取前3行+后3行; 因此在第3行后面开始省略, 在后3行之前结束省略<br>
     * 注意: 如果有换行符, 最好放在换行符后面
     * 
     * @param index 当前行数
     * @param count 总行数
     * @param limit 保留多少行(前后各保留limit行)
     * @return 返回当前SQL容器用于连写
     */
    public SqlBuffer tryOmit(int index, int count) {
        return tryOmit(index, count, 3);
    }

    /**
     * 传入size和当前index, 自动计算, 插入省略标记<br>
     * 日志只取前3行+后3行; 因此在第3行后面开始省略, 在后3行之前结束省略<br>
     * 注意: 如果有换行符, 最好放在换行符后面
     * 
     * @param index 当前行数(从0开始)
     * @param count 总行数
     * @param limit 保留多少行(前后各保留limit行)
     * @return 返回当前SQL容器用于连写
     */
    public SqlBuffer tryOmit(int index, int count, int limit) {
        if (count <= limit * 2 + 2) {
            // 如果总行数只比保留行数多2行, 就没必要省略了
            return this;
        }
        if (index == limit) {
            this.buffer.add(new OmitItem(true, index)); // 开始省略
        } else if (index == count - limit) {
            this.buffer.add(new OmitItem(false, index)); // 结束省略
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
                target.buffer.add(new OmitItem(omitItem.enabled(), omitItem.index()));
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
        return sqlFormatToString(sql);
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
        return sqlFormatToString(sql);
    }

    /**
     * 获取用于日志输出的SQL语句(预编译参数替换为拼写式参数)<br>
     * 如果参数值长度超过100会被截断(例如大片的HTML富文本代码等)<br>
     * 批量SQL会省略部分语句不输出到日志中(几百几千个批量操作会导致SQL太长)
     * 
     * @param version 数据库版本
     * @return SQL语句
     */
    public String getLoggingSqlString(DbVersion version) {
        return getLoggingSqlString(DbTools.buildSqlDialect(version), true);
    }

    /**
     * 获取用于日志输出的SQL语句(预编译参数替换为拼写式参数)<br>
     * 如果参数值长度超过100会被截断(例如大片的HTML富文本代码等)<br>
     * 批量SQL会省略部分语句不输出到日志中(几百几千个批量操作会导致SQL太长)
     * 
     * @param dialect 数据库方言
     * @return SQL语句
     */
    public String getLoggingSqlString(SqlDialect dialect) {
        return getLoggingSqlString(dialect, true);
    }

    /**
     * 获取用于日志输出的SQL语句(预编译参数替换为拼写式参数)
     * 
     * @param version 数据库版本
     * @param omitMode 是否使用省略模式<br>
     *            如果参数值长度超过100会被截断(例如大片的HTML富文本代码等);<br>
     *            批量SQL会省略部分语句不输出到日志中(几百几千个批量操作会导致SQL太长)
     * @return SQL语句
     */
    public String getLoggingSqlString(DbVersion version, boolean omitMode) {
        return getLoggingSqlString(DbTools.buildSqlDialect(version), omitMode);
    }

    /**
     * 获取用于日志输出的SQL语句(预编译参数替换为拼写式参数)
     * 
     * @param dialect 数据库方言
     * @param omitMode 是否使用省略模式<br>
     *            如果参数值长度超过100会被截断(例如大片的HTML富文本代码等);<br>
     *            批量SQL会省略部分语句不输出到日志中(几百几千个批量操作会导致SQL太长)
     * @return SQL语句
     */
    public String getLoggingSqlString(SqlDialect dialect, boolean omitMode) {
        int valueLimit = 100;
        StringBuilder sql = new StringBuilder();
        int charCount = 0;
        int lineCount = 0;
        // 解决OmitItem嵌套的问题
        Stack<OmitItem> omitStacks = new Stack<>();
        for (Item item : this.buffer) {
            if (item instanceof StringItem) {
                StringItem stringItem = (StringItem) item;
                String stringValue = stringItem.getValue().toString();
                if (omitMode && !omitStacks.isEmpty()) { // 省略模式下, 只需要统计数量
                    charCount += stringValue == null ? 4 : stringValue.length();
                    lineCount += stringValue == null ? 0 : IndentTools.countNewlineChars(stringValue);
                } else {
                    sql.append(stringValue);
                }
            } else if (item instanceof VariableItem) {
                VariableItem variable = ((VariableItem) item);
                String stringValue = DbTools.variableToString(variable.value, dialect);
                if (omitMode && !omitStacks.isEmpty()) { // 省略模式下, 只需要统计数量
                    charCount += stringValue == null ? 4 : stringValue.length();
                    lineCount += stringValue == null ? 0 : IndentTools.countNewlineChars(stringValue);
                } else {
                    if (omitMode) { // 省略模式下, 截短超过长度的字段值
                        stringValue = tryCutStringOverlength(stringValue, valueLimit);
                    }
                    sql.append(stringValue);
                    sql.append("/*").append(variable.getKey()).append("*/");
                }
            } else if (item instanceof RawValueItem) {
                RawValueItem rawValueItem = (RawValueItem) item;
                String stringValue = DbTools.resolveRawValue(rawValueItem.getValue(), dialect);
                if (omitMode && !omitStacks.isEmpty()) { // 省略模式下, 只需要统计数量
                    charCount += stringValue == null ? 4 : stringValue.length();
                    lineCount += stringValue == null ? 0 : IndentTools.countNewlineChars(stringValue);
                } else {
                    sql.append(stringValue);
                }
            } else if (item instanceof OmitItem) {
                if (!omitMode) {
                    continue;
                }
                boolean omitEnabled = !omitStacks.isEmpty();
                OmitItem omitItem = (OmitItem) item;
                OmitItem srartItem = null;
                // 开始标记入栈, 结束标记出栈
                if (omitItem.enabled()) {
                    omitStacks.add(omitItem);
                } else {
                    if (omitStacks.isEmpty()) {
                        // 结束标记多于开始标记, 不作处理
                    } else {
                        srartItem = omitStacks.pop();
                    }
                }
                // OmitItem堆栈之前不是空的, 现在变空了, 说明已经回到顶层且结束了
                if (omitEnabled && omitStacks.isEmpty()) {
                    if (charCount > 0) { // 插入省略信息
                        int itemCount = calcOmittedIndex(srartItem, omitItem);
                        insertOmittedDetails(sql, charCount, lineCount, itemCount);
                    }
                    // 统计信息归零
                    lineCount = 0;
                    charCount = 0;
                }
            } else {
                throw new UnsupportedOperationException("Unsupported item: " + item.getClass());
            }
        }
        // 到最后了, OmitItem还不是空的, 说明开始标记多于结束标记
        if (!omitStacks.isEmpty() && charCount > 0) { // 插入省略信息
            insertOmittedDetails(sql, charCount, lineCount, -1);
        }
        return sqlFormatToString(sql);
    }

    /** 替换\t为4个空格, 替换\r\n为\n, 替换单独的\r为\n; 清除末尾的空白 **/
    protected String sqlFormatToString(StringBuilder sql) {
        StringTools.replace(sql, "\t", "    ", "\r\n", "\n", "\r", "\n");
        // 清除末尾的空白
        int size = sql.length();
        int lastIndex = size;
        // 从最后开始, 判断前一个字符是不是空白
        for (int i = size; i > 0; i--) {
            char c = sql.charAt(i - 1); // 判断前一个字符
            if (!StringTools.isAsciiWhitespace(c)) {
                lastIndex = i;
                break;
            }
        }
        if (lastIndex < size) {
            sql.setLength(lastIndex);
        }
        return sql.toString();
    }

    protected void insertOmittedDetails(StringBuilder sql, int charCount, int lineCount, int itemCount) {
        // 计算省略掉的行数和字符数信息
        // /* 10000 chars, 100 lines, 50 items are omitted here ... */
        StringBuilder msg = generateOmittedDetails(charCount, lineCount, itemCount);
        // 在最后一个换行符之后插入省略信息
        IndentTools.insertMessageAfterLastNewline(sql, msg);
    }

    // 生成省略掉的行数和字符数详细描述
    // /* 10000 chars, 100 lines, 50 items are omitted here ... */
    protected StringBuilder generateOmittedDetails(int charCount, int lineCount, int itemCount) {
        StringBuilder msg = new StringBuilder();
        msg.append("/*").append(' ');
        msg.append(charCount).append(' ').append("chars");
        if (lineCount > 0) {
            msg.append(',').append(' ').append(lineCount).append(' ').append("lines");
        }
        if (itemCount > 0) {
            msg.append(',').append(' ').append(itemCount).append(' ').append("items");
        }
        msg.append(' ').append("are omitted here").append(' ').append("...").append(' ').append("*/");
        return msg;
    }

    private int calcOmittedIndex(OmitItem start, OmitItem end) {
        if (start == null || end == null) {
            return -1;
        }
        if (start.index() < 0 || end.index() < 0 || start.index() > end.index()) {
            return -1;
        }
        return end.index() - start.index();
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
        return getLoggingSqlString(dialect, true);
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
        if (SqlTextTools.endsWithSqlSymbol(this, ')') || SqlTextTools.startsWithSqlSymbol(part)) {
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
        if (SqlTextTools.endsWithSqlSymbol(this, ')') || SqlTextTools.startsWithSqlSymbol(part)) {
            return this;
        }
        this.append(' ');
        return this;
    }

    /** 自动追加空格 **/
    protected SqlBuffer autoAppendWhitespace() {
        if (this.isEmpty()) {
            return this;
        }
        if (SqlTextTools.endsWithSqlSymbol(this, ')')) {
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
        if (SqlTextTools.endsWithSqlSymbol(part, ')') || SqlTextTools.startsWithSqlSymbol(this)) {
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
        if (SqlTextTools.endsWithSqlSymbol(part, ')') || SqlTextTools.startsWithSqlSymbol(this)) {
            return this;
        }
        this.prepend(' ');
        return this;
    }

    /** 查找最后的缩进量 **/
    protected int findLastIndentSize() {
        return SqlTextTools.findLastIndentSize(this);
    }

    /** 清除最后的文字后面的缩进空白(返回清除了几个缩进量) **/
    protected int clearTrailingIndentWhitespace() {
        return SqlTextTools.clearTrailingIndentWhitespace(this);
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
        private final int index;

        public OmitItem(boolean enabled) {
            this.enabled = enabled;
            this.index = -1;
        }

        public OmitItem(boolean enabled, int index) {
            this.enabled = enabled;
            this.index = index;
        }

        public boolean enabled() {
            return enabled;
        }

        public int index() {
            return index;
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

        /**
         * 删除左侧空白
         * 
         * @return 删除后剩下多少字符
         */
        public int trimLeft() {
            return trimLeftRight(true, false);
        }

        /**
         * 删除右侧空白
         * 
         * @return 删除后剩下多少字符
         */
        public int trimRight() {
            return trimLeftRight(false, true);
        }

        private int trimLeftRight(boolean trimLeft, boolean trimRight) {
            if (value == null || value.length() == 0) {
                return 0;
            }

            int size = value.length();
            char[] chars = value.toString().toCharArray();

            if (trimLeft) {
                int start = 0;
                while ((start < size) && (chars[start] <= ' ')) {
                    start++;
                }
                if (start > 0) {
                    value.delete(0, start);
                }
            }
            if (trimRight) {
                int end = size;
                while ((end > 0) && (chars[end - 1] <= ' ')) {
                    end--;
                }
                if (end < size) {
                    value.delete(end, size);
                }
            }
            return value.length();
        }

        /** 缩进TAB(只在换行符后面增加TAB) **/
        public void indentAll(char[] tabs) {
            for (int i = value.length() - 1; i >= 0; i--) {
                if (value.charAt(i) == '\n') {
                    value.insert(i + 1, tabs);
                }
            }
        }

        /**
         * 在第1个非空字符前插入前缀<br>
         * 删除prefixOverrides, 插入prefix<br>
         * 如果内容全部为空, 将不会做任何处理, 返回false<br>
         * 如果prefix为空, 又没有找到prefixOverrides, 也会返回false
         * 
         * @param prefix 待插入的前缀
         * @param prefixOverrides 待替换的前缀, 不区分大小写, 支持以|拆分的多个前缀, 如AND|OR
         * @return 是否有变更
         */
        public boolean insertPrefix(String prefix, String prefixOverrides) {
            if (value.length() == 0) {
                return false;
            }

            int last = value.length() - 1;
            int position = -1;
            // 查找插入点: 第1个非空字符的位置
            for (int i = 0; i <= last; i++) {
                char c = value.charAt(i);
                if (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
                    continue;
                } else {
                    position = i;
                    break; // 位置找到了
                }
            }
            if (position < 0) {
                return false;
            }
            boolean removed = false;
            boolean changed = false;
            if (VerifyTools.isNotBlank(prefixOverrides)) {
                String[] array = StringTools.split(prefixOverrides, '|');
                for (String prefixOverride : array) {
                    if (startsWith(value, prefixOverride, position)) {
                        value.delete(position, position + prefixOverride.length());
                        removed = true;
                        changed = true;
                        break;
                    }
                }
            }
            if (VerifyTools.isNotBlank(prefix)) {
                changed = true;
                if (removed) {
                    value.insert(position, prefix);
                } else {
                    String suffix = value.substring(position, Math.min(position + 10, value.length()));
                    if (SqlTextTools.endsWithSqlSymbol(prefix, ')') || SqlTextTools.startsWithSqlSymbol(suffix)) {
                        value.insert(position, prefix);
                    } else {
                        value.insert(position, prefix + ' ');
                    }
                }
            }
            return changed;
        }

        /**
         * 在最后1个非空字符后插入后缀<br>
         * 删除suffixOverrides, 插入suffix<br>
         * 如果内容全部为空, 将不会做任何处理, 返回false<br>
         * 如果suffix为空, 又没有找到suffixOverrides, 也会返回false
         * 
         * @param suffix 待插入的后缀
         * @param suffixOverrides 待替换的后缀, 不区分大小写, 支持以|拆分的多个前缀, 如AND|OR
         * @return 是否有变更
         */
        public boolean insertSuffix(String suffix, String suffixOverrides) {
            if (value.length() == 0) {
                return false;
            }

            int last = value.length() - 1;
            int position = -1;
            // 查找插入点: 第1个非空字符之后的那个位置
            for (int i = last; i >= 0; i--) {
                char c = value.charAt(i);
                if (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
                    continue;
                } else {
                    position = i + 1;
                    break; // 位置找到了
                }
            }
            // 停在第1个非空字符之后, 所以不能等于0
            if (position <= 0) {
                return false;
            }
            boolean removed = false;
            boolean changed = false;
            if (VerifyTools.isNotBlank(suffixOverrides)) {
                String[] array = StringTools.split(suffixOverrides, '|');
                for (String suffixOverride : array) {
                    if (endsWith(value, suffixOverride, position)) {
                        value.delete(position - suffixOverride.length(), position);
                        position -= suffixOverride.length();
                        removed = true;
                        changed = true;
                        break;
                    }
                }
            }
            if (VerifyTools.isNotBlank(suffix)) {
                changed = true;
                if (removed) {
                    value.insert(position, suffix);
                } else {
                    String prefix = value.substring(Math.max(position - 10, 0), position);
                    if (SqlTextTools.endsWithSqlSymbol(prefix, ')') || SqlTextTools.startsWithSqlSymbol(suffix)) {
                        value.insert(position, suffix);
                    } else {
                        value.insert(position, ' ' + suffix);
                    }
                }
            }
            return changed;
        }

        private static boolean startsWith(CharSequence string, String prefix, int index) {
            if (string.length() < index + prefix.length()) {
                return false;
            }
            int i = 0;
            for (; i < prefix.length(); i++) {
                if (Character.toUpperCase(prefix.charAt(i)) != Character.toUpperCase(string.charAt(i + index))) {
                    return false;
                }
            }
            // 如果prefix是单词结尾, 则下一个字符必须不是单词
            // 例如替换OR, 遇到ORG_ID, 应判定为不匹配
            if (i + index < string.length() && SqlTextTools.isSqlWordChar(prefix.charAt(prefix.length() - 1))) {
                if (SqlTextTools.isSqlWordChar(string.charAt(i + index))) {
                    return false;
                }
            }
            return true;
        }

        private static boolean endsWith(CharSequence string, String suffix, int index) {
            int si = index - suffix.length();
            if (si < 0) {
                return false;
            }
            for (int i = 0; i < suffix.length(); i++) {
                if (Character.toUpperCase(suffix.charAt(i)) != Character.toUpperCase(string.charAt(si + i))) {
                    return false;
                }
            }
            // 如果suffix是单词开头, 则前一个字符必须不是单词
            if (si > 0 && SqlTextTools.isSqlWordChar(suffix.charAt(0))) {
                if (SqlTextTools.isSqlWordChar(string.charAt(si - 1))) {
                    return false;
                }
            }
            return true;
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
