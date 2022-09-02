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

public boolean isConvertible(final Class<?> cls) {//method b
    try {
        return cls != null && findConverterQuiet(cls) != null;
    } catch (RuntimeException ex) {
        return false;
    }
}

/**
 * Mock enum used to test renames.
 */
public enum Status {

    VALID,
    INVALID;

    public static final boolean STRING_CONVERTIBLE = StringConvert.INSTANCE.isConvertible(String.class);//method b
}

private <T> TypedStringConverter<T> findConverterQuiet(final Class<T> cls) {
    if (cls == null) {
        throw new IllegalArgumentException("Class must not be null");
    }
    TypedStringConverter<T> conv = (TypedStringConverter<T>) registered.get(cls);//relies on registered converters
    if (conv == CACHED_NULL) {
        return null;
    }
    if (conv == null) {
        try {
            conv = findAnyConverter(cls);
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
public void test_Class() {
    JDKStringConverter test = JDKStringConverter.CLASS;
    doTest(test, Class.class, Locale.class, "java.util.Locale");
    doTest(test, Class.class, FromString.class, "org.joda.convert.FromString");
}

public void doTest(JDKStringConverter test, Class<?> cls, Object obj, String str, Object objFromStr) {
    assertEquals(cls, test.getType());
    assertEquals(str, test.convertToString(obj));
    assertEquals(objFromStr, test.convertFromString(cls, str));
}
