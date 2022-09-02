public String convertToString(Class<?> cls, Object object) {//method b
    if (object == null) {
        return null;
    }
    StringConverter<Object> conv = findConverterNoGenerics(cls);
    return conv.convertToString(object);
}

//convertToString eventually call this method
private <T> TypedStringConverter<T> findConverterQuiet(final Class<T> cls) {
    if (cls == null) {
        throw new IllegalArgumentException("Class must not be null");
    }
    TypedStringConverter<T> conv = (TypedStringConverter<T>) registered.get(cls);
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

public <T> void registerMethodConstructor(final Class<T> cls, String toStringMethodName) {
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
    registered.putIfAbsent(cls, converter);//registers a converter for a given type
}

@Test
public void test_registerMethodConstructorCharSequence() {
    StringConvert test = new StringConvert();
    test.registerMethodConstructor(DistanceNoAnnotationsCharSequence.class, "toString");//method a
    DistanceNoAnnotationsCharSequence d = new DistanceNoAnnotationsCharSequence(25);
    assertEquals("Distance[25m]", test.convertToString(d));//method b
    assertEquals(d.amount, test.convertFromString(DistanceNoAnnotationsCharSequence.class, "25m").amount);
    StringConverter<DistanceNoAnnotationsCharSequence> conv = test.findConverter(DistanceNoAnnotationsCharSequence.class);
    assertEquals(true, conv instanceof MethodConstructorStringConverter<?>);
    assertSame(conv, test.findConverter(DistanceNoAnnotationsCharSequence.class));
}
