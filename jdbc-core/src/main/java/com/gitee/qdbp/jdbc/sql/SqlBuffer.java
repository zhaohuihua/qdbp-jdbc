package com.gitee.qdbp.jdbc.sql;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.gitee.qdbp.able.jdbc.model.DbFieldName;
import com.gitee.qdbp.jdbc.model.DbType;
import com.gitee.qdbp.jdbc.model.DbVersion;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.utils.DbTools;

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
        } else if (value instanceof DbFieldName) {
            append(value.toString());
        } else {
            this.buffer.add(new VariableItem(index++, value));
        }
        return this;
    }

    public boolean isEmpty() {
        return buffer.isEmpty();
    }

    /** 缩进1个TAB **/
    public SqlBuffer indent() {
        return indent(1, true);
    }

    /**
     * 缩进n个TAB
     * 
     * @param size 缩进多少个TAB
     * @param leading 开头要不要缩进
     * @return 返回当前SQL容器用于连写
     */
    public SqlBuffer indent(int size, boolean leading) {
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
    public String getPreparedSqlString() {
        StringBuilder temp = new StringBuilder();
        for (Object item : this.buffer) {
            if (item instanceof VariableItem) {
                temp.append(':').append(((VariableItem) item).getKey());
            } else if (item instanceof StringItem) {
                StringItem stringItem = (StringItem) item;
                temp.append(stringItem.getValue());
            } else {
                throw new UnsupportedOperationException("Unsupported item: " + item.getClass());
            }
        }
        return temp.toString();
    }

    /** 获取预编译SQL参数 **/
    public Map<String, Object> getPreparedVariables() {
        Map<String, Object> map = new HashMap<>();
        for (Object item : this.buffer) {
            if (item instanceof VariableItem) {
                VariableItem variable = ((VariableItem) item);
                map.put(variable.getKey(), DbTools.variableToDbValue(variable.getValue()));
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
            if (item instanceof VariableItem) {
                VariableItem variable = ((VariableItem) item);
                sql.append(DbTools.variableToString(variable.value, dialect));
            } else if (item instanceof StringItem) {
                StringItem stringItem = (StringItem) item;
                sql.append(stringItem.getValue());
            } else {
                throw new UnsupportedOperationException("Unsupported item: " + item.getClass());
            }
        }
        return sql.toString();
    }

    public String toString() {
        SqlDialect dialect = DbTools.buildSqlDialect(new DbVersion(DbType.Oracle));
        return getExecutableSqlString(dialect);
    }

    protected static interface Item {
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
            return "$" + index;
        }

        public Object getValue() {
            return this.value;
        }

        public String toString() {
            return this.getKey();
        }
    }
}
