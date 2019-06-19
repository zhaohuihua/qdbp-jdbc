package com.gitee.qdbp.able.jdbc.condition;

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
 * 增加一个参数resultField, 用于指定表数据保存至结果类的哪个字段(子对象)<br>
 * 生成的查询语句的查询字段, 对于重名字段加上表别名作为前缀, 生成列别名, 如U_ID, U_REMARK, UR_ID, UR_REMARK, R_ID, R_REMARK<br>
 * 查询结果根据列别名找到字段名和表别名; 再根据表别名找到resultField, 根据字段名填充数据<br>
 * 也可以指定resultField=this表示结果字段放在主对象中<br>
 * <pre>
 * // 最容易理解的代码写法:
 * TableJoin tables = new TableJoin(SysUser.class, "u", "user")
 *     .innerJoin(SysUserRole.class, "ur") // 未指定resultField, 不会作为查询字段, 也就不会保存查询结果
 *     .on("u.id", "=", "ur.userId")
 *     .and("ur.dataState", "=", 1)
 *     .innerJoin(SysRole.class, "r", "role")
 *     .on("ur.roleId", "=", "r.id")
 *     .and("r.dataState", "=", 1);
 * DbWhere where = new DbWhere();
 * where.on("u.userId", "in", userIds);
 * // UserRole = { SysUser user; SysRole role; }
 * EasyJoinQuery&lt;UserRole&gt; query = coreJdbcBoot.build(tables, UserRole.class);
 * List&lt;UserRole&gt; roles = query.list(where);
 * </pre>
 * <pre>
 * // 只取SysRole对象, 例如查询当前用户的所有角色
 * TableJoin tables = new TableJoin(SysUser.class, "u")
 *     .innerJoin(SysUserRole.class, "ur")
 *     .on("u.id", "=", "ur.userId")
 *     .and("ur.dataState", "=", 1)
 *     .innerJoin(SysRole.class, "r", "this") // this表示结果字段放在主对象中
 *     .on("ur.roleId", "=", "r.id")
 *     .and("r.dataState", "=", 1);
 * DbWhere where = new DbWhere();
 * where.on("u.userId", "=", userId);
 * EasyJoinQuery&lt;SysRole&gt; query = coreJdbcBoot.build(tables, SysRole.class);
 * List&lt;SysRole&gt; roles = query.list(where);
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

    /**
     * 构造函数
     * 
     * @param tableType 主表类型
     * @param tableAlias 主表别名
     */
    public TableJoin(Class<?> tableType, String tableAlias) {
        this.major = new TableItem(tableType, tableAlias);
    }

    /**
     * 构造函数
     * 
     * @param tableType 主表类型
     * @param tableAlias 主表别名
     * @param resultField 数据保存至结果类的哪个字段(子对象), this表示结果字段放在主对象中
     */
    public TableJoin(Class<?> tableType, String tableAlias, String resultField) {
        this.major = new TableItem(tableType, tableAlias, resultField);
    }

    /**
     * 增加 inner join 表连接, 仅用于表关联, 不取返回结果(如果需要返回结果请添加resultField字段)
     * 
     * @param tableType 表类型
     * @param tableAlias 表别名
     * @return JoinStart 用于添加on条件
     */
    public JoinStart innerJoin(Class<?> tableType, String tableAlias) {
        return joinStart(tableType, tableAlias, JoinType.InnerJoin);
    }

    /**
     * 增加 left join 表连接, 仅用于表关联, 不取返回结果(如果需要返回结果请添加resultField字段)
     * 
     * @param tableType 表类型
     * @param tableAlias 表别名
     * @return JoinStart 用于添加on条件
     */
    public JoinStart leftJoin(Class<?> tableType, String tableAlias) {
        return joinStart(tableType, tableAlias, JoinType.LeftJoin);
    }

    /**
     * 增加 right join 表连接, 仅用于表关联, 不取返回结果(如果需要返回结果请添加resultField字段)
     * 
     * @param tableType 表类型
     * @param tableAlias 表别名
     * @return JoinStart 用于添加on条件
     */
    public JoinStart rightJoin(Class<?> tableType, String tableAlias) {
        return joinStart(tableType, tableAlias, JoinType.RightJoin);
    }

    /**
     * 增加 full join 表连接, 仅用于表关联, 不取返回结果(如果需要返回结果请添加resultField字段)
     * 
     * @param tableType 表类型
     * @param tableAlias 表别名
     * @return JoinStart 用于添加on条件
     */
    public JoinStart fullJoin(Class<?> tableType, String tableAlias) {
        return joinStart(tableType, tableAlias, JoinType.FullJoin);
    }

    /**
     * 增加 inner join 表连接
     * 
     * @param tableType 表类型
     * @param tableAlias 表别名
     * @param resultField 数据保存至结果类的哪个字段(子对象), this表示结果字段放在主对象中
     * @return JoinStart 用于添加on条件
     */
    public JoinStart innerJoin(Class<?> tableType, String tableAlias, String resultField) {
        return joinStart(tableType, tableAlias, resultField, JoinType.InnerJoin);
    }

    /**
     * 增加 left join 表连接
     * 
     * @param tableType 表类型
     * @param tableAlias 表别名
     * @param resultField 数据保存至结果类的哪个字段(子对象), this表示结果字段放在主对象中
     * @return JoinStart 用于添加on条件
     */
    public JoinStart leftJoin(Class<?> tableType, String tableAlias, String resultField) {
        return joinStart(tableType, tableAlias, resultField, JoinType.LeftJoin);
    }

    /**
     * 增加 right join 表连接
     * 
     * @param tableType 表类型
     * @param tableAlias 表别名
     * @param resultField 数据保存至结果类的哪个字段(子对象), this表示结果字段放在主对象中
     * @return JoinStart 用于添加on条件
     */
    public JoinStart rightJoin(Class<?> tableType, String tableAlias, String resultField) {
        return joinStart(tableType, tableAlias, resultField, JoinType.RightJoin);
    }

    /**
     * 增加 full join 表连接
     * 
     * @param tableType 表类型
     * @param tableAlias 表别名
     * @param resultField 数据保存至结果类的哪个字段(子对象), this表示结果字段放在主对象中
     * @return JoinStart 用于添加on条件
     */
    public JoinStart fullJoin(Class<?> tableType, String tableAlias, String resultField) {
        return joinStart(tableType, tableAlias, resultField, JoinType.FullJoin);
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

    protected JoinStart joinStart(Class<?> tableType, String tableAlias, JoinType type) {
        return this.joinStart(tableType, tableAlias, null, type);
    }

    protected JoinStart joinStart(Class<?> tableType, String tableAlias, String resultField, JoinType type) {
        DbWhere where = new DbWhere();
        this.current = new JoinItem(tableType, tableAlias, resultField, JoinType.InnerJoin, where);
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
        private Class<?> tableType;
        /** 别名 **/
        private String tabletableAlias;
        /** 数据保存至结果类的哪个字段(子对象), this表示结果字段放在主对象中 **/
        private String resultField;

        protected TableItem(Class<?> tableType, String tabletableAlias) {
            this.tableType = tableType;
            this.tabletableAlias = tabletableAlias;
        }

        protected TableItem(Class<?> tableType, String tabletableAlias, String resultField) {
            this.tableType = tableType;
            this.tabletableAlias = tabletableAlias;
            this.resultField = resultField;
        }

        /** 表类型 **/
        public Class<?> getTableType() {
            return tableType;
        }

        /** 表类型 **/
        public void setTableType(Class<?> tableType) {
            this.tableType = tableType;
        }

        /** 别名 **/
        public String getTableAlias() {
            return tabletableAlias;
        }

        /** 别名 **/
        public void setTableAlias(String tableAlias) {
            this.tabletableAlias = tableAlias;
        }

        /** 数据保存至结果类的哪个字段(子对象), this表示结果字段放在主对象中 **/
        public String getResultField() {
            return resultField;
        }

        /** 数据保存至结果类的哪个字段(子对象), this表示结果字段放在主对象中 **/
        public void setResultField(String resultField) {
            this.resultField = resultField;
        }

    }

    public static class JoinItem extends TableItem {

        /** serialVersionUID **/
        private static final long serialVersionUID = 1L;
        /** 连接类型 **/
        private JoinType joinType;
        /** 连接条件 **/
        private DbWhere where;

        protected JoinItem(Class<?> tableType, String tableAlias, JoinType joinType, DbWhere where) {
            super(tableType, tableAlias);
            this.joinType = joinType;
            this.where = where;
        }

        protected JoinItem(Class<?> tableType, String tableAlias, String resultField, JoinType joinType,
                DbWhere where) {
            super(tableType, tableAlias, resultField);
            this.joinType = joinType;
            this.where = where;
        }

        /** 连接类型 **/
        public JoinType getJoinType() {
            return joinType;
        }

        /** 连接类型 **/
        public void setJoinType(JoinType type) {
            this.joinType = type;
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
        public JoinStart innerJoin(Class<?> tableType, String tableAlias) {
            return this.join.joinStart(tableType, tableAlias, JoinType.InnerJoin);
        }

        /** 增加LeftJoin表连接 **/
        public JoinStart leftJoin(Class<?> tableType, String tableAlias) {
            return this.join.joinStart(tableType, tableAlias, JoinType.LeftJoin);
        }

        /** 增加RightJoin表连接 **/
        public JoinStart rightJoin(Class<?> tableType, String tableAlias) {
            return this.join.joinStart(tableType, tableAlias, JoinType.RightJoin);
        }

        /** 增加FullJoin表连接 **/
        public JoinStart fullJoin(Class<?> tableType, String tableAlias) {
            return this.join.joinStart(tableType, tableAlias, JoinType.FullJoin);
        }

        /** 增加InnerJoin表连接 **/
        public JoinStart innerJoin(Class<?> tableType, String tableAlias, String resultField) {
            return this.join.joinStart(tableType, tableAlias, resultField, JoinType.InnerJoin);
        }

        /** 增加LeftJoin表连接 **/
        public JoinStart leftJoin(Class<?> tableType, String tableAlias, String resultField) {
            return this.join.joinStart(tableType, tableAlias, resultField, JoinType.LeftJoin);
        }

        /** 增加RightJoin表连接 **/
        public JoinStart rightJoin(Class<?> tableType, String tableAlias, String resultField) {
            return this.join.joinStart(tableType, tableAlias, resultField, JoinType.RightJoin);
        }

        /** 增加FullJoin表连接 **/
        public JoinStart fullJoin(Class<?> tableType, String tableAlias, String resultField) {
            return this.join.joinStart(tableType, tableAlias, resultField, JoinType.FullJoin);
        }
    }

}
