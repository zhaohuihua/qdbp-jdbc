package com.gitee.qdbp.jdbc.plugins.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.gitee.qdbp.able.matches.EqualsStringMatcher;
import com.gitee.qdbp.able.matches.StringMatcher;

/**
 * 为了将公共字段排在最后<br>
 * 可以配置父类的包名, 如 实体类都继承com.xxx.core.beans.IdEntity, 那么可以设置com.xxx.core.beans为公共包名<br>
 * 也可以配置公共字段名, 如 createUser, createTime, dataState等<br>
 *
 * @author zhaohuihua
 * @version 190601
 */
public class CommonFieldResolver {

    private List<StringMatcher> commonPackageMatchers;
    private List<StringMatcher> commonFieldNameMatchers;

    public void setCommonPackagePatterns(List<StringMatcher> matchers) {
        this.commonPackageMatchers = matchers;
    }

    public void addCommonPackagePatterns(StringMatcher... matchers) {
        if (matchers == null || matchers.length == 0) {
            return;
        }
        if (commonPackageMatchers == null) {
            commonPackageMatchers = new ArrayList<>();
        }
        commonPackageMatchers.addAll(Arrays.asList(matchers));
    }

    public void addCommonPackagePatterns(String... matchers) {
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

    public void setCommonFieldNamePatterns(List<StringMatcher> matchers) {
        this.commonFieldNameMatchers = matchers;
    }

    public void addCommonFieldNamePatterns(StringMatcher... matchers) {
        if (matchers == null || matchers.length == 0) {
            return;
        }
        if (commonFieldNameMatchers == null) {
            commonFieldNameMatchers = new ArrayList<>();
        }
        commonFieldNameMatchers.addAll(Arrays.asList(matchers));
    }

    public void addCommonFieldNamePatterns(String... matchers) {
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

}
