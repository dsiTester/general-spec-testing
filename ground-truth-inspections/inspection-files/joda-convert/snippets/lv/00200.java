/**
    * Tries to register the subclasses of TimeZone.
    * Try various things, doesn't matter if the map entry gets overwritten.
    */
private void tryRegisterTimeZone() {//method a
    try {
        registered.put(SimpleTimeZone.class, JDKStringConverter.TIME_ZONE);

    } catch (Throwable ex) {
        if (LOG) {
            System.err.println("tryRegisterTimeZone1: " + ex);
        }
    }
    try {
        TimeZone zone = TimeZone.getDefault();
        registered.put(zone.getClass(), JDKStringConverter.TIME_ZONE);

    } catch (Throwable ex) {
        if (LOG) {
            System.err.println("tryRegisterTimeZone2: " + ex);
        }
    }
    try {
        TimeZone zone = TimeZone.getTimeZone("Europe/London");
        registered.put(zone.getClass(), JDKStringConverter.TIME_ZONE);

    } catch (Throwable ex) {
        if (LOG) {
            System.err.println("tryRegisterTimeZone3: " + ex);
        }
    }
}

public void registerFactory(final StringConverterFactory factory) {//method b
    if (factory == null) {
        throw new IllegalArgumentException("Factory must not be null");
    }
    if (this == INSTANCE) {
        throw new IllegalStateException("Global singleton cannot be extended");
    }
    factories.add(0, factory);
}

@Test(expected = IllegalStateException.class)//we expect to fail
public void test_registerFactory_cannotChangeSingleton() {
    //the creation of this instance calls method-a
    StringConvert.INSTANCE.registerFactory(new Factory1());//method b
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
        tryRegisterJava8Optionals();
        tryRegisterTimeZone();//method a
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
