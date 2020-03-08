package com.gitee.qdbp.jdbc.operator.base;

import java.util.Arrays;
import java.util.List;

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

    private boolean isAscii(String string) {
        for (int i = 0, z = string.length(); i < z; i++) {
            char c = string.charAt(i);
            if (!(c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z')) {
                return false;
            }
        }
        return true;
    }
}
