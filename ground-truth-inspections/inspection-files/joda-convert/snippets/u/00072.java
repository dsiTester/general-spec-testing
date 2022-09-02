@Test(expected=IllegalStateException.class)
public void test_findConverter_Object() {
    StringConvert.INSTANCE.findConverter(Object.class);//method a
}

public <T> StringConverter<T> findConverter(final Class<T> cls) {//method a
    return findTypedConverter(cls);
}

public <T> TypedStringConverter<T> findTypedConverter(final Class<T> cls) {
    TypedStringConverter<T> conv = findConverterQuiet(cls);//method b
    if (conv == null) {
        throw new IllegalStateException("No registered converter found: " + cls);
    }
    return conv;
}

private <T> TypedStringConverter<T> findConverterQuiet(final Class<T> cls) {//method b
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
