public <T> void register(final Class<T> cls, final ToStringConverter<T> toString, final FromStringConverter<T> fromString) {
    if (fromString == null || toString == null) {
        throw new IllegalArgumentException("Converters must not be null");
    }
    register(cls, new TypedStringConverter<T>() {
        @Override
        public String convertToString(T object) {//method a
            return toString.convertToString(object);
        }
        @Override
        public T convertFromString(Class<? extends T> cls, String str) {//method b
            return fromString.convertFromString(cls, str);
        }
        @Override
        public Class<?> getEffectiveType() {
            return cls;
        }
    });
}

@Test
public void test_register_FunctionalInterfaces() {//u
    StringConvert test = new StringConvert();
    test.register(DistanceNoAnnotations.class, DISTANCE_TO_STRING_CONVERTER, DISTANCE_FROM_STRING_CONVERTER);
    DistanceNoAnnotations d = new DistanceNoAnnotations(25);
    assertEquals("Distance[25m]", test.convertToString(d));// call to a; we fail here
    assertEquals(d.amount, test.convertFromString(DistanceNoAnnotations.class, "25m").amount); // call to b
    StringConverter<DistanceNoAnnotations> conv = test.findConverter(DistanceNoAnnotations.class);
    assertEquals(true, conv.getClass().getName().contains("$"));
    assertSame(conv, test.findConverter(DistanceNoAnnotations.class));
}

public void test_doubleArray() {//ls
    doTest(new double[0], "");
    doTest(new double[] {5d}, "5.0");
    doTest(new double[] {5.123456789d}, "5.123456789");
    doTest(new double[] {-1234d, 5678d}, "-1234.0,5678.0");
    doTest(new double[] {Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, -0.0d, +0.0d, 0d}, "NaN,-Infinity,Infinity,-0.0,0.0,0.0");
    doTest(new double[] {0.0000006d, 6000000000d}, "6.0E-7,6.0E9");
}

private void doTest(double[] array, String str) {
    StringConvert test = new StringConvert(true, NumericArrayStringConverterFactory.INSTANCE);
    assertEquals(str, test.convertToString(array));//correct replacement
    assertEquals(str, test.convertToString(double[].class, array));
    assertTrue(Arrays.equals(array, test.convertFromString(double[].class, str)));
}
