package com.gitee.qdbp.jdbc.plugins;

import java.util.List;
import com.gitee.qdbp.jdbc.model.SimpleFieldColumn;

/**
 * 为了将公共字段排在最后, 判断字段是不是公共字段或是否来自于公共包下的对象
 *
 * @author zhaohuihua
 * @version 190601
 */
public interface CommonFieldResolver {

    /** 判断字段是否来自于公共包下的对象 **/
    boolean isCommonPackage(String pkg);

    /** 判断字段是不是公共字段 **/
    boolean isCommonFieldName(String fieldName);

    /** 公共字段排序 **/
    List<SimpleFieldColumn> sortCommonFields(List<SimpleFieldColumn> fields);
}
