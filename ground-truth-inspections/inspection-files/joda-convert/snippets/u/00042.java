@Test
public void test_convertToString_withType_inherit1() {
    assertEquals("CEILING", StringConvert.INSTANCE.convertToString(RoundingMode.class, RoundingMode.CEILING));
}

public String convertToString(Class<?> cls, Object object) {//method a
    if (object == null) {
        return null;
    }
    StringConverter<Object> conv = findConverterNoGenerics(cls);
    return conv.convertToString(object);
}

public StringConverter<Object> findConverterNoGenerics(final Class<?> cls) {
    return findTypedConverterNoGenerics(cls);//method b
}

public TypedStringConverter<Object> findTypedConverterNoGenerics(final Class<?> cls) {//method b
    TypedStringConverter<Object> conv = (TypedStringConverter<Object>) findConverterQuiet(cls);
    if (conv == null) {
        throw new IllegalStateException("No registered converter found: " + cls);
    }
    return conv;
}