package com.gitee.qdbp.jdbc.test.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.able.jdbc.condition.TableJoin;
import com.gitee.qdbp.able.jdbc.ordering.OrderPaging;
import com.gitee.qdbp.able.jdbc.ordering.Orderings;
import com.gitee.qdbp.able.jdbc.paging.PageList;
import com.gitee.qdbp.jdbc.api.QdbcBoot;
import com.gitee.qdbp.jdbc.sql.fragment.QueryFragmentHelper;
import com.gitee.qdbp.jdbc.test.model.RoleIdUserResult;
import com.gitee.qdbp.jdbc.test.model.SysRoleEntity;
import com.gitee.qdbp.jdbc.test.model.SysUserEntity;
import com.gitee.qdbp.jdbc.test.model.SysUserRoleEntity;
import com.gitee.qdbp.jdbc.test.model.UserIdRoleResult;
import com.gitee.qdbp.tools.utils.VerifyTools;

@Service
public class SysUserService {

    @Autowired
    private QdbcBoot qdbcBoot;

    /** 查询指定用户所分配的角色信息 **/
    public List<SysRoleEntity> getUserRoles(String userId, DbWhere where, Orderings orderings) {
        VerifyTools.requireNonNull(userId, "userId");

        TableJoin tables = TableJoin.of(SysUserRoleEntity.class, "ur", SysRoleEntity.class, "r");
        QueryFragmentHelper sqlHelper = qdbcBoot.buildSqlBuilder(tables).helper();

        Map<String, Object> params = new HashMap<>();
        params.put("userIds", Arrays.asList(userId));
        if (VerifyTools.isNotBlank(where)) {
            params.put("whereCondition", sqlHelper.buildWhereSql(where, false));
        }
        if (VerifyTools.isNotBlank(orderings)) {
            params.put("orderByCondition", sqlHelper.buildOrderBySql(orderings, false));
        }

        String sqlId = "user.roles.query";
        return qdbcBoot.getSqlDao().listForObjects(sqlId, params, SysRoleEntity.class);
    }

    /** 批量查询用户所分配的角色信息 **/
    public List<UserIdRoleResult> getUserRoles(List<String> userIds, DbWhere where, Orderings orderings) {
        VerifyTools.requireNonNull(userIds, "userIds");

        TableJoin tables = TableJoin.of(SysUserRoleEntity.class, "ur", SysRoleEntity.class, "r");
        QueryFragmentHelper sqlHelper = qdbcBoot.buildSqlBuilder(tables).helper();

        Map<String, Object> params = new HashMap<>();
        params.put("userIds", userIds);
        if (VerifyTools.isNotBlank(where)) {
            params.put("whereCondition", sqlHelper.buildWhereSql(where, false));
        }
        if (VerifyTools.isNotBlank(orderings)) {
            params.put("orderByCondition", sqlHelper.buildOrderBySql(orderings, false));
        }

        String sqlId = "user.roles.query";
        return qdbcBoot.getSqlDao().listForObjects(sqlId, params, UserIdRoleResult.class);
    }

    /** 查询指定用户所分配的角色信息 **/
    public PageList<SysRoleEntity> getUserRoles(String userId, DbWhere where, OrderPaging odpg) {
        VerifyTools.requireNonNull(userId, "userId");

        TableJoin tables = TableJoin.of(SysUserRoleEntity.class, "ur", SysRoleEntity.class, "r");
        QueryFragmentHelper sqlHelper = qdbcBoot.buildSqlBuilder(tables).helper();

        Map<String, Object> params = new HashMap<>();
        params.put("userIds", Arrays.asList(userId));
        if (VerifyTools.isNotBlank(where)) {
            params.put("whereCondition", sqlHelper.buildWhereSql(where, false));
        }
        if (VerifyTools.isNotBlank(odpg.getOrderings())) {
            params.put("orderByCondition", sqlHelper.buildOrderBySql(odpg.getOrderings(), false));
        }

        String sqlId = "user.roles.query";
        return qdbcBoot.getSqlDao().pageForObjects(sqlId, params, odpg, SysRoleEntity.class);
    }

    /** 批量查询用户所分配的角色信息 **/
    public PageList<UserIdRoleResult> getUserRoles(List<String> userIds, DbWhere where, OrderPaging odpg) {
        VerifyTools.requireNonNull(userIds, "userIds");

        TableJoin tables = TableJoin.of(SysUserRoleEntity.class, "ur", SysRoleEntity.class, "r");
        QueryFragmentHelper sqlHelper = qdbcBoot.buildSqlBuilder(tables).helper();

        Map<String, Object> params = new HashMap<>();
        params.put("userIds", userIds);
        if (VerifyTools.isNotBlank(where)) {
            params.put("whereCondition", sqlHelper.buildWhereSql(where, false));
        }
        if (VerifyTools.isNotBlank(odpg.getOrderings())) {
            params.put("orderByCondition", sqlHelper.buildOrderBySql(odpg.getOrderings(), false));
        }

        String sqlId = "user.roles.query";
        return qdbcBoot.getSqlDao().pageForObjects(sqlId, params, odpg, UserIdRoleResult.class);
    }

    /** 查询指定角色下的用户信息 **/
    public List<SysUserEntity> getRoleUsers(String roleId, DbWhere where, Orderings orderings) {
        VerifyTools.requireNonNull(roleId, "roleId");

        TableJoin tables = TableJoin.of(SysUserRoleEntity.class, "ur", SysUserEntity.class, "u");
        QueryFragmentHelper sqlHelper = qdbcBoot.buildSqlBuilder(tables).helper();

        Map<String, Object> params = new HashMap<>();
        params.put("roleIds", Arrays.asList(roleId));
        if (VerifyTools.isNotBlank(where)) {
            params.put("whereCondition", sqlHelper.buildWhereSql(where, false));
        }
        if (VerifyTools.isNotBlank(orderings)) {
            params.put("orderByCondition", sqlHelper.buildOrderBySql(orderings, false));
        }

        String sqlId = "role.users.query";
        return qdbcBoot.getSqlDao().listForObjects(sqlId, params, SysUserEntity.class);
    }

    /** 批量查询指定角色下的用户信息 **/
    public List<RoleIdUserResult> getRoleUsers(List<String> roleIds, DbWhere where, Orderings orderings) {
        VerifyTools.requireNonNull(roleIds, "roleIds");

        TableJoin tables = TableJoin.of(SysUserRoleEntity.class, "ur", SysUserEntity.class, "u");
        QueryFragmentHelper sqlHelper = qdbcBoot.buildSqlBuilder(tables).helper();

        Map<String, Object> params = new HashMap<>();
        params.put("roleIds", roleIds);
        if (VerifyTools.isNotBlank(where)) {
            params.put("whereCondition", sqlHelper.buildWhereSql(where, false));
        }
        if (VerifyTools.isNotBlank(orderings)) {
            params.put("orderByCondition", sqlHelper.buildOrderBySql(orderings, false));
        }

        String sqlId = "role.users.query";
        return qdbcBoot.getSqlDao().listForObjects(sqlId, params, RoleIdUserResult.class);
    }

    /** 查询指定角色下的用户信息 **/
    public PageList<SysUserEntity> getRoleUsers(String roleId, DbWhere where, OrderPaging odpg) {
        VerifyTools.requireNonNull(roleId, "roleId");

        TableJoin tables = TableJoin.of(SysUserRoleEntity.class, "ur", SysUserEntity.class, "u");
        QueryFragmentHelper sqlHelper = qdbcBoot.buildSqlBuilder(tables).helper();

        Map<String, Object> params = new HashMap<>();
        params.put("roleIds", Arrays.asList(roleId));
        if (VerifyTools.isNotBlank(where)) {
            params.put("whereCondition", sqlHelper.buildWhereSql(where, false));
        }
        if (VerifyTools.isNotBlank(odpg.getOrderings())) {
            params.put("orderByCondition", sqlHelper.buildOrderBySql(odpg.getOrderings(), false));
        }

        String sqlId = "role.users.query";
        return qdbcBoot.getSqlDao().pageForObjects(sqlId, params, odpg, SysUserEntity.class);
    }

    /** 批量查询指定角色下的用户信息 **/
    public PageList<RoleIdUserResult> getRoleUsers(List<String> roleIds, DbWhere where, OrderPaging odpg) {
        VerifyTools.requireNonNull(roleIds, "roleIds");

        TableJoin tables = TableJoin.of(SysUserRoleEntity.class, "ur", SysUserEntity.class, "u");
        QueryFragmentHelper sqlHelper = qdbcBoot.buildSqlBuilder(tables).helper();

        Map<String, Object> params = new HashMap<>();
        params.put("roleIds", roleIds);
        if (VerifyTools.isNotBlank(where)) {
            params.put("whereCondition", sqlHelper.buildWhereSql(where, false));
        }
        if (VerifyTools.isNotBlank(odpg.getOrderings())) {
            params.put("orderByCondition", sqlHelper.buildOrderBySql(odpg.getOrderings(), false));
        }

        String sqlId = "role.users.query";
        return qdbcBoot.getSqlDao().pageForObjects(sqlId, params, odpg, RoleIdUserResult.class);
    }
}
