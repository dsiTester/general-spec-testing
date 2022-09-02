/**
    * Tries to register the Guava converters class.
    */
private void tryRegisterGuava() {//method a
    try {
        // Guava is not a direct dependency, which is significant in the Java 9 module system
        // to access Guava this module must add a read edge to the module graph
        // but since this code is written for Java 6, we have to do this by reflection
        // yuck
        Class<?> moduleClass = Class.class.getMethod("getModule").getReturnType();
        Object convertModule = Class.class.getMethod("getModule").invoke(StringConvert.class);
        Object layer = convertModule.getClass().getMethod("getLayer").invoke(convertModule);
        if (layer != null) {
            Object optGuava = layer.getClass().getMethod("findModule", String.class).invoke(layer, "com.google.common");
            boolean found = (Boolean) optGuava.getClass().getMethod("isPresent").invoke(optGuava);
            if (found) {
                Object guavaModule = optGuava.getClass().getMethod("get").invoke(optGuava);
                moduleClass.getMethod("addReads", moduleClass).invoke(convertModule, guavaModule);
            }
        }

    } catch (Throwable ex) {
        if (LOG) {
            System.err.println("tryRegisterGuava1: " + ex);
        }
    }
    try {
        // can now check for Guava
        // if we have created a read edge, or if we are on the classpath, this will succeed
        loadType("com.google.common.reflect.TypeToken");
        @SuppressWarnings("unchecked")
        Class<?> cls = (Class<TypedStringConverter<?>>) loadType("org.joda.convert.TypeTokenStringConverter");
        TypedStringConverter<?> conv = (TypedStringConverter<?>) cls.getDeclaredConstructor().newInstance();
        registered.put(conv.getEffectiveType(), conv);

    } catch (Throwable ex) {
        if (LOG) {
            System.err.println("tryRegisterGuava2: " + ex);
        }
    }
}

private <T> TypedStringConverter<T> findAnyConverter(final Class<T> cls) {//method b
    // check factories
    for (StringConverterFactory factory : factories) {
        StringConverter<T> factoryConv = (StringConverter<T>) factory.findConverter(cls);
        if (factoryConv != null) {
            return TypedAdapter.adapt(cls, factoryConv);
        }
    }
    return null;
}

public boolean isConvertible(final Class<?> cls) {
    try {
        return cls != null && findConverterQuiet(cls) != null;//calls method b
    } catch (RuntimeException ex) {
        return false;
    }
}

private <T> TypedStringConverter<T> findConverterQuiet(final Class<T> cls) {
    if (cls == null) {
        throw new IllegalArgumentException("Class must not be null");
    }
    TypedStringConverter<T> conv = (TypedStringConverter<T>) registered.get(cls);
    if (conv == CACHED_NULL) {
        return null;
    }
    if (conv == null) {
        try {
            conv = findAnyConverter(cls);//only call method-b if we don't have the converter registered
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


@Test
public void test_isConvertible() {
    assertTrue(StringConvert.INSTANCE.isConvertible(Integer.class));
    assertTrue(StringConvert.INSTANCE.isConvertible(String.class));
    assertFalse(StringConvert.INSTANCE.isConvertible(Object.class));//calls method b
}

public StringConvert(boolean includeJdkConverters, StringConverterFactory... factories) {
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
        tryRegisterGuava();//method a
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
        this.factories.add(TypeStringConverterFactory.INSTANCE);//converts types
    }
}

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
