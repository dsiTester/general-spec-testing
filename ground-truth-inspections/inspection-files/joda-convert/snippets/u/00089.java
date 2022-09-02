public TypedStringConverter<Object> findTypedConverterNoGenerics(final Class<?> cls) {//method a
    TypedStringConverter<Object> conv = (TypedStringConverter<Object>) findConverterQuiet(cls);//method b
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

@Test(expected=IllegalStateException.class)
public void test_findConverterNoGenerics_Object() {
    StringConvert.INSTANCE.findConverterNoGenerics(Object.class);
}
