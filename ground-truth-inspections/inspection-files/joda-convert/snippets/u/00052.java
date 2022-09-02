public String convertToString(Object object) {//method a
    if (object == null) {
        return null;
    }
    Class<?> cls = object.getClass();
    StringConverter<Object> conv = findConverterNoGenerics(cls);//method b
    return conv.convertToString(object);
}

@Test
public void test_register_FunctionalInterfaces() {
    StringConvert test = new StringConvert();
    test.register(DistanceNoAnnotations.class, DISTANCE_TO_STRING_CONVERTER, DISTANCE_FROM_STRING_CONVERTER);
    DistanceNoAnnotations d = new DistanceNoAnnotations(25);
    assertEquals("Distance[25m]", test.convertToString(d));//fail this assertion
    assertEquals(d.amount, test.convertFromString(DistanceNoAnnotations.class, "25m").amount);
    StringConverter<DistanceNoAnnotations> conv = test.findConverter(DistanceNoAnnotations.class);
    assertEquals(true, conv.getClass().getName().contains("$"));
    assertSame(conv, test.findConverter(DistanceNoAnnotations.class));
}
