package com.gitee.qdbp.jdbc.sql.mapper;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.gitee.qdbp.able.exception.ResourceNotFoundException;
import com.gitee.qdbp.staticize.common.IReader;
import com.gitee.qdbp.staticize.io.IReaderCreator;
import com.gitee.qdbp.tools.files.PathTools;
import com.gitee.qdbp.tools.utils.StringTools;

/**
 * SQL模板扫描
 *
 * @author zhaohuihua
 * @version 20200817
 */
class SqlTemplateScanner {

    private String commentTagName = "comment";
    private String importTagName = "import";

    public void scanSqlTemplates(String folder) {
        List<URL> urls = PathTools.scanResources(folder, "*.sql");
        CacheBox cache = new CacheBox();
        List<String> errors = new ArrayList<>();
        List<String> conflicts = new ArrayList<>();
        for (URL url : urls) {
            try {
                String content = PathTools.downloadString(url);
                clearCommentContent(content, "/*--", "--*/");
                clearCommentContent(content, "<%--", "--%>");
                clearCommentContent(content, "<comment>", "</comment>");
                String absolutePath = url.toString();
                String relativePath = removeLeftAt(absolutePath, folder);
                registerSqlFragment(cache, absolutePath, relativePath, content);
            } catch (IOException e) {
                errors.add(url.toString() + ' ' + e.getMessage());
            }
        }
    }

    /** 清除注释内容, 替换为comment标签; 为避免影响源码位置, 保留所有换行符 **/
    // /* */ 这个注释是SQL的正常注释, 注释中的内容仍然会解析, 也将会输出到最终生成的SQL中
    // /*-- --*/ 约定这个注释符号为模板注释, 不会解析也不会输出到SQL中, 作用等同于jsp中的<%-- --%>
    private String clearCommentContent(String string, String leftSymbol, String rightSymbol) {
        StringBuilder buffer = new StringBuilder(string);
        int index = 0;
        while (true) {
            int nextStartIndex = buffer.indexOf(leftSymbol, index);
            if (nextStartIndex < 0) {
                break;
            }
            int nextEndIndex = buffer.indexOf(rightSymbol, nextStartIndex);
            if (nextEndIndex < 0) {
                break;
            }
            StringBuilder replacement = new StringBuilder();
            replacement.append('<').append(commentTagName).append('>');
            for (int i = nextStartIndex + leftSymbol.length(); i < nextEndIndex; i++) {
                char c = buffer.charAt(i);
                if (c == '\r' || c == '\n') {
                    replacement.append(c);
                }
            }
            replacement.append('<').append('/').append(commentTagName).append('>');
            buffer.replace(nextStartIndex, nextEndIndex + rightSymbol.length(), replacement.toString());
            index = nextStartIndex + replacement.length();
        }
        return buffer.toString();
    }

    private void registerSqlFragment(CacheBox cache, String absolutePath, String relativePath, String content) {
    }

    /**
     * 按SqlId将一个文本内容拆分为SQL片断<br>
     * 为避免影响源码位置, 第2个片断会从第1个片断结束的位置开始, 前面填充换行符
     * 
     * @param content 文本内容
     * @return SQL片断列表
     */
    private List<SqlFragment> splitSqlFile(String absolutePath, String relativePath, String content) {
        String[] lines = StringTools.split(content, '\n');
        // 当前的SqlId
        String sqlId = null;
        int sqlLine = 0;
        // SQL片断缓存
        List<String> buffer = new ArrayList<>();
        // 出现在SqlId之前的公共import语句, key=行号, value=import语句
        Map<Integer, String> importMaps = new HashMap<>();

        List<SqlFragment> sqlFragments = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            String line = StringTools.removeRight(lines[i], '\r');
            LineItem item = parseLineContent(line);
            if (item instanceof ImportStatement) {
                if (sqlId == null) { // 出现在SqlId之前的import语句
                    importMaps.put(i, item.getText());
                } else {
                    buffer.add(item.getText());
                }
            } else if (item instanceof SqlIdDefinition) {
                if (!buffer.isEmpty() && sqlId == null) {
                    // TODO 判断buffer中除了注释和换行之外有没有SQL语句, 因为这部分内容将被丢弃
                } else if (!buffer.isEmpty() && sqlId != null) {
                    // 遇到新的SqlId, 先处理上一个SQL片断
                    String sqlContent = generateSqlContent(sqlLine, buffer, importMaps);
                    sqlFragments.add(new SqlFragment(absolutePath, relativePath, sqlId, sqlContent));
                }
                buffer.clear();
                sqlId = ((SqlIdDefinition) item).getSqlId();
                sqlLine = i;
            } else {
                buffer.add(item.getText());
            }
        }
        if (!buffer.isEmpty() && sqlId == null) {
            // 整个文档没有SqlId
            String sqlContent = generateSqlContent(sqlLine, buffer, importMaps);
            sqlFragments.add(new SqlFragment(absolutePath, relativePath, null, sqlContent));
        } else if (!buffer.isEmpty() && sqlId != null) {
            // 处理最后一个SQL片断
            String sqlContent = generateSqlContent(sqlLine, buffer, importMaps);
            sqlFragments.add(new SqlFragment(absolutePath, relativePath, sqlId, sqlContent));
        }
    }

    // 保留sql片断在原始文件中的行号
    // 保留公共的import语句
    private String generateSqlContent(int sqlLine, List<String> contents,
            Map<Integer, String> importMaps) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < sqlLine; i++) {
            if (importMaps.containsKey(i)) {
                buffer.append(importMaps.get(i));
            }
            buffer.append('\n');
        }
        for (String line : contents) {
            buffer.append(line).append('\n');
        }
        return buffer.toString();
    }

    /** 解析行内容, 识别为SqlId声明或import语句或普通文本 **/
    // 两个左右尖括号中间的内容就是SqlId
    // -- <<normal.find.children>> 递归查询所有子节点
    // /** <<normal.find.children>> 递归查询所有子节点 **/
    // 以import开头的, 是class导入语句, 替换为import标签
    // -- import com.gitee.qdbp.jdbc.sql.SqlTools
    // /** import com.gitee.qdbp.jdbc.sql.SqlTools **/
    // 替换为
    // <import>com.gitee.qdbp.jdbc.sql.SqlTools</import>
    private LineItem parseLineContent(String string) {
        String trimed = string.trim();
        if (trimed.startsWith("--")) {
            trimed = StringTools.removePrefix(trimed, "--");
        } else if (trimed.startsWith("/**") && trimed.endsWith("**/")) {
            trimed = StringTools.removePrefix(trimed, "/**");
            trimed = StringTools.removeSuffix(trimed, "**/");
        }
        trimed = string.trim();
        if (trimed.startsWith("<<")) {
            // SqlId, 如: <<normal.find.children>>
            String sqlId = StringTools.getSubstringInPairedSymbol(trimed, "<<", ">>");
            if (sqlId == null || sqlId.trim().length() == 0) {
                return new LineItem(string);
            } else {
                return new SqlIdDefinition(sqlId.trim(), string);
            }
        } else if (trimed.startsWith("import")) {
            // import语句, 如: import com.gitee.qdbp.jdbc.sql.SqlTools
            String className = StringTools.removePrefix(trimed, "import");
            className = StringTools.removeSuffixAt(className.trim(), ';');
            if (className == null || className.trim().length() == 0) {
                return new LineItem(string);
            } else {
                className = className.trim();
                // 替换为<import>com.gitee.qdbp.jdbc.sql.SqlTools</import>
                StringBuilder importTag = new StringBuilder();
                importTag.append('<').append(importTagName).append('>');
                importTag.append(className);
                importTag.append('<').append('/').append(importTagName).append('>');
                return new SqlIdDefinition(className, importTag.toString());
            }
        } else {
            // 普通文本
            return new LineItem(string);
        }
    }

    // string = jar:file:/E:/repository/qdbp-jdbc-core-3.0.0.jar!/settings/sqls/account/usermanage.sql
    // substring = settings/sqls/
    // result = settings/sqls/account/usermanage.sql
    private static String removeLeftAt(String string, String substring) {
        int index = string.indexOf(substring);
        return index < 0 ? string : string.substring(index);
    }

    protected static class LineItem {

        /** 文本内容 **/
        private final String text;

        public LineItem(String text) {
            this.text = text;
        }

        /** 文本内容 **/
        public String getText() {
            return text;
        }
    }

    protected static class SqlIdDefinition extends LineItem {

        private final String sqlId;

        public SqlIdDefinition(String sqlId, String text) {
            super(text);
            this.sqlId = sqlId;
        }

        public String getSqlId() {
            return sqlId;
        }
    }

    protected static class ImportStatement extends LineItem {

        private final String className;

        public ImportStatement(String className, String text) {
            super(text);
            this.className = className;
        }

        public String getClassName() {
            return className;
        }
    }

    protected static class SqlFragment {

        private final String absolutePath;
        private final String relativePath;
        private final String sqlId;
        private final String sqlContent;

        public SqlFragment(String absolutePath, String relativePath, String sqlId, String sqlContent) {
            this.absolutePath = absolutePath;
            this.relativePath = relativePath;
            this.sqlId = sqlId;
            this.sqlContent = sqlContent;
        }

        public String getAbsolutePath() {
            return absolutePath;
        }

        public String getRelativePath() {
            return relativePath;
        }

        public String getSqlId() {
            return sqlId;
        }

        public String getSqlContent() {
            return sqlContent;
        }

    }

    static class CacheItem {

        private final String location;
        private final IReader reader;

        public CacheItem(String location, IReader reader) {
            this.location = location;
            this.reader = reader;
        }

        public String getLocation() {
            return location;
        }

        public IReader getReader() {
            return reader;
        }
    }

    static class CacheBox implements IReaderCreator {

        private Map<String, CacheItem> cache = new ConcurrentHashMap<>();

        public void register(String sqlId, CacheItem item) {
            cache.put(sqlId, item);
        }

        public boolean exist(String sqlId) {
            return cache.containsKey(sqlId);
        }

        @Override
        public IReader create(String sqlId) throws IOException, ResourceNotFoundException {
            CacheItem item = cache.get(sqlId);
            return item == null ? null : item.getReader();
        }

        @Override
        public Date getUpdateTime(String path) throws IOException, ResourceNotFoundException {
            return null;
        }
    }
}
