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
        tryRegisterJava8Optionals();//method b
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
        this.factories.add(TypeStringConverterFactory.INSTANCE);
    }
}

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

private void tryRegisterJava8Optionals() {//method b
    try {
        loadType("java.util.OptionalInt");
        @SuppressWarnings("unchecked")
        Class<?> cls1 = (Class<TypedStringConverter<?>>) loadType("org.joda.convert.OptionalIntStringConverter");
        TypedStringConverter<?> conv1 = (TypedStringConverter<?>) cls1.getDeclaredConstructor().newInstance();
        registered.put(conv1.getEffectiveType(), conv1);

        @SuppressWarnings("unchecked")
        Class<?> cls2 = (Class<TypedStringConverter<?>>) loadType("org.joda.convert.OptionalLongStringConverter");
        TypedStringConverter<?> conv2 = (TypedStringConverter<?>) cls2.getDeclaredConstructor().newInstance();
        registered.put(conv2.getEffectiveType(), conv2);

        @SuppressWarnings("unchecked")
        Class<?> cls3 = (Class<TypedStringConverter<?>>) loadType("org.joda.convert.OptionalDoubleStringConverter");
        TypedStringConverter<?> conv3 = (TypedStringConverter<?>) cls3.getDeclaredConstructor().newInstance();
        registered.put(conv3.getEffectiveType(), conv3);

    } catch (Throwable ex) {
        if (LOG) {
            System.err.println("tryRegisterOptionals: " + ex);
        }
    }
}

@Test(expected = IllegalArgumentException.class)
public void test_registerFactory_null() {
    StringConvert test = new StringConvert();//calls both method-a and method-b
    test.registerFactory(null);
}
