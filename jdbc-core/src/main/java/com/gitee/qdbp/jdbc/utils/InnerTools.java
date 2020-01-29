package com.gitee.qdbp.jdbc.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.util.StringUtils;
import com.gitee.qdbp.able.matches.AntStringMatcher;
import com.gitee.qdbp.able.matches.EqualsStringMatcher;
import com.gitee.qdbp.able.matches.RegexpStringMatcher;
import com.gitee.qdbp.able.matches.StringMatcher;
import com.gitee.qdbp.tools.utils.StringTools;

public class InnerTools {

    /** 列表型配置值的分隔符 **/
    private static final String LIST_VALUE_DELIMITERS = ",; \t\n";

    /**
     * 将文本拆分为数组(分隔符为[,; \t\n])
     * 
     * @param text 文本
     * @return 数组
     */
    public static String[] tokenizeToStringArray(String text) {
        if (text == null) {
            return new String[0];
        } else {
            return StringUtils.tokenizeToStringArray(text, LIST_VALUE_DELIMITERS);
        }
    }

    /**
     * 将文本拆分为列表(分隔符为[,; \t\n])
     * 
     * @param text 文本
     * @return 列表
     */
    public static List<String> tokenizeToStringList(String text) {
        String[] array = tokenizeToStringArray(text);
        return Arrays.asList(array);
    }

    /**
     * 解析StringMatcher<br>
     * regexp:开头的解析为RegexpStringMatcher<br>
     * ant:开头的解析为AntStringMatcher<br>
     * 其余的解析为EqualsStringMatcher<br>
     * 
     * @param text 文本
     * @return StringMatcher列表
     */
    public static List<StringMatcher> parseStringMatcher(String text) {
        List<StringMatcher> result = new ArrayList<>();
        List<String> list = InnerTools.tokenizeToStringList(text);
        for (String item : list) {
            if (item.startsWith("regexp:")) {
                String value = StringTools.removePrefix(item, "regexp:");
                result.add(new RegexpStringMatcher(value));
            } else if (item.startsWith("ant:")) {
                String value = StringTools.removePrefix(item, "ant:");
                result.add(new AntStringMatcher(value));
            } else {
                result.add(new EqualsStringMatcher(item));
            }
        }
        return result;
    }
}
