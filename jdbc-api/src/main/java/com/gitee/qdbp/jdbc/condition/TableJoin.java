package com.gitee.qdbp.jdbc.condition;

import java.util.List;

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
public class TableJoin {

    private TableItem table;
    private List<JoinItem> joins;
    private JoinItem current;

    public TableJoin(Class<?> clazz, String alias) {
        this.table = new TableItem(clazz, alias);
    }

    public JoinStart innerJoin(Class<?> clazz, String alias) {
        return joinStart(clazz, alias, JoinType.InnerJoin);
    }

    public JoinStart leftJoin(Class<?> clazz, String alias) {
        return joinStart(clazz, alias, JoinType.LeftJoin);
    }

    public JoinStart rightJoin(Class<?> clazz, String alias) {
        return joinStart(clazz, alias, JoinType.RightJoin);
    }

    public JoinStart fullJoin(Class<?> clazz, String alias) {
        return joinStart(clazz, alias, JoinType.RightJoin);
    }

    protected JoinStart joinStart(Class<?> clazz, String alias, JoinType type) {
        DbWhere where = new DbWhere();
        this.current = new JoinItem(clazz, alias, JoinType.InnerJoin, where);
        this.joins.add(this.current);
        return new JoinStart(this);
    }

    public static enum JoinType {
        InnerJoin, LeftJoin, RightJoin, FullJoin
    }

    public static class TableItem {

        private Class<?> clazz;
        private String alias;

        public TableItem(Class<?> clazz, String alias) {
            this.clazz = clazz;
            this.alias = alias;
        }
    }

    public static class JoinItem extends TableItem {

        private JoinType type;
        private DbWhere where;

        public JoinItem(Class<?> clazz, String alias, JoinType type, DbWhere where) {
            super(clazz, alias);
            this.type = type;
            this.where = where;
        }
    }

    public static class JoinStart {

        private TableJoin join;

        public JoinStart(TableJoin join) {
            this.join = join;
        }

        public JoinOn on(String fieldName, String operate, Object... fieldValues) {
            this.join.current.where.on(fieldName, operate, fieldValues);
            return new JoinOn(join);
        }

    }

    public static class JoinOn {

        private TableJoin join;

        public JoinOn(TableJoin join) {
            this.join = join;
        }

        public JoinOn and(String fieldName, String operate, Object... fieldValues) {
            this.join.current.where.on(fieldName, operate, fieldValues);
            return this;
        }

        public JoinStart innerJoin(Class<?> clazz, String alias) {
            return this.join.joinStart(clazz, alias, JoinType.InnerJoin);
        }

        public JoinStart leftJoin(Class<?> clazz, String alias) {
            return this.join.joinStart(clazz, alias, JoinType.LeftJoin);
        }

        public JoinStart rightJoin(Class<?> clazz, String alias) {
            return this.join.joinStart(clazz, alias, JoinType.RightJoin);
        }

        public JoinStart fullJoin(Class<?> clazz, String alias) {
            return this.join.joinStart(clazz, alias, JoinType.RightJoin);
        }
    }
}
