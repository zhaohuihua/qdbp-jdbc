package com.gitee.qdbp.jdbc.sql.parse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.gitee.qdbp.able.exception.ResourceNotFoundException;
import com.gitee.qdbp.jdbc.model.DbType;
import com.gitee.qdbp.jdbc.sql.parse.SqlFragmentContainer.TagData;
import com.gitee.qdbp.staticize.common.IMetaData;
import com.gitee.qdbp.staticize.common.IReader;
import com.gitee.qdbp.staticize.io.IReaderCreator;
import com.gitee.qdbp.staticize.io.SimpleReader;
import com.gitee.qdbp.staticize.parse.TagParser;
import com.gitee.qdbp.staticize.tags.base.Taglib;
import com.gitee.qdbp.tools.files.PathTools;
import com.gitee.qdbp.tools.utils.ConvertTools;
import com.gitee.qdbp.tools.utils.StringTools;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * SQL模板解析<br>
 * SQL模板不支持静态的include, 因为解析时无法确定dbType<br>
 * include标签解析时只记录引用的路径, 发布时动态加载引用的内容<br>
 *
 * @author zhaohuihua
 * @version 20200817
 */
class SqlFragmentParser {

    private static Logger log = LoggerFactory.getLogger(SqlFragmentParser.class);

    private String commentTagName = "comment";
    private String importTagName = "import";
    private Taglib taglib;
    private Map<String, ?> dbTypes = new HashMap<>();

    private CacheBox cacheBox = new CacheBox();
    /** 已加入缓存中的内容, key=sqlKey, value=location **/
    private Map<String, String> cachedMaps = new HashMap<>();
    /** 是否允许通过fragmentId获取SQL片断 **/
    private boolean useFragmentIdQuery = true;
    /** SqlKey的别名, key=sqlKey, value=fragmentKey **/
    private Map<String, String> sqlKeyAlias = new HashMap<>();
    /** 冲突列表, key=sqlKey, value=[location] **/
    private Map<String, List<String>> conflicts = new HashMap<>();

    public SqlFragmentParser(Taglib taglib, List<DbType> dbTypes) {
        this.taglib = taglib;
        for (DbType dbType : dbTypes) {
            this.dbTypes.put(dbType.name().toLowerCase(), null);
        }
    }

    public void parseSqlContent(String sqlPath, String sqlContent) {
        StringBuilder buffer = new StringBuilder(sqlContent);
        clearCommentContent(buffer, '<' + commentTagName + '>', '<' + '/' + commentTagName + '>', true);
        clearCommentContent(buffer, "/*--", "--*/", true);
        clearCommentContent(buffer, "<%--", "--%>", true);

        List<SqlFragment> sqlFragments = splitSqlFile(sqlPath, buffer.toString());
        for (SqlFragment sqlFragment : sqlFragments) {
            registerSqlFragment(sqlPath, sqlFragment);
        }
    }

    public List<TagData> parseCachedSqlFragments() {
        this.printConflictLogs();
        List<String> sqlKeys = cacheBox.getSqlKeys();
        List<TagData> tagDatas = new ArrayList<>();
        for (String sqlKey : sqlKeys) {
            TagParser parser = new TagParser(taglib, cacheBox);
            IMetaData metadata;
            try {
                metadata = parser.parse(sqlKey);
            } catch (Exception e) {
                log.warn("Sql template parse error: {}", sqlKey, e);
                continue;
            }
            String fragmentKey = null;
            if (useFragmentIdQuery && sqlKeyAlias.containsKey(sqlKey)) {
                // 为实现根据fragmentId(dbType)也能获取到SQL片断, 根据fragmentKey再注册一次
                fragmentKey = sqlKeyAlias.get(sqlKey);
            }
            tagDatas.add(new TagData(sqlKey, fragmentKey, metadata));
        }
        return tagDatas;
    }

    public CacheBox getCacheBox() {
        return this.cacheBox;
    }

    private void printConflictLogs() {
        if (conflicts.isEmpty() || !log.isWarnEnabled()) {
            return;
        }
        // 输出冲突日志
        StringBuilder buffer = new StringBuilder();
        List<String> sqlKeys = new ArrayList<>(conflicts.keySet());
        Collections.sort(sqlKeys);
        for (String sqlKey : sqlKeys) {
            if (buffer.length() > 0) {
                buffer.append('\n');
            }
            buffer.append(sqlKey).append(' ').append("conflict with:");
            List<String> conflictPaths = conflicts.get(sqlKey);
            buffer.append("\n  ").append(ConvertTools.joinToString(conflictPaths, "\n  "));
        }
        log.warn("Sql template conflict list:\n{}", buffer.toString());
    }

    private void registerSqlFragment(String sqlPath, SqlFragment sqlFragment) {
        SqlId result = parseSqlId(sqlPath, sqlFragment.getId(), sqlFragment.getLine());
        // 如 fileId=user.manage, fragmentId=user.resource.query, dbType=mysql
        // fragmentId和dbType都有可能为空
        // sqlId=fileId:fragmentId
        // sqlKey=fileId:fragmentId(dbType)
        String fileId = result.getFileId();
        String fragmentId = result.getFragmentId();
        String dbType = VerifyTools.nvl(result.getDbType(), "*");
        String sqlRelativePath = getSqlRelativePath(sqlPath);
        if (fragmentId == null) { // 只有文件名, 没有SqlId
            // 注册fileId(dbType)
            String sqlId = fileId;
            String sqlKey = sqlId + '(' + dbType + ')';
            String sqlLocation = sqlRelativePath;
            IReader reader = new SimpleReader(sqlLocation, sqlFragment.getContent());
            if (checkCachedSqlFragment(sqlKey, sqlLocation)) {
                cacheBox.register(sqlKey, reader);
            }
        } else {
            // 注册fileId:fragmentId(dbType)
            String sqlId = fileId + ':' + fragmentId;
            String sqlKey = sqlId + '(' + dbType + ')';
            String fragmentKey = fragmentId + '(' + dbType + ')';
            String sqlLocation = fragmentId + '@' + sqlRelativePath + ':' + sqlFragment.getLine();
            IReader reader = new SimpleReader(sqlLocation, sqlFragment.getContent());
            if (checkCachedSqlFragment(sqlKey, sqlLocation)) {
                cacheBox.register(sqlKey, reader);
                if (useFragmentIdQuery && checkCachedSqlFragment(fragmentKey, sqlLocation)) {
                    sqlKeyAlias.put(sqlKey, fragmentKey);
                }
            }
        }
    }

    private String getSqlRelativePath(String sqlPath) {
        // jar:file:/E:/repository/com/gitee/qdbp/xxx-1.0.0.jar!/settings/sqls/xxx.properties
        // file:/D:/qdbp/qdbp-jdbc/jdbc-core/target/classes/settings/sqls/xxx.properties
        String lowerCase = sqlPath.toLowerCase();
        int jarIndex = lowerCase.indexOf(".jar!/");
        if (jarIndex > 0) {
            int startIndex = lowerCase.lastIndexOf('/', jarIndex);
            if (startIndex > 0) {
                return sqlPath.substring(startIndex + 1);
            }
        }
        int classesIndex = lowerCase.indexOf("/classes/");
        if (classesIndex > 0) {
            return sqlPath.substring(classesIndex + "/classes/".length());
        }
        return sqlPath;
    }

    private boolean checkCachedSqlFragment(String sqlKey, String sqlLocation) {
        if (!cachedMaps.containsKey(sqlKey)) {
            cachedMaps.put(sqlKey, sqlLocation);
            return true;
        } else {
            String oldLocation = cachedMaps.get(sqlKey);
            if (!conflicts.containsKey(sqlKey)) {
                conflicts.put(sqlKey, ConvertTools.toList(oldLocation));
            }
            conflicts.get(sqlKey).add(sqlLocation + " [ignored]");
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

    private SqlId parseSqlId(String sqlPath, String fragmentId, int lineIndex) {
        // 如 fileName=user.manage.sql, fileId=user.manage, dbType=null
        // 如 fileName=user.manage.mysql.sql, fileId=user.manage, dbType=mysql
        String fileName = PathTools.getFileName(sqlPath);
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
                log.warn(msg, tempType, fragmentId, sqlPath, lineIndex);
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
                String msg = "DbType[{}] of the file conflicts with SqlFragment[{}], use [{}], {}(L{})";
                log.warn(msg, dbType, originalId, fragmentDbType, sqlPath, lineIndex);
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
     * @param sqlPath 文件路径
     * @param content 文本内容
     * @return SQL片断列表
     */
    protected List<SqlFragment> splitSqlFile(String sqlPath, String content) {
        String[] lines = StringTools.split(content, false, '\n');
        // 当前的SqlId
        String sqlId = null;
        // sqlId所在的行号(从1开始)
        int sqlLine = 0;
        // SQL片断缓存
        List<String> buffer = new ArrayList<>();
        // 出现在SqlId之前的公共import语句, key=行号(从1开始), value=import语句
        Map<Integer, String> importMaps = new HashMap<>();

        List<SqlFragment> sqlFragments = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            String line = StringTools.removeRight(lines[i], '\r');
            LineItem item = parseLineContent(line);
            if (item instanceof ImportStatement) {
                if (sqlId == null) { // 出现在SqlId之前的import语句
                    importMaps.put(i + 1, item.getText());
                } else {
                    buffer.add(item.getText());
                }
            } else if (item instanceof SqlIdDefinition) {
                if (!buffer.isEmpty()) {
                    // 遇到新的SqlId, 处理这个SqlId上面已经缓存的SQL片断
                    handleSqlFragment(sqlPath, sqlId, sqlLine, buffer, importMaps, sqlFragments);
                    buffer.clear();
                }
                sqlId = ((SqlIdDefinition) item).getSqlId();
                sqlLine = i + 1;
            } else {
                buffer.add(item.getText());
            }
        }
        if (!buffer.isEmpty()) {
            // 处理剩下的Sql片断
            handleSqlFragment(sqlPath, sqlId, sqlLine, buffer, importMaps, sqlFragments);
        }
        return sqlFragments;
    }

    private void handleSqlFragment(String sqlPath, String sqlId, int sqlLine, List<String> buffer,
            Map<Integer, String> importMaps, List<SqlFragment> sqlFragments) {
        // 生成SQL内容, 为避免影响源码位置, 第2个片断会从第1个片断结束的位置开始, 前面填充换行符(或公共import语句)
        String sqlContent = generateSqlContent(sqlLine, buffer, importMaps);
        // 判断除了import/注释/空格/换行之外有没有实质的SQL语句
        if (existSqlFragment(sqlContent)) {
            // 这里的sqlId有可能为空
            sqlFragments.add(new SqlFragment(sqlId, sqlLine, sqlContent));
        } else if (sqlId != null) {
            String msg = "Not found content under the SqlFragment[{}], {}(L{})";
            log.warn(msg, sqlId, sqlPath, sqlLine);
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
        trimed = trimed.trim();
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
                return new ImportStatement(className, importTag);
            }
        } else {
            // 普通文本
            return new LineItem(string);
        }
    }

    // 保留sql片断在原始文件中的行号
    // 保留公共的import语句
    // importMaps: 公共import语句, key=行号(从1开始), value=import语句
    protected String generateSqlContent(int startLine, List<String> contents, Map<Integer, String> importMaps) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 1; i <= startLine; i++) {
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
        if (string.trim().isEmpty()) {
            return false;
        }
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
            if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9') {
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

    private static class CacheBox implements IReaderCreator {

        // key=sqlKey
        private Map<String, IReader> cache = new HashMap<>();

        // sqlKey=fileId:fragmentId(dbType)
        public void register(String sqlKey, IReader reader) {
            cache.put(sqlKey, reader);
        }

        public List<String> getSqlKeys() {
            return new ArrayList<>(cache.keySet());
        }

        @Override
        public IReader create(String sqlId) throws ResourceNotFoundException {
            IReader reader = cache.get(sqlId);
            if (reader != null) {
                return reader;
            } else {
                throw new ResourceNotFoundException(sqlId + " not found");
            }
        }

        @Override
        public String getRelativePath(String sqlId, String newId) {
            return newId;
        }

        @Override
        public Date getUpdateTime(String path) {
            return null;
        }
    }
}
