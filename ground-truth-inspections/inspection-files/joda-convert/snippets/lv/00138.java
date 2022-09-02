private void tryRegisterJava8Optionals() {//method a
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

public <T> void register(final Class<T> cls, StringConverter<T> converter) {//method b
    if (cls == null) {
        throw new IllegalArgumentException("Class must not be null");
    }
    if (converter == null) {
        throw new IllegalArgumentException("StringConverter must not be null");
    }
    if (this == INSTANCE) {
        throw new IllegalStateException("Global singleton cannot be extended");
    }
    registered.put(cls, TypedAdapter.adapt(cls, converter));
}

@Test(expected=IllegalStateException.class)//we expect to fail
public void test_register_notOnShared() {
    //the creation of this instance calls method-a
    StringConvert.INSTANCE.register(Integer.class, MockIntegerStringConverter.INSTANCE);//method b
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
        tryRegisterGuava();
        tryRegisterJava8Optionals();//method a
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
