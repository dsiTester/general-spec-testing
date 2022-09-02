private void tryRegisterThreeTenOld() {//method a
    try {
        tryRegister("javax.time.Instant", "parse");
        tryRegister("javax.time.Duration", "parse");
        tryRegister("javax.time.calendar.LocalDate", "parse");
        tryRegister("javax.time.calendar.LocalTime", "parse");
        tryRegister("javax.time.calendar.LocalDateTime", "parse");
        tryRegister("javax.time.calendar.OffsetDate", "parse");
        tryRegister("javax.time.calendar.OffsetTime", "parse");
        tryRegister("javax.time.calendar.OffsetDateTime", "parse");
        tryRegister("javax.time.calendar.ZonedDateTime", "parse");
        tryRegister("javax.time.calendar.Year", "parse");
        tryRegister("javax.time.calendar.YearMonth", "parse");
        tryRegister("javax.time.calendar.MonthDay", "parse");
        tryRegister("javax.time.calendar.Period", "parse");
        tryRegister("javax.time.calendar.ZoneOffset", "of");
        tryRegister("javax.time.calendar.ZoneId", "of");
        tryRegister("javax.time.calendar.TimeZone", "of");

    } catch (Throwable ex) {
        if (LOG) {
            System.err.println("tryRegisterThreeTenOld: " + ex);
        }
    }
}

/**
    * Tries to register Java 8 classes.
    */
private void tryRegisterJava8() {//method a
    try {
        tryRegister("java.time.Instant", "parse");
        tryRegister("java.time.Duration", "parse");
        tryRegister("java.time.LocalDate", "parse");
        tryRegister("java.time.LocalTime", "parse");
        tryRegister("java.time.LocalDateTime", "parse");
        tryRegister("java.time.OffsetTime", "parse");
        tryRegister("java.time.OffsetDateTime", "parse");
        tryRegister("java.time.ZonedDateTime", "parse");
        tryRegister("java.time.Year", "parse");
        tryRegister("java.time.YearMonth", "parse");
        tryRegister("java.time.MonthDay", "parse");
        tryRegister("java.time.Period", "parse");
        tryRegister("java.time.ZoneOffset", "of");
        tryRegister("java.time.ZoneId", "of");
        tryRegister("java.time.ZoneRegion", "of");

    } catch (Throwable ex) {
        if (LOG) {
            System.err.println("tryRegisterJava8: " + ex);
        }
    }
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
        tryRegisterJava8();//method a
        tryRegisterThreeTenBackport();
        tryRegisterThreeTenOld();//method b
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

@Test(expected = IllegalArgumentException.class)
public void test_registerFactory_null() {
    StringConvert test = new StringConvert();//calls both method-a and method-b
    test.registerFactory(null);
}
