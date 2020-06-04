package com.gitee.qdbp.jdbc.exception;

import java.util.List;
import com.gitee.qdbp.tools.utils.ConvertTools;
import com.gitee.qdbp.tools.utils.StringTools;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 不支持字段的异常类
 *
 * @author zhaohuihua
 * @version 190601
 */
public class UnsupportedFieldException extends RuntimeException {

    /** serialVersionUID **/
    private static final long serialVersionUID = 1L;

    private String beanType;
    private String message;
    private List<String> fields;

    public UnsupportedFieldException(String beanType, String message, List<String> fields) {
        super(message);
        this.beanType = beanType;
        this.message = message;
        this.fields = fields;
    }

    public UnsupportedFieldException(String message, List<String> fields) {
        this(null, message, fields);
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getFields() {
        return this.fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    @Override
    public String getMessage() {
        String details = fields == null ? null : '[' + ConvertTools.joinToString(fields) + ']';
        String desc = VerifyTools.nvl(message, "unsupported fields");
        return StringTools.concat(' ', beanType, desc, details);
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
