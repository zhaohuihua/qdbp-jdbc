package com.gitee.qdbp.jdbc.base;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import com.gitee.qdbp.able.matches.FileMatcher;
import com.gitee.qdbp.able.matches.StringMatcher.LogicType;
import com.gitee.qdbp.able.matches.WrapFileMatcher;
import com.gitee.qdbp.tools.files.PathTools;

public class FindResourceTest {

    public static void main(String[] args) throws IOException {
        test("*.xml");
        System.out.println();
        test("*.properties");
        System.out.println();
        test("*.txt");
        System.out.println();
        test(new WrapFileMatcher(LogicType.OR, "name:ant:*.txt", "name:ant:*.xml", "name:ant:*.properties"));
    }

    private static void test(String filter) {
        System.out.println(filter);
        List<URL> urls = PathTools.scanResources("settings/", filter);
        for (URL url : urls) {
            System.out.println(url);
        }
    }

    private static void test(FileMatcher matcher) {
        System.out.println(matcher.toString());
        List<URL> urls = PathTools.scanResources("settings/", matcher);
        for (URL url : urls) {
            System.out.println(url);
        }
    }
}
