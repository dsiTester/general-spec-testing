public String convertToString(Object object) {//method a
    if (object == null) {
        return null;
    }
    Class<?> cls = object.getClass();
    StringConverter<Object> conv = findConverterNoGenerics(cls);
    return conv.convertToString(object);
}

public <T> T convertFromString(Class<T> cls, String str) {
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

@Test
public void test_shortArray() {
    doTest(new short[0], "");
    doTest(new short[] {5}, "5");
    doTest(new short[] {-1234, 5678}, "-1234,5678");
}

private void doTest(short[] array, String str) {
    StringConvert test = new StringConvert(true, NumericArrayStringConverterFactory.INSTANCE);
    assertEquals(str, test.convertToString(array));
    assertEquals(str, test.convertToString(short[].class, array));
    assertTrue(Arrays.equals(array, test.convertFromString(short[].class, str)));
}
