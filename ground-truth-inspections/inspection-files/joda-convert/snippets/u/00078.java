@Test
public void test_register_FunctionalInterfaces() {
    StringConvert test = new StringConvert();
    test.register(DistanceNoAnnotations.class, DISTANCE_TO_STRING_CONVERTER, DISTANCE_FROM_STRING_CONVERTER);
    DistanceNoAnnotations d = new DistanceNoAnnotations(25);
    assertEquals("Distance[25m]", test.convertToString(d));//this leads to method-a
    assertEquals(d.amount, test.convertFromString(DistanceNoAnnotations.class, "25m").amount);
    StringConverter<DistanceNoAnnotations> conv = test.findConverter(DistanceNoAnnotations.class);
    assertEquals(true, conv.getClass().getName().contains("$"));
    assertSame(conv, test.findConverter(DistanceNoAnnotations.class));
}

public String convertToString(Object object) {
    if (object == null) {
        return null;
    }
    Class<?> cls = object.getClass();
    StringConverter<Object> conv = findConverterNoGenerics(cls);//method a
    return conv.convertToString(object);//NPE
}


public StringConverter<Object> findConverterNoGenerics(final Class<?> cls) {//method a
    return findTypedConverterNoGenerics(cls);//method b
}

public TypedStringConverter<Object> findTypedConverterNoGenerics(final Class<?> cls) {//method b
    TypedStringConverter<Object> conv = (TypedStringConverter<Object>) findConverterQuiet(cls);
    if (conv == null) {
        throw new IllegalStateException("No registered converter found: " + cls);
    }
    return conv;
}
