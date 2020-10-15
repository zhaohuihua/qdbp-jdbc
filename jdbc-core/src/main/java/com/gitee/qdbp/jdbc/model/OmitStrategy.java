package com.gitee.qdbp.jdbc.model;

import java.io.Serializable;
import com.gitee.qdbp.tools.utils.ConvertTools;
import com.gitee.qdbp.tools.utils.StringTools;

/**
 * 省略策略
 *
 * @author zhaohuihua
 * @version 20201005
 * @since 3.2.0
 */
public class OmitStrategy implements Serializable {

    /** serialVersionUID **/
    private static final long serialVersionUID = 1L;

    /** 至少超过多少项时启用省略 **/
    private final int minSize;
    /** 省略时首末各保留多少项 **/
    private final int keepSize;

    public OmitStrategy(int minSize, int keepSize) {
        this.minSize = minSize;
        this.keepSize = keepSize;
    }

    /** 至少超过多少项时启用省略 **/
    public int getMinSize() {
        return minSize;
    }

    /** 省略时首末各保留多少项 **/
    public int getKeepSize() {
        return keepSize;
    }

    public static OmitStrategy of(String string) {
        if (string == null || string.trim().length() == 0) {
            return new OmitStrategy(0, 0);
        }
        try {
            int minSize;
            int keepSize;
            if (string.indexOf(':') < 0) {
                minSize = ConvertTools.toInteger(string.trim());
                keepSize = 3;
            } else {
                String[] array = StringTools.split(string.trim(), ':');
                if (array.length != 2) {
                    throw new IllegalArgumentException("OmitSizeFormatError: " + string);
                }
                minSize = ConvertTools.toInteger(array[0]);
                keepSize = ConvertTools.toInteger(array[1]);
            }
            if (minSize == 0) {
                return new OmitStrategy(0, 0);
            }
            if (minSize < 0 || keepSize <= 0) {
                throw new IllegalArgumentException("OmitSizeFormatError: " + string);
            }
            if (minSize < keepSize * 2 + 2) {
                minSize = keepSize * 2 + 2;
            }
            return new OmitStrategy(minSize, keepSize);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("OmitSizeFormatError: " + string);
        }
    }
}
