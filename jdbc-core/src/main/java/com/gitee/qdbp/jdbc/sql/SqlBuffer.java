package com.gitee.qdbp.jdbc.sql;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.gitee.qdbp.jdbc.utils.DbTools;
import com.gitee.qdbp.tools.utils.NamingTools;
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
            if (item instanceof VarItem) {
                VarItem placeholder = ((VarItem) item);
                placeholder.index += offset;
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
        this.buffer.add(new VarItem(index++, value));
        return this;
    }

    /**
     * 增加变量, 同时将变量以占位符追加到SQL语句中
     * 
     * @param name 名称
     * @param value 变量
     * @return 返回当前SQL容器用于连写
     */
    public SqlBuffer addVariable(String name, Object value) {
        this.buffer.add(new VarItem(index++, name, value));
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
     * @param leading 开头要不要缩进, 如果不是完整SQL则开头不能缩进
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
                tabs[i] = '\n';
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
            if (item instanceof VarItem) {
                VarItem placeholder = ((VarItem) item);
                target.addVariable(placeholder.name, placeholder.value);
            } else if (item instanceof StringItem) {
                StringItem stringItem = (StringItem) item;
                target.buffer.add(stringItem);
            } else {
                throw new UnsupportedOperationException("Unsupported item: " + item.getClass());
            }
        }
    }

    /** 获取预编译SQL语句(命名变量) **/
    public String getPreparedSqlString() {
        StringBuilder temp = new StringBuilder();
        for (Object item : this.buffer) {
            if (item instanceof VarItem) {
                temp.append(':').append(((VarItem) item).getKey());
            } else if (item instanceof StringItem) {
                StringItem stringItem = (StringItem) item;
                temp.append(stringItem.getValue());
            } else {
                throw new UnsupportedOperationException("Unsupported item: " + item.getClass());
            }
        }
        return temp.toString();
    }

    /** 获取SQL命名变量 **/
    public Map<String, Object> getPreparedNamedVariables() {
        Map<String, Object> map = new HashMap<>();
        for (Object item : this.buffer) {
            if (item instanceof VarItem) {
                VarItem placeholder = ((VarItem) item);
                map.put(placeholder.getKey(), placeholder.getValue());
            }
        }
        return map;
    }

    public String toString() {
        return getNormalSqlString();
    }

    /** 获取普通SQL(不带预编译参数) **/
    public String getNormalSqlString() {
        StringBuilder sql = new StringBuilder();
        for (Object item : this.buffer) {
            if (item instanceof VarItem) {
                VarItem placeholder = ((VarItem) item);
                String value = DbTools.variableToString(placeholder.getValue());
                sql.append(value);
            } else if (item instanceof StringItem) {
                StringItem stringItem = (StringItem) item;
                sql.append(stringItem.getValue());
            } else {
                throw new UnsupportedOperationException("Unsupported item: " + item.getClass());
            }
        }
        return sql.toString();
    }

    private static final Pattern PLACEHOLDER = Pattern.compile("([\\$#])\\{([\\.\\w]+)\\}");

    /**
     * 从SQL模板解析获取SqlBuffer对象, 允许在占位符处插入另一个SqlBuffer片段<br>
     * #{fieldName}使用预编译方式, ${fieldName}使用SQL拼接方式
     * 
     * @param sqlTemplate SQL模板
     * @param params 占位符变量
     * @return SqlBuffer对象
     */
    public static SqlBuffer parse(String sqlTemplate, Map<String, Object> params) {
        SqlBuffer buffer = new SqlBuffer();
        Matcher matcher = PLACEHOLDER.matcher(sqlTemplate);
        int index = 0;
        while (matcher.find()) {
            if (index < matcher.start()) {
                buffer.append(sqlTemplate.substring(index, matcher.start()));
            }
            String prefix = matcher.group(1);
            String placeholder = matcher.group(2);
            Object value = getMapValue(params, placeholder);
            if ("$".equals(prefix)) {
                buffer.append(DbTools.variableToString(value));
            } else {
                if (value instanceof SqlBuffer) {
                    buffer.append((SqlBuffer) value);
                } else {
                    buffer.addVariable(value);
                }
            }
            index = matcher.end();
        }
        if (index < sqlTemplate.length()) {
            buffer.append(sqlTemplate.substring(index));
        }
        return buffer;
    }

    private static Object getMapValue(Map<String, Object> params, String key) {
        return params.get(key);
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

        /** 缩进TAB, 在换行符后面增加TAB **/
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

    protected static class VarItem implements Item, Serializable {

        /** SerialVersionUID **/
        private static final long serialVersionUID = 1L;

        private int index;
        private String name;
        private Object value;

        public VarItem(int index, Object value) {
            this.index = index;
            this.value = value;
        }

        public VarItem(int index, String name, Object value) {
            this.index = index;
            this.name = NamingTools.toCamelString(name, true);
            this.value = value;
        }

        public String getKey() {
            if (VerifyTools.isBlank(name)) {
                return "$" + index;
            } else {
                return "$" + index + "$" + name;
            }
        }

        public Object getValue() {
            return this.value;
        }

        public String toString() {
            return this.getKey();
        }
    }
}
