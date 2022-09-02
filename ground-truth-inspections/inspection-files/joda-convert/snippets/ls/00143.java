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
