

public String convertToString(Object object) {
    if (object == null) {
        return null;
    }
    Class<?> cls = object.getClass();
    StringConverter<Object> conv = findConverterNoGenerics(cls);//calls method a
    return conv.convertToString(object);//NPE
}

public StringConverter<Object> findConverterNoGenerics(final Class<?> cls) {
    return findTypedConverterNoGenerics(cls);//method a, replaced with null
}

public <T> T convertFromString(Class<T> cls, String str) {//method b
    if (str == null) {
        return null;
    }
    StringConverter<T> conv = findConverter(cls);
    return conv.convertFromString(cls, str);
}

@Test
public void test_register_FunctionalInterfaces() {
    StringConvert test = new StringConvert();
    test.register(DistanceNoAnnotations.class, DISTANCE_TO_STRING_CONVERTER, DISTANCE_FROM_STRING_CONVERTER);
    DistanceNoAnnotations d = new DistanceNoAnnotations(25);
    assertEquals("Distance[25m]", test.convertToString(d));//calls method a
    assertEquals(d.amount, test.convertFromString(DistanceNoAnnotations.class, "25m").amount);//method b
    StringConverter<DistanceNoAnnotations> conv = test.findConverter(DistanceNoAnnotations.class);
    assertEquals(true, conv.getClass().getName().contains("$"));
    assertSame(conv, test.findConverter(DistanceNoAnnotations.class));
}
