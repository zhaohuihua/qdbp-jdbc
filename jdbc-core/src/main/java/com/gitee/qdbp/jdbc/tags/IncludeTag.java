package com.gitee.qdbp.jdbc.tags;

import java.io.IOException;
import com.gitee.qdbp.staticize.annotation.DynamicAttrSupport;
import com.gitee.qdbp.staticize.exception.TagException;
import com.gitee.qdbp.staticize.tags.base.BaseTag;
import com.gitee.qdbp.staticize.tags.base.NextStep;

/**
 * Include标签, 支持动态参数
 * 
 * @author zhaohuihua
 * @version 140722
 */
@DynamicAttrSupport("param")
public class IncludeTag extends BaseTag {

    public IncludeTag() {
        super();
    }

    /**
     * 获取src字段名
     *
     * @return src字段名
     */
    public String getSrcField() {
        return "src";
    }

    /** {@inheritDoc} **/
    @Override
    public NextStep doHandle() throws TagException, IOException {
        return NextStep.EVAL_BODY;
    }
}
