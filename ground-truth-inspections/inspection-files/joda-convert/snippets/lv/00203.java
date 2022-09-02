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
