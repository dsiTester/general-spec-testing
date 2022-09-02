/**
    * Tries to register ThreeTen backport classes.
    */
private void tryRegisterThreeTenBackport() {//method a
    try {
        tryRegister("org.threeten.bp.Instant", "parse");
        tryRegister("org.threeten.bp.Duration", "parse");
        tryRegister("org.threeten.bp.LocalDate", "parse");
        tryRegister("org.threeten.bp.LocalTime", "parse");
        tryRegister("org.threeten.bp.LocalDateTime", "parse");
        tryRegister("org.threeten.bp.OffsetTime", "parse");
        tryRegister("org.threeten.bp.OffsetDateTime", "parse");
        tryRegister("org.threeten.bp.ZonedDateTime", "parse");
        tryRegister("org.threeten.bp.Year", "parse");
        tryRegister("org.threeten.bp.YearMonth", "parse");
        tryRegister("org.threeten.bp.MonthDay", "parse");
        tryRegister("org.threeten.bp.Period", "parse");
        tryRegister("org.threeten.bp.ZoneOffset", "of");
        tryRegister("org.threeten.bp.ZoneId", "of");
        tryRegister("org.threeten.bp.ZoneRegion", "of");

    } catch (Throwable ex) {
        if (LOG) {
            System.err.println("tryRegisterThreeTenBackport: " + ex);
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
        tryRegisterTimeZone();
        tryRegisterJava8();
        tryRegisterThreeTenBackport();//method a
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
