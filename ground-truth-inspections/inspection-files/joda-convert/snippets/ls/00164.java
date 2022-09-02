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

public boolean isConvertible(final Class<?> cls) {
    try {
        return cls != null && findConverterQuiet(cls) != null;//calls method b
    } catch (RuntimeException ex) {
        return false;
    }
}

private <T> TypedStringConverter<T> findConverterQuiet(final Class<T> cls) {
    if (cls == null) {
        throw new IllegalArgumentException("Class must not be null");
    }
    TypedStringConverter<T> conv = (TypedStringConverter<T>) registered.get(cls);
    if (conv == CACHED_NULL) {
        return null;
    }
    if (conv == null) {//only if we don't find any registered converters
        try {
            conv = findAnyConverter(cls);//only call method-b if we don't have the converter registered
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
public void test_isConvertible() {
    assertTrue(StringConvert.INSTANCE.isConvertible(Integer.class));
    assertTrue(StringConvert.INSTANCE.isConvertible(String.class));
    assertFalse(StringConvert.INSTANCE.isConvertible(Object.class));//calls method b
}
