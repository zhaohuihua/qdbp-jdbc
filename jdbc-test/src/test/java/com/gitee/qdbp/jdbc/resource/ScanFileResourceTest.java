package com.gitee.qdbp.jdbc.resource;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import com.gitee.qdbp.tools.files.PathTools;

public class ScanFileResourceTest {

    public static void main(String[] args) throws IOException {
        test("logback.xml");
        System.out.println();
        test("setting.properties");
        System.out.println();
        test("settings/tags/taglib.txt");
        System.out.println();
        test("support/http/resources/api.html");
        System.out.println();
        test("META-INF/MANIFEST.MF");
    }

    private static void test(String resource) {
        System.out.println("<<" + resource + ">>");
        List<URL> urls = PathTools.scanResources(resource);
        for (URL url : urls) {
            System.out.println(url);
        }
    }

}
