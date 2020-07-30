package com.gitee.qdbp.jdbc.plugins.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.gitee.qdbp.able.matches.EqualsStringMatcher;
import com.gitee.qdbp.able.matches.StringMatcher;
import com.gitee.qdbp.jdbc.model.SimpleFieldColumn;
import com.gitee.qdbp.jdbc.plugins.CommonFieldResolver;
import com.gitee.qdbp.jdbc.utils.InnerTools;

/**
 * 公共字段判断类(为了将公共字段排在最后)<br>
 * 可以配置父类的包名, 如 实体类都继承com.xxx.core.beans.IdEntity, 那么可以设置com.xxx.core.beans为公共包名<br>
 * 也可以配置公共字段名, 如 createUser, createTime, dataState等<br>
 *
 * @author zhaohuihua
 * @version 190601
 */
public class SimpleCommonFieldResolver implements CommonFieldResolver {

    /** 公共包名匹配方法集合 **/
    private List<StringMatcher> commonPackageMatchers;
    /** 公共字段名匹配方法集合 **/
    private List<StringMatcher> commonFieldNameMatchers;

    /**
     * 设置公共包名匹配模式<br>
     * regexp:开头的解析为RegexpStringMatcher<br>
     * ant:开头的解析为AntStringMatcher<br>
     * 其余的解析为EqualsStringMatcher<br>
     * 
     * @param text 文本
     */
    public void setCommonPackagePatterns(String text) {
        setCommonPackageMatchers(InnerTools.parseStringMatcher(text));
    }

    /**
     * 设置公共字段名匹配模式<br>
     * regexp:开头的解析为RegexpStringMatcher<br>
     * ant:开头的解析为AntStringMatcher<br>
     * contains:开头的解析为ContainsStringMatcher<br>
     * 其余的也解析为ContainsStringMatcher<br>
     * 
     * @param text 文本
     */
    public void setCommonFieldNamePatterns(String text) {
        setCommonFieldNameMatchers(InnerTools.parseStringMatcher(text));
    }

    public void setCommonPackageMatchers(List<StringMatcher> matchers) {
        this.commonPackageMatchers = matchers;
    }

    public void addCommonPackageMatchers(StringMatcher... matchers) {
        if (matchers == null || matchers.length == 0) {
            return;
        }
        if (commonPackageMatchers == null) {
            commonPackageMatchers = new ArrayList<>();
        }
        commonPackageMatchers.addAll(Arrays.asList(matchers));
    }

    public void addCommonPackageMatchers(String... matchers) {
        if (matchers == null || matchers.length == 0) {
            return;
        }
        if (commonPackageMatchers == null) {
            commonPackageMatchers = new ArrayList<>();
        }
        for (String string : matchers) {
            commonPackageMatchers.add(new EqualsStringMatcher(string));
        }
    }

    public void setCommonFieldNameMatchers(List<StringMatcher> matchers) {
        this.commonFieldNameMatchers = matchers;
    }

    public void addCommonFieldNameMatchers(StringMatcher... matchers) {
        if (matchers == null || matchers.length == 0) {
            return;
        }
        if (commonFieldNameMatchers == null) {
            commonFieldNameMatchers = new ArrayList<>();
        }
        commonFieldNameMatchers.addAll(Arrays.asList(matchers));
    }

    public void addCommonFieldNameMatchers(String... matchers) {
        if (matchers == null || matchers.length == 0) {
            return;
        }
        if (commonFieldNameMatchers == null) {
            commonFieldNameMatchers = new ArrayList<>();
        }
        for (String string : matchers) {
            commonFieldNameMatchers.add(new EqualsStringMatcher(string));
        }
    }

    @Override
    public boolean isCommonPackage(String pkg) {
        if (commonPackageMatchers == null || commonPackageMatchers.isEmpty()) {
            return false;
        } else {
            for (StringMatcher matcher : commonPackageMatchers) {
                if (matcher.matches(pkg)) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public boolean isCommonFieldName(String fieldName) {
        if (commonFieldNameMatchers == null || commonFieldNameMatchers.isEmpty()) {
            return false;
        } else {
            for (StringMatcher matcher : commonFieldNameMatchers) {
                if (matcher.matches(fieldName)) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public List<SimpleFieldColumn> sortCommonFields(List<SimpleFieldColumn> fields) {
        if (commonFieldNameMatchers == null || commonFieldNameMatchers.isEmpty()) {
            return fields;
        }
        // 通过字段名指定的公共字段
        List<SimpleFieldColumn> commonFieldColumns = new ArrayList<>();
        Map<String, Void> commonFieldNames = new HashMap<>();
        // 按照配置的公共字段顺序排序, 而不管字段在类中的声明顺序
        for (StringMatcher matcher : commonFieldNameMatchers) {
            for (SimpleFieldColumn field : fields) {
                if (matcher.matches(field.getFieldName())) {
                    commonFieldColumns.add(field);
                    commonFieldNames.put(field.getFieldName(), null);
                }
            }
        }
        // 先放公共包下的字段, 再放通过字段名指定的公共字段
        List<SimpleFieldColumn> result = new ArrayList<>();
        // 先放公共包下的字段(即先放不是通过字段指定的)
        if (commonFieldColumns.size() < fields.size()) {
            for (SimpleFieldColumn field : fields) {
                if (!commonFieldNames.containsKey(field.getColumnName())) {
                    result.add(field); // 不是通过字段指定的
                }
            }
        }
        // 再放通过字段名指定的公共字段
        if (!commonFieldColumns.isEmpty()) {
            result.addAll(commonFieldColumns);
        }
        return result;
    }

}
