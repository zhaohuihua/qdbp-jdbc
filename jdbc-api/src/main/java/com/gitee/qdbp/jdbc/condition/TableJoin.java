package com.gitee.qdbp.jdbc.condition;

import java.io.Serializable;
import java.util.List;
import com.gitee.qdbp.tools.utils.NamingTools;

/**
 * 表关联<br>
 * <pre>
 * new TableJoin(SysUser.class, "SU")
 *     .innerJoin(SysUserRole.class, "SUR")
 *     .on("SU.id", "=", "SUR.userId")
 *     .and("SUR.dataState", "=", 1)
 *     .innerJoin(SysRole.class, "SR")
 *     .on("SUR.roleId", "=", "SR.id")
 *     .and("SR.dataState", "=", 1);
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
        DbWhere where = new DbWhere();
        this.current = new JoinItem(clazz, alias, JoinType.InnerJoin, where);
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

        protected TableItem(Class<?> table, String alias) {
            this.table = table;
            this.alias = alias;
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
    }

}
