@Test(expected=IllegalStateException.class)
public void test_findConverter_Object() {
    StringConvert.INSTANCE.findConverter(Object.class);//method a
}

public <T> StringConverter<T> findConverter(final Class<T> cls) {//method a
    return findTypedConverter(cls);//method b
}

public <T> TypedStringConverter<T> findTypedConverter(final Class<T> cls) {//method b
    TypedStringConverter<T> conv = findConverterQuiet(cls);
    if (conv == null) {
        throw new IllegalStateException("No registered converter found: " + cls);
    }
    return conv;
}
