package com.gitee.qdbp.jdbc.support;

import org.springframework.core.convert.ConversionService;

public interface ConversionServiceAware {

    void setConversionService(ConversionService conversionService);
}
