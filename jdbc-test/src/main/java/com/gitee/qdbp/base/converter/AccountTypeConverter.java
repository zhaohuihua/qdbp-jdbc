package com.gitee.qdbp.base.converter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import com.gitee.qdbp.jdbc.test.enums.AccountType;

/**
 * 账户类型转换
 *
 * @author zhaohuihua
 * @version 200202
 */
public class AccountTypeConverter<E extends Enum<E>> implements ConditionalGenericConverter {

    private Class<E> clazz;
    private List<AccountType> types;

    public AccountTypeConverter(Class<E> type) {
        this.clazz = type;
        E[] enums = type.getEnumConstants();
        if (enums == null) {
            throw new IllegalArgumentException(type.getSimpleName() + " does not represent an enum type.");
        }
        this.types = new ArrayList<>();
        for (E e : enums) {
            types.add((AccountType) e);
        }
    }

    private AccountType convert(Object type) {
        if (type instanceof String || type instanceof Number) {
            String target = type.toString();
            for (AccountType i : types) {
                if (i.name().equals(target) || String.valueOf(i.ordinal()).equals(target)) {
                    return i;
                }
            }
        }
        return null;
    }

    public Class<E> getEnumType() {
        return clazz;
    }

    private static final Set<ConvertiblePair> CONVERTIBLE_PAIRS;
    private static final TypeDescriptor ACCOUNT_TYPE = TypeDescriptor.valueOf(AccountType.class);
    private static final TypeDescriptor STRING_TYPE = TypeDescriptor.valueOf(String.class);
    private static final TypeDescriptor NUMBER_TYPE = TypeDescriptor.valueOf(Number.class);

    static {
        Set<ConvertiblePair> convertiblePairs = new HashSet<ConvertiblePair>(2);
        convertiblePairs.add(new ConvertiblePair(String.class, AccountType.class));
        convertiblePairs.add(new ConvertiblePair(Number.class, AccountType.class));
        CONVERTIBLE_PAIRS = Collections.unmodifiableSet(convertiblePairs);
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return CONVERTIBLE_PAIRS;
    }

    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        return targetType.isAssignableTo(ACCOUNT_TYPE)
                && (sourceType.isAssignableTo(STRING_TYPE) || sourceType.isAssignableTo(NUMBER_TYPE));
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        return convert(source);
    }
}
