package com.gitee.qdbp.jdbc.sql.parse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.gitee.qdbp.able.beans.KeyString;
import com.gitee.qdbp.able.exception.ResourceNotFoundException;
import com.gitee.qdbp.jdbc.model.DbType;
import com.gitee.qdbp.jdbc.sql.parse.SqlFragmentContainer.ParsedFragment;
import com.gitee.qdbp.staticize.common.IMetaData;
import com.gitee.qdbp.staticize.common.IReader;
import com.gitee.qdbp.staticize.io.IReaderCreator;
import com.gitee.qdbp.staticize.io.SimpleReader;
import com.gitee.qdbp.staticize.parse.TagParser;
import com.gitee.qdbp.staticize.tags.base.Taglib;
import com.gitee.qdbp.tools.files.PathTools;
import com.gitee.qdbp.tools.utils.ConvertTools;
import com.gitee.qdbp.tools.utils.StringTools;

/**
 * SQL模板解析<br>
 * SQL模板不支持静态的include, 因为解析时无法确定dbType<br>
 * include标签解析时只记录引用的路径, 发布时动态加载引用的内容<br>
 *
 * @author zhaohuihua
 * @version 20200817
 * @since 3.2.0
 */
class SqlFragmentParser {

    private static Logger log = LoggerFactory.getLogger(SqlFragmentParser.class);

    private String commentTagName = "comment";
    private String importTagName = "import";
    private String supportsTagName = "supports";
    private Taglib taglib;
    private Map<String, ?> dbTypes = new HashMap<>();
    /** 是否允许通过fragmentId获取SQL片断 **/
    private boolean useFragmentIdQuery = true;

    private CacheBox cacheBox = new CacheBox();

    // sqlKey=fileId:fragmentId(supports)
    /** 已加入缓存中的内容, key=sqlKeySorted, value={sqlKeyOriginal, location} **/
    private Map<String, KeyString> cachedMaps = new HashMap<>();
    /** 冲突列表, key=sqlKeySorted, value=[{sqlKeyOriginal, location}] **/
    private Map<String, List<KeyString>> conflicts = new HashMap<>();

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

    public List<ParsedFragment> parseCachedSqlFragments() {
        this.printConflictLogs();
        // sqlKey=fileId:fragmentId(supports)
        List<String> sqlKeys = cacheBox.getSqlKeys();
        List<ParsedFragment> tagDatas = new ArrayList<>();
        for (String sqlKey : sqlKeys) {
            TmplData data = cacheBox.getSqlFragment(sqlKey);
            TagParser parser = new TagParser(taglib, cacheBox);
            try {
                IMetaData metadata = parser.parse(sqlKey);
                tagDatas.add(new ParsedFragment(data.getSqlId(), data.getSqlAlias(), data.getSupports(), metadata));
            } catch (Exception e) {
                log.warn("Sql template parse error: {}", data.getReader().getRealPath(), e);
                continue;
            }
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
        List<String> sqlKeySorteds = new ArrayList<>(conflicts.keySet());
        Collections.sort(sqlKeySorteds);
        for (String sqlKeySorted : sqlKeySorteds) {
            if (buffer.length() > 0) {
                buffer.append('\n');
            }
            List<KeyString> conflictContents = conflicts.get(sqlKeySorted);
            String sqlKeyOriginal = conflictContents.get(0).getKey();
            List<String> conflictLocations = getValuesOfKeyStringList(conflictContents);
            buffer.append(sqlKeyOriginal).append(' ').append("conflict with:");
            buffer.append("\n  ").append(ConvertTools.joinToString(conflictLocations, "\n  "));
        }
        log.warn("Sql template conflict list:\n{}", buffer.toString());
    }

    private List<String> getValuesOfKeyStringList(List<KeyString> list) {
        List<String> values = new ArrayList<>();
        for (KeyString item : list) {
            values.add(item.getValue());
        }
        return values;
    }

    private void registerSqlFragment(String sqlPath, SqlFragment sqlFragment) {
        SqlId result = parseSqlId(sqlPath, sqlFragment);
        // 如 fileId=user.manage, fragmentId=user.resource.query, supports=mysql
        // fragmentId和supports都有可能为空
        // sqlId=fileId:fragmentId
        // sqlKey=fileId:fragmentId(supports)
        String fileId = result.getFileId();
        String fragmentId = result.getFragmentId();
        String supports = result.getSupports() != null ? result.getSupports() : "*";
        String sqlRelativePath = getSqlRelativePath(sqlPath);
        if (fragmentId == null) { // 只有文件名, 没有SqlId
            // 注册fileId(supports)
            String sqlId = fileId;
            String sqlKey = sqlId + '(' + supports + ')';
            String sqlLocation = sqlRelativePath;
            IReader reader = new SimpleReader(sqlLocation, sqlFragment.getContent());
            if (checkCachedSqlFragment(sqlId, supports, sqlLocation)) {
                cacheBox.register(sqlKey, new TmplData(sqlId, null, supports, reader));
            }
        } else {
            // 注册fileId:fragmentId(supports)
            String sqlId = fileId + ':' + fragmentId;
            String sqlKey = sqlId + '(' + supports + ')';
            String sqlLocation = fragmentId + '@' + sqlRelativePath + ':' + sqlFragment.getLine();
            IReader reader = new SimpleReader(sqlLocation, sqlFragment.getContent());
            if (checkCachedSqlFragment(sqlId, supports, sqlLocation)) {
                String sqlAlias = null;
                if (useFragmentIdQuery && checkCachedSqlFragment(fragmentId, supports, sqlLocation)) {
                    sqlAlias = fragmentId;
                }
                cacheBox.register(sqlKey, new TmplData(sqlId, sqlAlias, supports, reader));
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

    private boolean checkCachedSqlFragment(String sqlId, String supports, String sqlLocation) {
        String sqlKeySorted = sqlId + '(' + sortSupports(supports) + ')';
        String sqlKeyOriginal = sqlId + '(' + supports + ')';
        if (!cachedMaps.containsKey(sqlKeySorted)) {
            cachedMaps.put(sqlKeySorted, new KeyString(sqlKeyOriginal, sqlLocation));
            return true;
        } else {
            KeyString original = cachedMaps.get(sqlKeySorted);
            KeyString current = new KeyString(sqlKeyOriginal, sqlLocation + " [ignored]");
            if (conflicts.containsKey(sqlKeySorted)) {
                conflicts.get(sqlKeySorted).add(current);
            } else {
                conflicts.put(sqlKeySorted, ConvertTools.toList(original, current));
            }
            return false;
        }
    }

    private String sortSupports(String supports) {
        if (supports == null || "*".equals(supports)) {
            return supports;
        }
        supports = StringTools.removeLeftRight(supports.trim(), ',');
        String[] supportArray = StringTools.split(supports, ',');
        Arrays.sort(supportArray);
        return ConvertTools.joinToString(supportArray, ',');
    }

    protected static class SqlId {

        private final String fileId;
        private final String fragmentId;
        private final String supports;

        public SqlId(String fileId, String fragmentId, String supports) {
            this.fileId = fileId;
            this.fragmentId = fragmentId;
            this.supports = supports;
        }

        public String getFileId() {
            return fileId;
        }

        public String getFragmentId() {
            return fragmentId;
        }

        public String getSupports() {
            return supports;
        }
    }

    private SqlId parseSqlId(String sqlPath, SqlFragment sqlFragment) {
        String fragmentId = sqlFragment.getId();
        // 如 fileName=user.manage.sql, fileId=user.manage, fileDbType=null
        // 如 fileName=user.manage.mysql.sql, fileId=user.manage, fileDbType=mysql
        String fileName = PathTools.getFileName(sqlPath);
        String fileId = PathTools.removeExtension(fileName);
        String fileDbType = null;
        int fileDotIndex = fileId.lastIndexOf('.');
        if (fileDotIndex > 0) {
            String tempType = fileId.substring(fileDotIndex + 1);
            if (dbTypes.containsKey(tempType)) {
                fileDbType = tempType.toLowerCase();
                fileId = fileId.substring(0, fileDotIndex);
            }
        }
        if (fragmentId == null) {
            return new SqlId(fileId, null, resolveSqlFragmentSupports(fileDbType, null, sqlFragment));
        }

        // 如 user.resource.query 或 user.resource.query:*
        // 或 user.resource.query:mysql 或 user.resource.query:mysql.8
        String originalId = fragmentId;
        String fragmentDbType = null;
        int fragmentColonIndex = fragmentId.indexOf(':');
        if (fragmentColonIndex >= 0) {
            // user.resource.query:mysql 或 user.resource.query:*
            fragmentId = originalId.substring(0, fragmentColonIndex).trim();
            fragmentDbType = originalId.substring(fragmentColonIndex + 1).trim();
        }

        // fileId=user.manage, fragmentId=user.resource.query, fragmentDbType=mysql
        return new SqlId(fileId, fragmentId, resolveSqlFragmentSupports(fileDbType, fragmentDbType, sqlFragment));
    }

    // 取值顺序: innerSupports/fragmentDbType/commonSupports/fileDbType
    private String resolveSqlFragmentSupports(String fileDbType, String fragmentDbType, SqlFragment sqlFragment) {
        if (sqlFragment.getInnerSupports() != null) {
            return sqlFragment.getInnerSupports().getContent();
        } else if (fragmentDbType != null) {
            return fragmentDbType;
        } else if (sqlFragment.getCommonSupports() != null) {
            return sqlFragment.getCommonSupports().getContent();
        } else if (fileDbType != null) {
            return fileDbType;
        } else {
            return null;
        }
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
        // 当前SQL片断的版本支持声明
        LineContent supports = null;
        // 出现在SqlId之前的公共版本支持声明
        LineContent commSupports = null;
        // 出现在SqlId之前的公共import语句, key=行号(从1开始), value=import语句
        Map<Integer, String> commImports = new HashMap<>();

        List<SqlFragment> allSqls = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            String line = StringTools.removeRight(lines[i], '\r');
            LineItem item = parseLineContent(line);
            if (item instanceof ImportStatement) {
                if (sqlId == null) { // 出现在SqlId之前的公共import语句
                    commImports.put(i + 1, item.getText());
                } else {
                    buffer.add(item.getText());
                }
            } else if (item instanceof SupportsStatement) {
                if (sqlId == null) { // 出现在SqlId之前的公共版本支持声明
                    if (commSupports != null) {
                        String msg = "<{}> line {} ignored, conflict with line {}, in SqlTemplate {}";
                        log.warn(msg, supportsTagName, i, commSupports.getLine(), sqlPath);
                    } else {
                        commSupports = new LineContent(i, item.getText());
                    }
                } else {
                    if (supports != null) {
                        String msg = "<{}> line {} ignored, conflict with line {}, under the SqlFragment[{}] {}(L{})";
                        log.warn(msg, supportsTagName, i, supports.getLine(), sqlId, sqlPath, sqlLine);
                    } else {
                        supports = new LineContent(i, item.getText());
                    }
                }
            } else if (item instanceof SqlIdDefinition) {
                if (!buffer.isEmpty()) {
                    // 遇到新的SqlId, 处理旧的SqlId已经缓存的SQL片断
                    handleSqlFragment(sqlPath, sqlId, sqlLine, supports, commSupports, commImports, buffer, allSqls);
                    buffer.clear();
                }
                sqlId = ((SqlIdDefinition) item).getSqlId();
                supports = null;
                sqlLine = i + 1;
            } else {
                buffer.add(item.getText());
            }
        }
        if (!buffer.isEmpty()) {
            // 处理剩下的Sql片断
            handleSqlFragment(sqlPath, sqlId, sqlLine, supports, commSupports, commImports, buffer, allSqls);
        }
        return allSqls;
    }

    private void handleSqlFragment(String sqlPath, String sqlId, int sqlLine, LineContent innerSupports,
            LineContent commonSupports, Map<Integer, String> importMaps, List<String> buffer,
            List<SqlFragment> sqlFragments) {
        // 生成SQL内容, 为避免影响源码位置, 第2个片断会从第1个片断结束的位置开始, 前面填充换行符(或公共import语句)
        String sqlContent = generateSqlContent(sqlLine, buffer, importMaps);
        // 判断除了import/supports/注释/空格/换行之外有没有实质的SQL语句
        if (existSqlFragment(sqlContent)) {
            // 这里的sqlId有可能为空
            sqlFragments.add(new SqlFragment(sqlId, sqlLine, innerSupports, commonSupports, sqlContent));
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
        } else if (trimed.startsWith(importTagName)) {
            // import语句, 如: import com.gitee.qdbp.jdbc.sql.SqlTools
            String className = StringTools.removePrefix(trimed, importTagName);
            className = StringTools.removeSuffixAt(className.trim(), ';');
            if (className == null || className.trim().length() == 0) {
                return new LineItem(string);
            } else {
                className = className.trim();
                // 替换为<import>com.gitee.qdbp.jdbc.sql.SqlTools</import>
                String importTag = generateSimpleTag(importTagName, className);
                return new ImportStatement(className, importTag);
            }
        } else if (trimed.startsWith("<" + supportsTagName + ">") && trimed.endsWith("</" + supportsTagName + ">")) {
            // <supports>mysql.8,mariadb.10.2.2,postgresql,db2,sqlserver,sqlite.3.8.3</supports>
            int tagLength = supportsTagName.length();
            String content = StringTools.removeLeftRight(trimed, tagLength + 2, tagLength + 3 + 0);
            return new SupportsStatement(content.trim());
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

    /** 判断除了import/supports/注释/空格/换行之外有没有实质的SQL语句 **/
    protected boolean existSqlFragment(String string) {
        if (string.trim().isEmpty()) {
            return false;
        }
        StringBuilder buffer = new StringBuilder(string);
        clearCommentContent(buffer, '<' + importTagName + '>', '<' + '/' + importTagName + '>', false);
        clearCommentContent(buffer, '<' + commentTagName + '>', '<' + '/' + commentTagName + '>', false);
        clearCommentContent(buffer, '<' + supportsTagName + '>', '<' + '/' + supportsTagName + '>', false);
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

    protected static class SupportsStatement extends LineItem {

        public SupportsStatement(String text) {
            super(text);
        }
    }

    protected static class LineContent {

        private final int line;
        private final String content;

        public LineContent(int line, String content) {
            this.line = line;
            this.content = content;
        }

        public int getLine() {
            return line;
        }

        public String getContent() {
            return content;
        }
    }

    protected static class SqlFragment extends LineContent {

        private final String id;
        /** 内部的版本支持声明语句 **/
        private final LineContent innerSupports;
        /** 公共的版本支持声明语句 **/
        private final LineContent commonSupports;

        public SqlFragment(String id, int line, LineContent innerSupports, LineContent commonSupports, String content) {
            super(line, content);
            this.id = id;
            this.innerSupports = innerSupports;
            this.commonSupports = commonSupports;
        }

        public String getId() {
            return id;
        }

        public LineContent getInnerSupports() {
            return innerSupports;
        }

        public LineContent getCommonSupports() {
            return commonSupports;
        }
    }

    private static class TmplData {

        private final String sqlId;
        private final String sqlAlias;
        private final String supports;
        private final IReader reader;

        public TmplData(String sqlId, String sqlAlias, String supports, IReader reader) {
            super();
            this.sqlId = sqlId;
            this.sqlAlias = sqlAlias;
            this.supports = supports;
            this.reader = reader;
        }

        public String getSqlId() {
            return sqlId;
        }

        public String getSqlAlias() {
            return sqlAlias;
        }

        public String getSupports() {
            return supports;
        }

        public IReader getReader() {
            return reader;
        }

    }

    private static class CacheBox implements IReaderCreator {

        // key=sqlKey=fileId:fragmentId(supports)
        private Map<String, TmplData> cache = new HashMap<>();

        // sqlKey=fileId:fragmentId(supports)
        public void register(String sqlKey, TmplData data) {
            cache.put(sqlKey, data);
        }

        public List<String> getSqlKeys() {
            return new ArrayList<>(cache.keySet());
        }

        public TmplData getSqlFragment(String sqlKey) {
            return cache.get(sqlKey);
        }

        @Override
        public IReader create(String sqlKey) throws ResourceNotFoundException {
            TmplData data = cache.get(sqlKey);
            if (data != null) {
                return data.getReader();
            } else {
                throw new ResourceNotFoundException(sqlKey + " not found");
            }
        }

        @Override
        public String getRelativePath(String sqlKey, String newKey) {
            return newKey;
        }

        @Override
        public Date getUpdateTime(String path) {
            return null;
        }
    }
}
