package com.gitee.qdbp.jdbc.sql;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.gitee.qdbp.tools.utils.DateTools;
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
        this.buffer.add(new StringItem(sql));
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
        this.buffer.add(new StringItem(part));
        return this;
    }

    /**
     * 将指定SQL片段追加到SQL后面
     * 
     * @param part SQL片段
     * @return 返回当前SQL容器用于连写
     */
    public SqlBuffer append(char... part) {
        this.buffer.add(new StringItem(part));
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
        this.buffer.add(new StringItem(part, suffix));
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
        this.buffer.add(new StringItem(prefix, part));
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
        this.buffer.add(new StringItem(prefix, part, suffix));
        return this;
    }

    /**
     * 将指定SQL片段增加到SQL语句最前面
     * 
     * @param part SQL片段
     * @return 返回当前SQL容器用于连写
     */
    public SqlBuffer prepend(String part) {
        this.buffer.add(0, new StringItem(part));
        return this;
    }

    /**
     * 将指定SQL片段增加到SQL语句最前面
     * 
     * @param part SQL片段
     * @return 返回当前SQL容器用于连写
     */
    public SqlBuffer prepend(char... part) {
        this.buffer.add(0, new StringItem(part));
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
        this.buffer.add(0, new StringItem(part, suffix));
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
        this.buffer.add(0, new StringItem(prefix, part));
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
        this.buffer.add(0, new StringItem(prefix, part, suffix));
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

    private StringItem tryGetLastStringItem() {
        if (this.buffer.isEmpty()) {
            return null;
        }
        Item last = this.buffer.get(this.buffer.size() - 1);
        return last instanceof StringItem ? (StringItem) last : null;
    }

    private StringItem tryGetFirstStringItem() {
        if (this.buffer.isEmpty()) {
            return null;
        }
        Item first = this.buffer.get(0);
        return first instanceof StringItem ? (StringItem) first : null;
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

    /** SQL语句(命名变量) **/
    public String getNamedSqlString() {
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

    /** SQL命名变量 **/
    public Map<String, Object> getNamedVariables() {
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
        StringBuilder sql = new StringBuilder();
        for (Object item : this.buffer) {
            if (item instanceof VarItem) {
                VarItem placeholder = ((VarItem) item);
                appendValue(sql, placeholder.getValue());
            } else if (item instanceof StringItem) {
                StringItem stringItem = (StringItem) item;
                sql.append(stringItem.getValue());
            } else {
                throw new UnsupportedOperationException("Unsupported item: " + item.getClass());
            }
        }
        return sql.toString();
    }

    protected void appendValue(StringBuilder sb, Object value) {
        if (value == null) {
            sb.append("NULL");
        } else if (value instanceof Number || value instanceof Boolean) {
            sb.append(value);
        } else if (value instanceof CharSequence) {
            sb.append("'").append(escapeSingleQuotation(value.toString())).append("'");
        } else if (value instanceof Character) {
            sb.append("'").append(value).append("'");
        } else if (value instanceof Date) {
            sb.append("'").append(DateTools.toNormativeString((Date) value)).append("'");
        } else if (value instanceof Enum) {
            sb.append(((Enum<?>) value).ordinal());
        } else {
            sb.append("'").append(escapeSingleQuotation(value.toString())).append("'");
        }
    }

    private String escapeSingleQuotation(String string) {
        return string.replace("'", "\\'");
    }

    /**
     * SQL模板的格式化处理, 允许在占位符中插入另一个SqlBuffer片段
     * 
     * @param sqlTemplate SQL模板
     * @param params 占位符变量
     * @return SqlBuffer对象
     */
    public static SqlBuffer format(String sqlTemplate, Map<String, Object> params) {
        throw new UnsupportedOperationException("TODO"); // TODO format sql template
    }

    protected static interface Item {
    }

    protected static class StringItem implements Item, Serializable {

        /** SerialVersionUID **/
        private static final long serialVersionUID = 1L;

        private final StringBuffer value;

        public StringItem(char... chars) {
            this.value = new StringBuffer().append(chars);
        }

        public StringItem(String value) {
            this.value = new StringBuffer(value);
        }

        public StringItem(String value, char suffix) {
            this.value = new StringBuffer(value).append(suffix);
        }

        public StringItem(char prefix, String value) {
            this.value = new StringBuffer().append(prefix).append(value);
        }

        public StringItem(char prefix, String value, char suffix) {
            this.value = new StringBuffer().append(prefix).append(value).append(suffix);
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
