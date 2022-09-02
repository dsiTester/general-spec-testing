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

public <T> void register(final Class<T> cls, final ToStringConverter<T> toString, final FromStringConverter<T> fromString) {//method b
    if (fromString == null || toString == null) {
        throw new IllegalArgumentException("Converters must not be null");
    }
    register(cls, new TypedStringConverter<T>() {
        @Override
        public String convertToString(T object) {
            return toString.convertToString(object);
        }
        @Override
        public T convertFromString(Class<? extends T> cls, String str) {
            return fromString.convertFromString(cls, str);
        }
        @Override
        public Class<?> getEffectiveType() {
            return cls;
        }
    });
}

@Test(expected = IllegalStateException.class)//we expect to fail
public void test_registerFactory_cannotChangeSingleton() {
    //the creation of this instance calls method-a
    StringConvert.INSTANCE.register(//method b
        DistanceNoAnnotations.class, DISTANCE_TO_STRING_CONVERTER, DISTANCE_FROM_STRING_CONVERTER);
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
