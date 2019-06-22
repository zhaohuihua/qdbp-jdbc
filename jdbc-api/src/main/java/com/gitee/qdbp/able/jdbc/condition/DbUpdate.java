package com.gitee.qdbp.able.jdbc.condition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.gitee.qdbp.able.jdbc.base.DbCondition;

/**
 * 数据库更新操作容器<br>
 * 支持Set,Add,ToNull操作<br>
 * <pre>
    DbUpdate ud = new DbUpdate();
    // SQL> SET USER_NAME = :$1$UserName
    ud.set("userName", "zhaohuihua"); // 用户名修改为指定值
    // SQL> SET MEMBER_SCORE = MEMBER_SCORE + :$1$MemberScore
    ud.add("memberScore", +100); // 会员积分增加100
    // SQL> SET MEMBER_SCORE = MEMBER_SCORE - :$1$MemberScore
    ud.add("memberScore", -100); // 会员积分减少100
    // SQL> SET USER_STATE = NULL
    ud.toNull("userState"); // 用户状态修改为空
 * </pre>
 *
 * @author zhaohuihua
 * @version 181221
 */
public class DbUpdate extends DbItems {

    /** SerialVersionUID **/
    private static final long serialVersionUID = 1L;

    /** SET :fieldName = :fieldValue **/
    public DbUpdate set(String fieldName, Object fieldValue) {
        this.put(fieldName, fieldValue);
        return this;
    }

    /** SET :fieldName = :fieldName + :fieldValue **/
    public DbUpdate add(String fieldName, Object fieldValue) {
        this.put("Add", fieldName, fieldValue);
        return this;
    }

    /** SET :fieldName = NULL **/
    public DbUpdate toNull(String fieldName) {
        this.put("ToNull", fieldName, null);
        return this;
    }

    protected void put(DbFields fields) {
        throw new UnsupportedOperationException("DbUpdate can't supported put(DbFields)");
    }

    /**
     * 查询指定字段所有的条件
     * 
     * @param fieldName 指定字段
     * @return 条件列表
     */
    public List<DbField> fields(String fieldName) {
        List<DbCondition> items = this.items();
        List<DbField> result = new ArrayList<>();
        if (items.isEmpty()) {
            return result;
        }

        Iterator<DbCondition> itr = items.iterator();
        while (itr.hasNext()) {
            DbCondition item = itr.next();
            if (item instanceof DbField) {
                if (((DbField) item).getFieldName().equals(fieldName)) {
                    result.add((DbField) item);
                }
            }
        }
        return result;
    }

    /**
     * 从map中获取参数构建对象
     * 
     * @param map Map参数
     * @return 对象实例
     */
    public static DbUpdate from(Map<String, Object> map) {
        return from(map, DbUpdate.class);
    }
}
