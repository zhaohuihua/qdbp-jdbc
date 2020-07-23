package com.gitee.qdbp.jdbc.operator.base;

import java.util.Arrays;
import java.util.List;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 基本运算符Abstract类
 *
 * @author zhaohuihua
 * @version 20200126
 */
public abstract class DbAbstractOperator {

    private String name;
    private String type;
    private List<String> aliases;

    /**
     * 构造函数
     * 
     * @param type 运算符类型(一般是数据库可识别的运算符)
     * @param aliases 运算符别名(书写时方便识别的运算符别名)<br>
     *            将会从别名中查找无空格的英文名称,将用于从request中自动构造where和update
     */
    public DbAbstractOperator(String type, String... aliases) {
        this.name = findName(type, aliases);
        this.type = type;
        if (aliases != null && aliases.length > 0) {
            this.aliases = Arrays.asList(aliases);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
    }

    public boolean matchers(String operator) {
        String otherOperator = convertKey(operator);
        if (VerifyTools.equals(otherOperator, convertKey(this.getType()))) {
            return true;
        }
        if (VerifyTools.equals(otherOperator, convertKey(this.getName()))) {
            return true;
        }
        List<String> aliases = this.getAliases();
        if (aliases != null) {
            for (String alias : aliases) {
                if (VerifyTools.equals(otherOperator, convertKey(alias))) {
                    return true;
                }
            }
        }
        return false;
    }

    /** 查找第一个无空格英文名称 **/
    private String findName(String type, String... aliases) {
        if (isAscii(type)) {
            return type;
        }
        if (aliases != null && aliases.length > 0) {
            for (String alias : aliases) {
                if (isAscii(alias)) {
                    return alias;
                }
            }
        }
        // 未找到无空格英文名称
        throw new IllegalArgumentException("EnglishNameWithoutSpace not found.");
    }

    private static boolean isAscii(String string) {
        for (int i = 0, z = string.length(); i < z; i++) {
            char c = string.charAt(i);
            if (!(c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z')) {
                return false;
            }
        }
        return true;
    }

    private static String convertKey(String operatorType) {
        return operatorType == null ? null : operatorType.toUpperCase();
    }
}
