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

public <T> void registerMethodConstructor(final Class<T> cls, String toStringMethodName) {//method b
    if (cls == null) {
        throw new IllegalArgumentException("Class must not be null");
    }
    if (toStringMethodName == null) {
        throw new IllegalArgumentException("Method name must not be null");
    }
    if (this == INSTANCE) {
        throw new IllegalStateException("Global singleton cannot be extended");
    }
    Method toString = findToStringMethod(cls, toStringMethodName);
    Constructor<T> fromString = findFromStringConstructorByType(cls);
    MethodConstructorStringConverter<T> converter = new MethodConstructorStringConverter<T>(cls, toString, fromString);
    registered.putIfAbsent(cls, converter);
}


@Test(expected = IllegalStateException.class)
public void ttest_registerMethodConstructor_cannotChangeSingleton() {
    StringConvert.INSTANCE.registerMethodConstructor(DistanceNoAnnotationsCharSequence.class, "toString");//method b
}

