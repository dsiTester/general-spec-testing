@Test
public void test_convertToString_withType_inherit1() {
    assertEquals("CEILING", StringConvert.INSTANCE.convertToString(RoundingMode.class, RoundingMode.CEILING));
}

public String convertToString(Class<?> cls, Object object) {//method a
    if (object == null) {
        return null;
    }
    StringConverter<Object> conv = findConverterNoGenerics(cls);//method b
    return conv.convertToString(object);
}

public StringConverter<Object> findConverterNoGenerics(final Class<?> cls) {//method b
    return findTypedConverterNoGenerics(cls);
}
