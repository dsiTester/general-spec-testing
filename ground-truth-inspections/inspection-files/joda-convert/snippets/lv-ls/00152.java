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

public <T> T convertFromString(Class<T> cls, String str) {
    if (str == null) {
        return null;
    }
    StringConverter<T> conv = findConverter(cls);
    return conv.convertFromString(cls, str);
}

public <T> StringConverter<T> findConverter(final Class<T> cls) {
    return findTypedConverter(cls);//method b
}

public <T> TypedStringConverter<T> findTypedConverter(final Class<T> cls) {///method b
    TypedStringConverter<T> conv = findConverterQuiet(cls);//relies on registered converter
    if (conv == null) {
        throw new IllegalStateException("No registered converter found: " + cls);
    }
    return conv;
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

@Test(expected = IllegalArgumentException.class)//we expect to not find a converter
public void test_convertFromString_nullClass() {//lv
    assertNull(StringConvert.INSTANCE.convertFromString(null, "6"));
}

@Test
public void test_convertFromString_primitiveBoolean() {//ls
    assertEquals(Boolean.TRUE, StringConvert.INSTANCE.convertFromString(Boolean.TYPE, "true"));//a boolean can be converted by other converters
}
