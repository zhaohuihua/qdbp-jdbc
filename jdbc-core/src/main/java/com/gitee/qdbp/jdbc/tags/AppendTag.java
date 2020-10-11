package com.gitee.qdbp.jdbc.tags;

/**
 * 如果内容不为空, 则输出prefix+内容+suffix<br>
 * &lt;append prefix="AND"&gt;#{whereCondition}&lt;/append&gt;
 *
 * @author zhaohuihua
 * @version 20200906
 */
public class AppendTag extends TrimBase {

    @Override
    public String getPrefix() {
        return super.getPrefix();
    }

    @Override
    public void setPrefix(String prefix) {
        super.setPrefix(prefix);
    }

    @Override
    public String getSuffix() {
        return super.getSuffix();
    }

    @Override
    public void setSuffix(String suffix) {
        super.setSuffix(suffix);
    }

}
