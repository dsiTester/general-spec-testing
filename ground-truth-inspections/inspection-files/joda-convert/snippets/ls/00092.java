public void registerFactory(final StringConverterFactory factory) {
    if (factory == null) {
        throw new IllegalArgumentException("Factory must not be null");
    }
    if (this == INSTANCE) {
        throw new IllegalStateException("Global singleton cannot be extended");
    }
    factories.add(0, factory);
}

public <T> TypedStringConverter<T> findTypedConverter(final Class<T> cls) {
    TypedStringConverter<T> conv = findConverterQuiet(cls);//method b
    if (conv == null) {
        throw new IllegalStateException("No registered converter found: " + cls);
    }
    return conv;
}

private <T> TypedStringConverter<T> findConverterQuiet(final Class<T> cls) {//method b
    if (cls == null) {
        throw new IllegalArgumentException("Class must not be null");
    }
    TypedStringConverter<T> conv = (TypedStringConverter<T>) registered.get(cls);
    if (conv == CACHED_NULL) {
        return null;
    }
    if (conv == null) {
        try {
            conv = findAnyConverter(cls);
        } catch (RuntimeException ex) {
            registered.putIfAbsent(cls, CACHED_NULL);
            throw ex;
        }
        if (conv == null) {
            registered.putIfAbsent(cls, CACHED_NULL);
            return null;
        }
        registered.putIfAbsent(cls, conv);
    }
    return conv;
}

private <T> TypedStringConverter<T> findAnyConverter(final Class<T> cls) {
    // check factories
    for (StringConverterFactory factory : factories) {//relies on factories
        StringConverter<T> factoryConv = (StringConverter<T>) factory.findConverter(cls);
        if (factoryConv != null) {
            return TypedAdapter.adapt(cls, factoryConv);
        }
    }
    return null;
}

@Test
public void test_registerFactory() {
    StringConvert test = new StringConvert();
    test.registerFactory(new Factory1());//method a
    assertEquals(DistanceMethodMethod.class, test.findTypedConverter(DistanceMethodMethod.class).getEffectiveType());//calls method-b
}

static class Factory1 implements StringConverterFactory {
    @Override
    public StringConverter<?> findConverter(Class<?> cls) {
        if (cls == DistanceMethodMethod.class) {
            return MockDistanceStringConverter.INSTANCE;
        }
        return null;
    }

}

public StringConvert(boolean includeJdkConverters, StringConverterFactory... factories) {//the default constructor has factories by default
    if (factories == null) {
        throw new IllegalArgumentException("StringConverterFactory array must not be null");
    }
    for (int i = 0; i < factories.length; i++) {
        if (factories[i] == null) {
            throw new IllegalArgumentException("StringConverterFactory array must not contain a null element");
        }
    }
    if (includeJdkConverters) {
        for (JDKStringConverter conv : JDKStringConverter.values()) {
            registered.put(conv.getType(), conv);
        }
        registered.put(Boolean.TYPE, JDKStringConverter.BOOLEAN);
        registered.put(Byte.TYPE, JDKStringConverter.BYTE);
        registered.put(Short.TYPE, JDKStringConverter.SHORT);
        registered.put(Integer.TYPE, JDKStringConverter.INTEGER);
        registered.put(Long.TYPE, JDKStringConverter.LONG);
        registered.put(Float.TYPE, JDKStringConverter.FLOAT);
        registered.put(Double.TYPE, JDKStringConverter.DOUBLE);
        registered.put(Character.TYPE, JDKStringConverter.CHARACTER);
        tryRegisterGuava();
        tryRegisterJava8Optionals();
        tryRegisterTimeZone();
        tryRegisterJava8();
        tryRegisterThreeTenBackport();
        tryRegisterThreeTenOld();
    }
    if (factories.length > 0) {
        this.factories.addAll(Arrays.asList(factories));
    }
    this.factories.add(AnnotationStringConverterFactory.INSTANCE);
    if (includeJdkConverters) {
        this.factories.add(EnumStringConverterFactory.INSTANCE);
        this.factories.add(TypeStringConverterFactory.INSTANCE);//a generic case
    }
}

//from class TypeStringConverterFactory

@Override
public StringConverter<?> findConverter(Class<?> cls) {
    if (Type.class.isAssignableFrom(cls) && cls != Class.class) {
        return new TypeStringConverter(cls);
    }
    return null;
}

//-----------------------------------------------------------------------
@Override
public String toString() {
    return getClass().getSimpleName();
}

//-----------------------------------------------------------------------
static final class TypeStringConverter implements TypedStringConverter<Type> {

    private final Class<?> effectiveType;

    TypeStringConverter(Class<?> effectiveType) {
        this.effectiveType = effectiveType;
    }

    @Override
    public String convertToString(Type type) {
        try {
            return Types.toString(type);
        } catch (Exception ex) {
            return type.toString();
        }
    }

    @Override
    public Type convertFromString(Class<? extends Type> cls, String str) {
        return TypeUtils.parse(str);
    }

    @Override
    public Class<?> getEffectiveType() {
        return effectiveType;
    }
}
