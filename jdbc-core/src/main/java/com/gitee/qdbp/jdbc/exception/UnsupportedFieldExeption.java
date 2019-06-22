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
public class UnsupportedFieldExeption extends RuntimeException {

    /** serialVersionUID **/
    private static final long serialVersionUID = 1L;

    private String beanType;
    private String message;
    private List<String> fields;

    public UnsupportedFieldExeption(String beanType, String message, List<String> fields) {
        super(message);
        this.beanType = beanType;
        this.message = message;
        this.fields = fields;
    }

    public UnsupportedFieldExeption(String message, List<String> fields) {
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
        String details = ConvertTools.joinToString(fields);
        return StringTools.concat(' ', beanType, VerifyTools.nvl(message, "unsupported fields"), details);
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
