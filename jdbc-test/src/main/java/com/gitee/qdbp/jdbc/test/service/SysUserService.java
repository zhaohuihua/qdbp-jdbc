package com.gitee.qdbp.jdbc.test.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.able.jdbc.condition.TableJoin;
import com.gitee.qdbp.able.jdbc.ordering.Orderings;
import com.gitee.qdbp.jdbc.api.QdbcBoot;
import com.gitee.qdbp.jdbc.sql.fragment.QueryFragmentHelper;
import com.gitee.qdbp.jdbc.test.model.SysRoleEntity;
import com.gitee.qdbp.jdbc.test.model.SysUserRoleEntity;
import com.gitee.qdbp.jdbc.test.model.UserRolesResult;
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
    public List<UserRolesResult> getUserRoles(List<String> userIds, DbWhere where, Orderings orderings) {
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
        return qdbcBoot.getSqlDao().listForObjects(sqlId, params, UserRolesResult.class);
    }
}
