package com.gitee.qdbp.jdbc.plugins.impl;

import java.beans.PropertyDescriptor;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.NotWritablePropertyException;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.util.ClassUtils;
import com.gitee.qdbp.jdbc.plugins.MapToBeanConverter;
import com.gitee.qdbp.jdbc.support.ConversionServiceAware;

/**
 * 利用spring的ConvertionService进行Map到JavaBean的转换<br>
 * 逻辑参考自spring的BeanPropertyRowMapper<br>
 *
 * @author zhaohuihua
 * @version 200201
 */
public class SpringMapToBeanConverter
        implements MapToBeanConverter, ConditionalGenericConverter, ConversionServiceAware {

    private final Logger log = LoggerFactory.getLogger(getClass());
    /** 空值时是否调用调用方法 **/
    private boolean invokeSetterWhenNullValue = false;
    /** ConversionService for binding map values to bean properties */
    private ConversionService conversionService; // = DefaultConversionService.getSharedInstance();

    @Override
    public <T> T convert(Map<String, ?> map, Class<T> mappedClass) {
        Map<String, PropertyDescriptor> mappedFields = getPropertyDescriptorMaps(mappedClass);
        T mappedObject = BeanUtils.instantiateClass(mappedClass);
        BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(mappedObject);
        initBeanWrapper(bw);

        boolean typeMismatchForNullValueLogged = false;
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            String field = entry.getKey();
            Object value = entry.getValue();
            if (value == null && !invokeSetterWhenNullValue) {
                continue; // 空值时是否调用调用方法
            }
            PropertyDescriptor pd = mappedFields.get(field);
            if (pd == null) {
                continue;
            }
            try {
                bw.setPropertyValue(pd.getName(), value);
            } catch (NotWritablePropertyException e) {
                String m = "Unable to set property '%s.%s'";
                String className = mappedClass.getSimpleName();
                throw new DataRetrievalFailureException(String.format(m, className, pd.getName()), e);
            } catch (TypeMismatchException e) {
                if (value != null) {
                    throw e;
                }
                // 值为空导致无法判断调用哪个方法的, 只记录日志, 不抛出异常
                // 如 setValue(String), setValue(Integer);
                // 此时如果调用setValue(null)就会报TypeMismatchException
                if (!typeMismatchForNullValueLogged) {
                    typeMismatchForNullValueLogged = true; // 只记一次日志
                    String m = "Intercepted TypeMismatchException "
                            + "with null value when setting property '{}.{}' of type '{}'";
                    String className = mappedClass.getSimpleName();
                    String propertyType = ClassUtils.getQualifiedName(pd.getPropertyType());
                    log.debug(m, className, pd.getName(), propertyType, e);
                }
            }
        }

        return mappedObject;
    }

    protected Map<String, PropertyDescriptor> getPropertyDescriptorMaps(Class<?> mappedClass) {
        Map<String, PropertyDescriptor> mappedFields = new HashMap<String, PropertyDescriptor>();
        PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(mappedClass);
        for (PropertyDescriptor pd : pds) {
            if (pd.getWriteMethod() != null) {
                mappedFields.put(pd.getName(), pd);
            }
        }
        return mappedFields;
    }

    /**
     * Initialize the given BeanWrapper to be used for row mapping. To be called for each row.<br>
     * The default implementation applies the configured {@link ConversionService}, if any. <br>
     * Can be overridden in subclasses.
     * 
     * @param bw the BeanWrapper to initialize
     * @see #getConversionService()
     * @see BeanWrapper#setConversionService
     */
    protected void initBeanWrapper(BeanWrapper bw) {
        ConversionService cs = getConversionService();
        if (cs != null) {
            bw.setConversionService(cs);
        }
    }

    /** 空值时是否调用调用方法 **/
    public boolean isInvokeSetterWhenNullValue() {
        return invokeSetterWhenNullValue;
    }

    /** 空值时是否调用调用方法 **/
    public void setInvokeSetterWhenNullValue(boolean invokeSetterWhenNullValue) {
        this.invokeSetterWhenNullValue = invokeSetterWhenNullValue;
    }

    /**
     * Set a {@link ConversionService} for binding map values to bean properties, <br>
     * or {@code null} for none.
     * 
     * @param conversionService {@link ConversionService}
     * @see BeanWrapper#initBeanWrapper
     */
    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    /**
     * Return a {@link ConversionService} for binding map values to bean properties, <br>
     * or {@code null} if none.
     * 
     * @return {@link ConversionService}
     */
    public ConversionService getConversionService() {
        return this.conversionService;
    }

    private static final Set<ConvertiblePair> CONVERTIBLE_PAIRS;
    private static final TypeDescriptor MAP_TYPE = TypeDescriptor.valueOf(Map.class);

    static {
        Set<ConvertiblePair> convertiblePairs = new HashSet<ConvertiblePair>(1);
        convertiblePairs.add(new ConvertiblePair(Map.class, Object.class));
        CONVERTIBLE_PAIRS = Collections.unmodifiableSet(convertiblePairs);
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return CONVERTIBLE_PAIRS;
    }

    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        return sourceType.isAssignableTo(MAP_TYPE);
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        @SuppressWarnings("unchecked")
        Map<String, ?> map = (Map<String, ?>) source;
        return convert(map, targetType.getType());
    }
}