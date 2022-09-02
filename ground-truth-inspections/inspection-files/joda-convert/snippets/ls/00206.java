private void tryRegisterThreeTenOld() {//method b
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

@Test(expected = IllegalArgumentException.class)
public void test_registerFactory_null() {
    StringConvert test = new StringConvert();//calls both method-a and method-b
    test.registerFactory(null);
}
