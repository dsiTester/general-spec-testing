public String convertToString(Object object) {//method a
    if (object == null) {
        return null;
    }
    Class<?> cls = object.getClass();
    StringConverter<Object> conv = findConverterNoGenerics(cls);
    return conv.convertToString(object);
}

public String convertToString(Class<?> cls, Object object) {//method b
    if (object == null) {
        return null;
    }
    StringConverter<Object> conv = findConverterNoGenerics(cls);
    return conv.convertToString(object);
}

@Test
public void test_shortArray() {
    doTest(new short[0], "");
    doTest(new short[] {5}, "5");
    doTest(new short[] {-1234, 5678}, "-1234,5678");
}

private void doTest(short[] array, String str) {
    StringConvert test = new StringConvert(true, NumericArrayStringConverterFactory.INSTANCE);
    assertEquals(str, test.convertToString(array));//method a
    assertEquals(str, test.convertToString(short[].class, array));//method b
    assertTrue(Arrays.equals(array, test.convertFromString(short[].class, str)));
}
