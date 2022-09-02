public <T> void register(final Class<T> cls, final ToStringConverter<T> toString, final FromStringConverter<T> fromString) {//method a
    if (fromString == null || toString == null) {
        throw new IllegalArgumentException("Converters must not be null");
    }
    register(cls, new TypedStringConverter<T>() {//method b
        @Override
        public String convertToString(T object) {
            return toString.convertToString(object);
        }
        @Override
        public T convertFromString(Class<? extends T> cls, String str) {
            return fromString.convertFromString(cls, str);
        }
        @Override
        public Class<?> getEffectiveType() {
            return cls;
        }
    });
}

@Test(expected=IllegalArgumentException.class)
public void test_register_FunctionalInterfaces_nullClass() {
    StringConvert test = new StringConvert();
    test.register(null, DISTANCE_TO_STRING_CONVERTER, DISTANCE_FROM_STRING_CONVERTER);//method a
}
