@Test(expected = ClassCastException.class)
public void test_convert_annotationSuperFactorySubViaSub2() {
    StringConvert test = new StringConvert();
    test.convertFromString(SuperFactorySub.class, "25m");//method a
}

public <T> T convertFromString(Class<T> cls, String str) {//method a
    if (str == null) {
        return null;
    }
    StringConverter<T> conv = findConverter(cls);
    return conv.convertFromString(cls, str);
}

public <T> StringConverter<T> findConverter(final Class<T> cls) {
    return findTypedConverter(cls);//method b
}

public <T> TypedStringConverter<T> findTypedConverter(final Class<T> cls) {//method b
    TypedStringConverter<T> conv = findConverterQuiet(cls);
    if (conv == null) {
        throw new IllegalStateException("No registered converter found: " + cls);
    }
    return conv;
}
