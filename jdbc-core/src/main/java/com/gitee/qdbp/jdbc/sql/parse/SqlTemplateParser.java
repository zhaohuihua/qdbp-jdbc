package com.gitee.qdbp.jdbc.sql.parse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.gitee.qdbp.able.exception.ResourceNotFoundException;
import com.gitee.qdbp.jdbc.model.MainDbType;
import com.gitee.qdbp.staticize.common.IReader;
import com.gitee.qdbp.staticize.io.IReaderCreator;
import com.gitee.qdbp.staticize.io.SimpleReader;
import com.gitee.qdbp.tools.files.PathTools;
import com.gitee.qdbp.tools.utils.ConvertTools;
import com.gitee.qdbp.tools.utils.StringTools;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * SQL模板解析
 *
 * @author zhaohuihua
 * @version 20200817
 */
class SqlTemplateParser {

    private static Logger log = LoggerFactory.getLogger(SqlTemplateScanner.class);

    private String commentTagName = "comment";
    private String importTagName = "import";
    private Map<String, ?> dbTypes = new HashMap<>();

    private CacheBox cacheBox = new CacheBox();
    /** 已加入缓存中的内容, key=sqlKey, value=location **/
    private Map<String, String> cachedMaps = new HashMap<>();
    /** 冲突列表, key=sqlKey, value=[location] **/
    private Map<String, List<String>> conflicts = new HashMap<>();

    public SqlTemplateParser() {
        for (MainDbType dbType : MainDbType.values()) {
            this.dbTypes.put(dbType.name().toLowerCase(), null);
        }
    }

    public void parseSqlContent(String filePath, String sqlContent) {
        StringBuilder buffer = new StringBuilder(sqlContent);
        clearCommentContent(buffer, '<' + commentTagName + '>', '<' + '/' + commentTagName + '>', true);
        clearCommentContent(buffer, "/*--", "--*/", true);
        clearCommentContent(buffer, "<%--", "--%>", true);

        List<SqlFragment> sqlFragments = splitSqlFile(filePath, buffer.toString());
        for (SqlFragment sqlFragment : sqlFragments) {
            registerSqlFragment(filePath, sqlFragment, cacheBox, cachedMaps, conflicts);
        }
    }

    private void registerSqlFragment(String filePath, SqlFragment sqlFragment, CacheBox cacheBox,
            Map<String, String> cachedMaps, Map<String, List<String>> conflicts) {
        SqlId result = parseSqlId(filePath, sqlFragment.getId(), sqlFragment.getLine());
        // 如 fileId=user.manage, fragmentId=user.resource.query, dbType=mysql
        // fragmentId和dbType都有可能为空
        String fileId = result.getFileId();
        String fragmentId = result.getFragmentId();
        String dbType = VerifyTools.nvl(result.getDbType(), "*");
        String dbKey = result.getDbType() == null ? "" : '(' + dbType + ')';
        IReader reader = new SimpleReader(sqlFragment.getContent());
        if (fragmentId == null) {
            String sqlLocation = filePath;
            String sqlId = fileId + ':' + "{default}" + ':' + dbType;
            String sqlKey = fileId + dbKey;
            if (checkCachedSqlFragment(sqlId, sqlKey, sqlLocation, cachedMaps, conflicts)) {
                cacheBox.register(sqlId, new CacheItem(sqlLocation, reader));
            }
        } else {
            String sqlLocation = fragmentId + " @ " + filePath + " line " + sqlFragment.getLine();
            { // 先注册fileId:fragmentId:dbType
                String sqlId = fileId + ':' + fragmentId + ':' + dbType;
                String sqlKey = fileId + ':' + fragmentId + dbKey;
                if (checkCachedSqlFragment(sqlId, sqlKey, sqlLocation, cachedMaps, conflicts)) {
                    cacheBox.register(sqlId, new CacheItem(sqlLocation, reader));
                }
            }
            { // 再注册fragmentId:dbType
                String sqlId = fragmentId + ':' + dbType;
                String sqlKey = fragmentId + dbKey;
                if (checkCachedSqlFragment(sqlId, sqlKey, sqlLocation, cachedMaps, conflicts)) {
                    cacheBox.register(sqlId, new CacheItem(sqlLocation, reader));
                }
            }
        }
    }

    private boolean checkCachedSqlFragment(String sqlId, String sqlKey, String sqlLocation,
            Map<String, String> cachedMaps, Map<String, List<String>> conflicts) {
        if (!cachedMaps.containsKey(sqlKey)) {
            cachedMaps.put(sqlKey, sqlLocation);
            return true;
        } else {
            String oldLocation = cachedMaps.get(sqlKey);
            if (!conflicts.containsKey(sqlKey)) {
                conflicts.put(sqlKey, ConvertTools.toList(oldLocation));
            }
            conflicts.get(sqlKey).add("[ignored] " + sqlLocation);
            return false;
        }
    }

    protected static class SqlId {

        private final String fileId;
        private final String fragmentId;
        private final String dbType;

        public SqlId(String fileId, String fragmentId, String dbType) {
            this.fileId = fileId;
            this.fragmentId = fragmentId;
            this.dbType = dbType;
        }

        public String getFileId() {
            return fileId;
        }

        public String getFragmentId() {
            return fragmentId;
        }

        public String getDbType() {
            return dbType;
        }
    }

    private SqlId parseSqlId(String filePath, String fragmentId, int lineIndex) {
        // 如 fileName=user.manage.sql, fileId=user.manage, dbType=null
        // 如 fileName=user.manage.mysql.sql, fileId=user.manage, dbType=mysql
        String fileName = PathTools.getFileName(filePath);
        String fileId = PathTools.removeExtension(fileName);
        String dbType = null;
        int fileDotIndex = fileId.lastIndexOf('.');
        if (fileDotIndex > 0) {
            String tempType = fileId.substring(fileDotIndex + 1);
            if (dbTypes.containsKey(tempType)) {
                dbType = tempType.toLowerCase();
                fileId = fileId.substring(0, fileDotIndex);
            }
        }
        if (fragmentId == null) {
            return new SqlId(fileId, null, dbType);
        }

        // 如 user.resource.query 或 user.resource.query:*
        // 如 user.resource.query.mysql 或 user.resource.query:mysql
        String originalId = fragmentId;
        String fragmentDbType = null;
        int fragmentColonIndex = fragmentId.indexOf(':');
        if (fragmentColonIndex >= 0) {
            // user.resource.query:mysql 或 user.resource.query:*
            fragmentId = fragmentId.substring(0, fragmentColonIndex).trim();
            String tempType = fragmentId.substring(fragmentColonIndex + 1).trim();
            if ("*".equals(tempType)) {
                fragmentDbType = null;
            } else if (dbTypes.containsKey(tempType)) {
                fragmentDbType = tempType.toLowerCase();
            } else {
                String msg = "DbType[{}] of the SqlFragment[{}] is unsupported, {} line {}";
                log.warn(msg, tempType, fragmentId, filePath, lineIndex);
            }
        } else {
            int fragmentDotIndex = fragmentId.lastIndexOf('.');
            if (fragmentDotIndex > 0) {
                // user.resource.query.mysql
                String tempType = fragmentId.substring(fragmentDotIndex + 1);
                if (dbTypes.containsKey(tempType)) {
                    fragmentDbType = tempType.toLowerCase();
                    fragmentId = fragmentId.substring(0, fragmentDotIndex);
                }
            }
        }
        // 文件上指定的DbType与SQL片段指定的是否一致
        // 如果不一致, 以SQL片段的为准
        if (fragmentDbType != null) {
            if (dbType == null) {
                dbType = fragmentDbType;
            } else if (!dbType.equals(fragmentDbType)) {
                String msg = "DbType[{}] of the file conflicts with SqlFragment[{}], use [{}], {} line {}";
                log.warn(msg, dbType, originalId, fragmentDbType, filePath, lineIndex);
                dbType = fragmentDbType;
            }
        }

        // fileId=user.manage, fragmentId=user.resource.query, dbType=mysql
        return new SqlId(fileId, fragmentId, dbType);
    }

    /**
     * 按SqlId将一个文本内容拆分为SQL片断<br>
     * 为避免影响源码位置, 第2个片断会从第1个片断结束的位置开始, 前面填充换行符
     * 
     * @param filePath 文件路径
     * @param content 文本内容
     * @return SQL片断列表
     */
    protected List<SqlFragment> splitSqlFile(String filePath, String content) {
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
                if (!buffer.isEmpty()) {
                    // 遇到新的SqlId, 处理这个SqlId上面已经缓存的SQL片断
                    handleSqlFragment(filePath, sqlId, sqlLine, buffer, importMaps, sqlFragments);
                }
                buffer.clear();
                sqlId = ((SqlIdDefinition) item).getSqlId();
                sqlLine = i + 1;
            } else {
                buffer.add(item.getText());
            }
        }
        if (!buffer.isEmpty()) {
            // 处理剩下的Sql片断
            handleSqlFragment(filePath, sqlId, sqlLine, buffer, importMaps, sqlFragments);
        }
        return sqlFragments;
    }

    private void handleSqlFragment(String filePath, String sqlId, int sqlLine, List<String> buffer,
            Map<Integer, String> importMaps, List<SqlFragment> sqlFragments) {
        // 生成SQL内容, 为避免影响源码位置, 第2个片断会从第1个片断结束的位置开始, 前面填充换行符(或公共import语句)
        String sqlContent = generateSqlContent(sqlLine, buffer, importMaps);
        // 判断除了import/注释/空格/换行之外有没有实质的SQL语句
        if (existSqlFragment(sqlContent)) {
            // 这里的sqlId有可能为空
            sqlFragments.add(new SqlFragment(sqlId, sqlLine, sqlContent));
        } else if (sqlId != null) {
            String msg = "Not found content under the SqlFragment[{}], {} line {}";
            log.warn(msg, sqlId, filePath, sqlLine);
        }
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
    protected LineItem parseLineContent(String string) {
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
                String importTag = generateSimpleTag(importTagName, className);
                return new SqlIdDefinition(className, importTag);
            }
        } else {
            // 普通文本
            return new LineItem(string);
        }
    }

    // 保留sql片断在原始文件中的行号
    // 保留公共的import语句
    protected String generateSqlContent(int startLine, List<String> contents, Map<Integer, String> importMaps) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < startLine; i++) {
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

    protected String generateSimpleTag(String tagName, String tagContent) {
        StringBuilder buffer = new StringBuilder();
        buffer.append('<').append(tagName).append('>');
        buffer.append(tagContent);
        buffer.append('<').append('/').append(tagName).append('>');
        return buffer.toString();
    }

    /** 清除注释内容, 替换为comment标签; 为避免影响源码位置, 保留所有换行符 **/
    // /* */ 这个注释是SQL的正常注释, 注释中的内容仍然会解析, 也将会输出到最终生成的SQL中
    // /*-- --*/ 约定这个注释符号为模板注释, 不会解析也不会输出到SQL中, 作用等同于jsp中的<%-- --%>
    protected void clearCommentContent(StringBuilder string, String leftSymbol, String rightSymbol, boolean keepLines) {
        int index = 0;
        while (true) {
            int nextStartIndex = string.indexOf(leftSymbol, index);
            if (nextStartIndex < 0) {
                break;
            }
            int nextEndIndex = string.indexOf(rightSymbol, nextStartIndex);
            if (nextEndIndex < 0) {
                break;
            }
            if (!keepLines) {
                string.delete(nextStartIndex, nextEndIndex + rightSymbol.length());
                index = nextStartIndex;
            } else {
                int startIndex = nextStartIndex + leftSymbol.length();
                String replacement = generateKeepLinesCommentTag(string, startIndex, nextEndIndex);
                string.replace(nextStartIndex, nextEndIndex + rightSymbol.length(), replacement);
                index = nextStartIndex + replacement.length();
            }
        }
    }

    protected void clearLineComment(StringBuilder string, String leadingSymbol, boolean keepLines,
            boolean allowBehindWithAscii) {
        String trailingSymbol = "\n";
        int index = 0;
        while (true) {
            int nextStartIndex = string.indexOf(leadingSymbol, index);
            if (nextStartIndex < 0) {
                break;
            }
            if (nextStartIndex > 0 && !allowBehindWithAscii) {
                // --前面的字符不能是英文字母, 以免i--这样的情况被清除
                char c = string.charAt(nextStartIndex - 1);
                if (c >= 'a' && c <= 'z' || c >= 'A' || c <= 'Z') {
                    index = nextStartIndex + leadingSymbol.length();
                    continue;
                }
            }
            int nextEndIndex = string.indexOf(trailingSymbol, nextStartIndex);
            if (nextEndIndex < 0) {
                nextEndIndex = string.length();
            }
            if (!keepLines) {
                string.delete(nextStartIndex, nextEndIndex + trailingSymbol.length());
                index = nextStartIndex;
            } else {
                int startIndex = nextStartIndex + leadingSymbol.length();
                String replacement = generateKeepLinesCommentTag(string, startIndex, nextEndIndex);
                string.replace(nextStartIndex, nextEndIndex + trailingSymbol.length(), replacement);
                index = nextStartIndex + replacement.length();
            }
        }
    }

    protected String generateKeepLinesCommentTag(CharSequence original, int startIndex, int endIndex) {
        StringBuilder replacement = new StringBuilder();
        replacement.append('<').append(commentTagName).append('>');
        for (int i = startIndex; i < endIndex; i++) {
            char c = original.charAt(i);
            if (c == '\r' || c == '\n') {
                replacement.append(c);
            }
        }
        replacement.append('<').append('/').append(commentTagName).append('>');
        return replacement.toString();
    }

    /** 判断除了import/注释/空格/换行之外有没有实质的SQL语句 **/
    protected boolean existSqlFragment(String string) {
        StringBuilder buffer = new StringBuilder(string);
        clearCommentContent(buffer, '<' + importTagName + '>', '<' + '/' + importTagName + '>', false);
        clearCommentContent(buffer, '<' + commentTagName + '>', '<' + '/' + commentTagName + '>', false);
        clearCommentContent(buffer, "<%--", "--%>", false);
        clearCommentContent(buffer, "/*", "*/", false);
        clearLineComment(buffer, "--", false, false);
        return countAsciiChars(buffer) > 0;
    }

    protected int countAsciiChars(StringBuilder string) {
        int count = 0;
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (c >= 'a' && c <= 'z' || c >= 'A' || c <= 'Z' || c >= '0' || c <= '9') {
                count++;
            }
        }
        return count;
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

        private final String id;
        private final int line;
        private final String content;

        public SqlFragment(String id, int line, String content) {
            this.id = id;
            this.line = line;
            this.content = content;
        }

        public String getId() {
            return id;
        }

        public int getLine() {
            return line;
        }

        public String getContent() {
            return content;
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

        @Override
        public IReader create(String sqlId) throws IOException, ResourceNotFoundException {
            CacheItem item = cache.get(sqlId);
            return item == null ? null : item.getReader();
        }

        @Override
        public String getRealPath(String sqlId) throws IOException, ResourceNotFoundException {
            CacheItem item = cache.get(sqlId);
            return item == null ? null : item.getLocation();
        }

        @Override
        public Date getUpdateTime(String path) throws IOException, ResourceNotFoundException {
            return null;
        }
    }
}
