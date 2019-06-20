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
        this.buffer.add(new CharItem(part));
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
        this.buffer.add(0, new CharItem(part));
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
            } else if (item instanceof CharItem) {
                target.append(((CharItem) item).getValue());
            } else if (item instanceof StringItem) {
                StringItem stringItem = (StringItem) item;
                target.append(stringItem.getPrefix(), stringItem.getValue(), stringItem.getSuffix());
            }
        }
    }

    /** SQL语句(命名变量) **/
    public String getNamedSqlString() {
        StringBuilder temp = new StringBuilder();
        for (Object item : this.buffer) {
            if (item instanceof VarItem) {
                temp.append(':').append(((VarItem) item).getKey());
            } else if (item instanceof CharItem) {
                temp.append(((CharItem) item).getValue());
            } else if (item instanceof StringItem) {
                StringItem stringItem = (StringItem) item;
                if (stringItem.getPrefix() != 0) {
                    temp.append(stringItem.getPrefix());
                }
                temp.append(stringItem.getValue());
                if (stringItem.getSuffix() != 0) {
                    temp.append(stringItem.getSuffix());
                }
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
            } else if (item instanceof CharItem) {
                sql.append(((CharItem) item).getValue());
            } else if (item instanceof StringItem) {
                StringItem stringItem = (StringItem) item;
                if (stringItem.getPrefix() != 0) {
                    sql.append(stringItem.getPrefix());
                }
                sql.append(stringItem.getValue());
                if (stringItem.getSuffix() != 0) {
                    sql.append(stringItem.getSuffix());
                }
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

    protected static class CharItem implements Item, Serializable {

        /** SerialVersionUID **/
        private static final long serialVersionUID = 1L;

        private char[] value;

        public CharItem(char... value) {
            this.value = value;
        }

        public char[] getValue() {
            return this.value;
        }

        public String toString() {
            return new String(this.value);
        }
    }

    protected static class StringItem implements Item, Serializable {

        /** SerialVersionUID **/
        private static final long serialVersionUID = 1L;

        private final String value;
        private final char prefix;
        private final char suffix;

        public StringItem(String value) {
            this.value = value;
            this.prefix = 0;
            this.suffix = 0;
        }

        public StringItem(String value, char suffix) {
            this.value = value;
            this.prefix = 0;
            this.suffix = suffix;
        }

        public StringItem(char prefix, String value) {
            this.value = value;
            this.prefix = prefix;
            this.suffix = 0;
        }

        public StringItem(char prefix, String value, char suffix) {
            this.value = value;
            this.prefix = prefix;
            this.suffix = suffix;
        }

        public String getValue() {
            return this.value;
        }

        public char getPrefix() {
            return prefix;
        }

        public char getSuffix() {
            return suffix;
        }

        public String toString() {
            if (this.prefix == 0 && this.suffix == 0) {
                return this.value;
            }
            StringBuilder sb = new StringBuilder();
            if (this.prefix != 0) {
                sb.append(this.prefix);
            }
            sb.append(this.value);
            if (this.suffix != 0) {
                sb.append(this.suffix);
            }
            return sb.toString();
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
