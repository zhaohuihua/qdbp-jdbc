package com.gitee.qdbp.jdbc.sql;

import java.io.Serializable;
import com.gitee.qdbp.tools.utils.IndentTools;

/**
 * SQL生成器, 会自动追加空格<br>
 * 这是SqlBuffer的快捷方式, 使用短名称作为方法名(名称太长不方便连写)<br>
 * ad=append, pd=prepend, var=addVariable
 *
 * @author zhaohuihua
 * @version 20200713
 */
public class SqlBuilder implements Serializable {

    /** SerialVersionUID **/
    private static final long serialVersionUID = 1L;

    private SqlBuffer buffer;

    /** 构造函数 **/
    public SqlBuilder() {
        this.buffer = new SqlBuffer();
    }

    /** 构造函数 **/
    public SqlBuilder(String sql) {
        this.buffer = new SqlBuffer(sql);
    }

    /** 构造函数 **/
    public SqlBuilder(SqlBuffer buffer) {
        this.buffer = buffer;
    }

    /** 返回SqlBuffer实例 **/
    public SqlBuffer end() {
        return this.buffer;
    }

    /**
     * ad=append: 将指定SQL片段追加到SQL后面, 将会自动追加空格
     * 
     * @param parts SQL片段
     * @return 返回当前SQL容器用于连写
     */
    public SqlBuilder ad(String... parts) {
        if (parts != null) {
            for (String part : parts) {
                this.buffer.autoAppendWhitespace(part).append(part);
            }
        }
        return this;
    }

    /**
     * ad=append: 将指定SQL片段追加到SQL后面, 将会自动追加空格
     * 
     * @param parts SQL片段
     * @return 返回当前SQL容器用于连写
     */
    public SqlBuilder ad(char... parts) {
        if (parts != null) {
            this.buffer.append(parts);
        }
        return this;
    }

    /**
     * pd=prepend: 将指定SQL片段增加到SQL语句最前面, 将会自动追加空格
     * 
     * @param parts SQL片段
     * @return 返回当前SQL容器用于连写
     */
    public SqlBuilder pd(String... parts) {
        if (parts != null) {
            for (int i = parts.length - 1; i >= 0; i--) {
                String part = parts[i];
                this.buffer.autoPrependWhitespace(part).prepend(part);
            }
        }
        return this;
    }

    /**
     * pd=prepend: 将指定SQL片段增加到SQL语句最前面, 将会自动追加空格
     * 
     * @param parts SQL片段
     * @return 返回当前SQL容器用于连写
     */
    public SqlBuilder pd(char... parts) {
        if (parts != null) {
            this.buffer.prepend(parts);
        }
        return this;
    }

    /**
     * ad=append: 将指定SQL片段追加到SQL语句后面, 将会自动追加空格
     * 
     * @param buffer SQL片段
     * @return 返回当前SQL容器用于连写
     */
    public SqlBuilder ad(SqlBuffer buffer) {
        if (buffer != null && !buffer.isEmpty()) {
            int indent = this.buffer.findLastIndentSize();
            if (indent > 0) {
                buffer.indentAll(indent, false);
            }
            this.buffer.autoAppendWhitespace(buffer).append(buffer);
        }
        return this;
    }

    /**
     * ad=append: 将指定SQL片段追加到SQL语句后面, 将会自动追加空格
     * 
     * @param another SQL片段
     * @return 返回当前SQL容器用于连写
     */
    public SqlBuilder ad(SqlBuilder another) {
        return this.ad(another.buffer);
    }

    /**
     * pd=prepend: 将指定SQL片段增加到SQL语句最前面, 将会自动追加空格
     * 
     * @param buffer SQL片段
     * @return 返回当前SQL容器用于连写
     */
    public SqlBuilder pd(SqlBuffer buffer) {
        if (buffer != null && !buffer.isEmpty()) {
            int indent = buffer.findLastIndentSize();
            if (indent > 0) {
                this.buffer.indentAll(indent, false);
            }
            this.buffer.autoPrependWhitespace(buffer).prepend(buffer);
        }
        return this;
    }

    /**
     * pd=prepend: 将指定SQL片段增加到SQL语句最前面, 将会自动追加空格
     * 
     * @param another SQL片段
     * @return 返回当前SQL容器用于连写
     */
    public SqlBuilder pd(SqlBuilder another) {
        return this.pd(another.buffer);
    }

    /**
     * var=addVariable: 增加变量, 同时将变量以占位符追加到SQL语句中
     * 
     * @param value 变量
     * @return 返回当前SQL容器用于连写
     */
    public SqlBuilder var(Object value) {
        this.buffer.addVariable(value);
        return this;
    }

    /**
     * 增加换行
     * 
     * @return 返回当前SQL容器用于连写
     */
    public SqlBuilder br() {
        this.buffer.append('\n');
        return this;
    }

    /**
     * 增加换行, 将会自动保持上一行的缩进量
     * 
     * @return 返回当前SQL容器用于连写
     */
    public SqlBuilder newline() {
        this.buffer.append('\n');
        int size = this.buffer.findLastIndentSize();
        if (size > 0) {
            this.buffer.append(IndentTools.getIndenTabs(size));
        }
        return this.tab(0);
    }

    /**
     * 增加缩进
     * 
     * @return 返回当前SQL容器用于连写
     */
    public SqlBuilder tab() {
        return this.tab(1);
    }

    /**
     * 在现有的基础上增加或减少缩进
     * 
     * @param size 正数为增加, 负数为减少
     * @return 返回当前SQL容器用于连写
     */
    public SqlBuilder tab(int size) {
        if (size > 0) {
            this.buffer.append(IndentTools.getIndenTabs(size));
        } else if (size < 0) {
            // 负数为减少缩进
            // 先清除缩进, 再追加至减少后的数量
            // 因为有可能存在\s\s\t这种不规范的缩进, 无法从后往前直接清除
            int currSize = this.buffer.clearTrailingIndentWhitespace();
            if (currSize > 0) {
                this.buffer.append(IndentTools.getIndenTabs(currSize + size));
            }
        }
        return this;
    }

    /**
     * 设置缩进量<br>
     * indent()是设置缩进量<br>
     * tab()是在当前缩进量的基础上增加或减少缩进<br>
     * 
     * @param size 必须为正数, 负数和0会报错
     * @return 返回当前SQL容器用于连写
     */
    public SqlBuilder indent(int size) {
        this.buffer.clearTrailingIndentWhitespace();
        if (size > 0) {
            this.buffer.append(IndentTools.getIndenTabs(size));
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
    public SqlBuilder omit(boolean enabled) {
        if (enabled) {
            this.buffer.startOmit();
        } else {
            this.buffer.endOmit();
        }
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
    public SqlBuilder omit(int index, int count) {
        this.buffer.tryOmit(index, count);
        return this;
    }

    /**
     * 复制
     * 
     * @return 副本
     */
    public SqlBuilder copy() {
        SqlBuilder target = new SqlBuilder();
        target.buffer = this.buffer.copy();
        return target;
    }

    /**
     * 复制到另一个缓存容器中
     * 
     * @param target 目标容器
     */
    public void copyTo(SqlBuilder target) {
        target.buffer = this.buffer.copy();
    }

    @Override
    public String toString() {
        return this.buffer.toString();
    }
}
