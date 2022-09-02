public String convertToString(Object object) {
    if (object == null) {
        return null;
    }
    Class<?> cls = object.getClass();
    StringConverter<Object> conv = findConverterNoGenerics(cls);
    return conv.convertToString(object);
}

public StringConverter<Object> findConverterNoGenerics(final Class<?> cls) {
    return findTypedConverterNoGenerics(cls);
}

public TypedStringConverter<Object> findTypedConverterNoGenerics(final Class<?> cls) {
    TypedStringConverter<Object> conv = (TypedStringConverter<Object>) findConverterQuiet(cls);//method-b
    if (conv == null) {
        throw new IllegalStateException("No registered converter found: " + cls);
    }
    return conv;
}

@Test
public void test_convert_annotationSuperFactorySubViaSub1() {
    StringConvert test = new StringConvert();
    SuperFactorySub d = new SuperFactorySub(25);
    assertEquals("25m", test.convertToString(d));
}
