@Test
public void test_shortArray() {
    doTest(new short[0], "");
    doTest(new short[] {5}, "5");
    doTest(new short[] {-1234, 5678}, "-1234,5678");
}

private void doTest(float[] array, String str) {
    StringConvert test = new StringConvert(true, NumericArrayStringConverterFactory.INSTANCE);
    assertEquals(str, test.convertToString(array));//correct replacement
    assertEquals(str, test.convertToString(float[].class, array));
    assertTrue(Arrays.equals(array, test.convertFromString(float[].class, str)));
}

public <T> T convertFromString(Class<T> cls, String str) {//method b
    if (str == null) {
        return null;
    }
    StringConverter<T> conv = findConverter(cls);
    return conv.convertFromString(cls, str);
}

public String convertToString(Class<?> cls, Object object) {//method a
    if (object == null) {
        return null;
    }
    StringConverter<Object> conv = findConverterNoGenerics(cls);
    return conv.convertToString(object);
}