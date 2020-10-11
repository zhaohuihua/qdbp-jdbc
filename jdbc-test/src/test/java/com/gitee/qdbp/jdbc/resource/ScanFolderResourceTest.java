package com.gitee.qdbp.jdbc.resource;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import com.gitee.qdbp.able.matches.FileMatcher;
import com.gitee.qdbp.able.matches.StringMatcher.LogicType;
import com.gitee.qdbp.able.matches.WrapFileMatcher;
import com.gitee.qdbp.tools.files.PathTools;

public class ScanFolderResourceTest {

    public static void main(String[] args) throws IOException {
        test("settings/", "*.xml");
        System.out.println();
        test("settings/", "*.properties");
        System.out.println();
        test("settings/", "*.txt");
        System.out.println();
        test("settings/", "*.txt,*.xml");
        System.out.println();
        test("settings/", new WrapFileMatcher(LogicType.OR, "name:ant:*.xml", "name:ant:*.properties"));
        System.out.println();
        test("support/http/resources/", "web*.html");
        System.out.println();
        test("support/", "wall.*");
    }

    private static void test(String folder, String filter) {
        System.out.println("<<" + folder + ">>    " + filter);
        List<URL> urls = PathTools.scanResources(folder, filter);
        for (URL url : urls) {
            System.out.println(url);
        }
    }

    private static void test(String folder, FileMatcher matcher) {
        System.out.println("<<" + folder + ">>    " + matcher.toString());
        List<URL> urls = PathTools.scanResources(folder, matcher);
        for (URL url : urls) {
            System.out.println(url);
        }
    }
}
