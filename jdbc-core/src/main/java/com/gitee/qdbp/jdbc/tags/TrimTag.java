package com.gitee.qdbp.jdbc.tags;

/**
 * 去掉第1个prefixOverrides, 去掉最后一个suffixOverrides<br>
 * 在前面增加prefix, 在后面增加suffix<br>
 *
 * @author zhaohuihua
 * @version 20200906
 * @since 3.2.0
 */
public class TrimTag extends TrimBase {

    @Override
    public void setPrefix(String prefix) {
        super.setPrefix(prefix);
    }

    @Override
    public void setSuffix(String suffix) {
        super.setSuffix(suffix);
    }

    @Override
    public void setPrefixOverrides(String prefixOverrides) {
        super.setPrefixOverrides(prefixOverrides);
    }

    @Override
    public void setSuffixOverrides(String suffixOverrides) {
        super.setSuffixOverrides(suffixOverrides);
    }
}
