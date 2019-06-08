package com.gitee.qdbp.jdbc.condition;

import java.io.Serializable;
import java.util.List;
import com.gitee.qdbp.tools.utils.NamingTools;

/**
 * 表关联<br>
 * 关于查询结果的思考:<br>
 * 对于SYS_USER,SYS_USER_ROLE,SYS_ROLE这样的关联查询<br>
 * 查询结果, 以前是新建一个类,继承SysUser再复制SysRole的所有字段<br>
 * 存在3个问题: 1是复制代码太多; 2是修改SYS_ROLE时需要修改SysRole和新建的这个类; 3是对于重名字段如createTime,remark, 不好处理<br>
 * 最理想的方式是什么呢?<br>
 * 我觉得应该是新建一个结果类, 有SysUser user, SysUserRole userRole, SysRole role三个字段(子对象), 分别保存来自三个表的查询结果!<br>
 * 如果查询结果不需要关注SYS_USER_ROLE这个关联表, 也可以建SysUser user, SysRole role两个字段(子对象)的类来保存查询结果<br>
 * 实现思路:<br>
 * 增加一个参数owner, 用于指定表数据保存至结果类的哪个子对象<br>
 * 生成的查询语句的查询字段, 对于重名字段加上表别名作为前缀, 生成列别名, 如U_ID, U_REMARK, UR_ID, UR_REMARK, R_ID, R_REMARK<br>
 * 查询结果根据列别名找到字段名和表别名; 再根据表别名找到owner, 根据字段名填充数据<br>
 * <pre>
 * // 最容易理解的代码写法:
 * new TableJoin(SysUser.class, "u", "user")
 *     .innerJoin(SysUserRole.class, "ur") // 未指定owner, 不会作为查询字段, 也就不会保存查询结果
 *     .on("u.id", "=", "ur.userId")
 *     .and("ur.dataState", "=", 1)
 *     .innerJoin(SysRole.class, "r", "role")
 *     .on("ur.roleId", "=", "r.id")
 *     .and("r.dataState", "=", 1);
 * </pre>
 *
 * @author zhaohuihua
 * @version 190604
 */
public class TableJoin implements Serializable {

    /** serialVersionUID **/
    private static final long serialVersionUID = 1L;
    /** 主表 **/
    private TableItem major;
    /** 关联表 **/
    private List<JoinItem> joins;
    /** 当前关联表 **/
    private JoinItem current;

    public TableJoin(Class<?> clazz, String alias) {
        this.major = new TableItem(clazz, alias);
    }

    /** 增加InnerJoin表连接 **/
    public JoinStart innerJoin(Class<?> clazz, String alias) {
        return joinStart(clazz, alias, JoinType.InnerJoin);
    }

    /** 增加LeftJoin表连接 **/
    public JoinStart leftJoin(Class<?> clazz, String alias) {
        return joinStart(clazz, alias, JoinType.LeftJoin);
    }

    /** 增加RightJoin表连接 **/
    public JoinStart rightJoin(Class<?> clazz, String alias) {
        return joinStart(clazz, alias, JoinType.RightJoin);
    }

    /** 增加FullJoin表连接 **/
    public JoinStart fullJoin(Class<?> clazz, String alias) {
        return joinStart(clazz, alias, JoinType.FullJoin);
    }

    /** 增加InnerJoin表连接 **/
    public JoinStart innerJoin(Class<?> clazz, String alias, String owner) {
        return joinStart(clazz, alias, owner, JoinType.InnerJoin);
    }

    /** 增加LeftJoin表连接 **/
    public JoinStart leftJoin(Class<?> clazz, String alias, String owner) {
        return joinStart(clazz, alias, owner, JoinType.LeftJoin);
    }

    /** 增加RightJoin表连接 **/
    public JoinStart rightJoin(Class<?> clazz, String alias, String owner) {
        return joinStart(clazz, alias, owner, JoinType.RightJoin);
    }

    /** 增加FullJoin表连接 **/
    public JoinStart fullJoin(Class<?> clazz, String alias, String owner) {
        return joinStart(clazz, alias, owner, JoinType.FullJoin);
    }

    /** 主表 **/
    public TableItem getMajor() {
        return major;
    }

    /** 主表 **/
    public void setMajor(TableItem major) {
        this.major = major;
    }

    /** 关联表 **/
    public List<JoinItem> getJoins() {
        return joins;
    }

    /** 关联表 **/
    public void setJoins(List<JoinItem> joins) {
        this.joins = joins;
    }

    protected JoinStart joinStart(Class<?> clazz, String alias, JoinType type) {
        return this.joinStart(clazz, alias, null, type);
    }

    protected JoinStart joinStart(Class<?> clazz, String alias, String owner, JoinType type) {
        DbWhere where = new DbWhere();
        this.current = new JoinItem(clazz, alias, owner, JoinType.InnerJoin, where);
        this.joins.add(this.current);
        return new JoinStart(this);
    }

    public static enum JoinType {
        InnerJoin, LeftJoin, RightJoin, FullJoin;

        public String toSqlString() {
            return NamingTools.toSpaceSplitString(this.name()).toUpperCase();
        }
    }

    public static class TableItem implements Serializable {

        /** serialVersionUID **/
        private static final long serialVersionUID = 1L;
        /** 表类型 **/
        private Class<?> table;
        /** 别名 **/
        private String alias;
        /** 数据保存至结果类的哪个子对象 **/
        private String owner;

        protected TableItem(Class<?> table, String alias) {
            this.table = table;
            this.alias = alias;
        }

        protected TableItem(Class<?> table, String alias, String owner) {
            this.table = table;
            this.alias = alias;
            this.owner = owner;
        }

        /** 表类型 **/
        public Class<?> getTable() {
            return table;
        }

        /** 表类型 **/
        public void setTable(Class<?> table) {
            this.table = table;
        }

        /** 别名 **/
        public String getAlias() {
            return alias;
        }

        /** 别名 **/
        public void setAlias(String alias) {
            this.alias = alias;
        }

        /** 数据保存至结果类的哪个子对象 **/
        public String getowner() {
            return owner;
        }

        /** 数据保存至结果类的哪个子对象 **/
        public void setowner(String owner) {
            this.owner = owner;
        }

    }

    public static class JoinItem extends TableItem {

        /** serialVersionUID **/
        private static final long serialVersionUID = 1L;
        /** 连接类型 **/
        private JoinType type;
        /** 连接条件 **/
        private DbWhere where;

        protected JoinItem(Class<?> clazz, String alias, JoinType type, DbWhere where) {
            super(clazz, alias);
            this.type = type;
            this.where = where;
        }

        protected JoinItem(Class<?> clazz, String alias, String owner, JoinType type, DbWhere where) {
            super(clazz, alias, owner);
            this.type = type;
            this.where = where;
        }

        /** 连接类型 **/
        public JoinType getType() {
            return type;
        }

        /** 连接类型 **/
        public void setType(JoinType type) {
            this.type = type;
        }

        /** 连接条件 **/
        public DbWhere getWhere() {
            return where;
        }

        /** 连接条件 **/
        public void setWhere(DbWhere where) {
            this.where = where;
        }

    }

    public static class JoinStart implements Serializable {

        /** serialVersionUID **/
        private static final long serialVersionUID = 1L;
        private TableJoin join;

        protected JoinStart(TableJoin join) {
            this.join = join;
        }

        /** 增加连接条件 **/
        public JoinOn on(String fieldName, String operate, Object... fieldValues) {
            this.join.current.where.on(fieldName, operate, fieldValues);
            return new JoinOn(join);
        }

    }

    public static class JoinOn implements Serializable {

        /** serialVersionUID **/
        private static final long serialVersionUID = 1L;
        private TableJoin join;

        public JoinOn(TableJoin join) {
            this.join = join;
        }

        /** 增加连接条件 **/
        public JoinOn and(String fieldName, String operate, Object... fieldValues) {
            this.join.current.where.on(fieldName, operate, fieldValues);
            return this;
        }

        /** 增加InnerJoin表连接 **/
        public JoinStart innerJoin(Class<?> clazz, String alias) {
            return this.join.joinStart(clazz, alias, JoinType.InnerJoin);
        }

        /** 增加LeftJoin表连接 **/
        public JoinStart leftJoin(Class<?> clazz, String alias) {
            return this.join.joinStart(clazz, alias, JoinType.LeftJoin);
        }

        /** 增加RightJoin表连接 **/
        public JoinStart rightJoin(Class<?> clazz, String alias) {
            return this.join.joinStart(clazz, alias, JoinType.RightJoin);
        }

        /** 增加FullJoin表连接 **/
        public JoinStart fullJoin(Class<?> clazz, String alias) {
            return this.join.joinStart(clazz, alias, JoinType.FullJoin);
        }

        /** 增加InnerJoin表连接 **/
        public JoinStart innerJoin(Class<?> clazz, String alias, String owner) {
            return this.join.joinStart(clazz, alias, owner, JoinType.InnerJoin);
        }

        /** 增加LeftJoin表连接 **/
        public JoinStart leftJoin(Class<?> clazz, String alias, String owner) {
            return this.join.joinStart(clazz, alias, owner, JoinType.LeftJoin);
        }

        /** 增加RightJoin表连接 **/
        public JoinStart rightJoin(Class<?> clazz, String alias, String owner) {
            return this.join.joinStart(clazz, alias, owner, JoinType.RightJoin);
        }

        /** 增加FullJoin表连接 **/
        public JoinStart fullJoin(Class<?> clazz, String alias, String owner) {
            return this.join.joinStart(clazz, alias, owner, JoinType.FullJoin);
        }
    }

}
