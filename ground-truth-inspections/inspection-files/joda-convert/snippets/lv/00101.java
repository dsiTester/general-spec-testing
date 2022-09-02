public <T> void register(final Class<T> cls, final ToStringConverter<T> toString, final FromStringConverter<T> fromString) {//method a
    if (fromString == null || toString == null) {
        throw new IllegalArgumentException("Converters must not be null");
    }
    register(cls, new TypedStringConverter<T>() {
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

public String convertToString(Class<?> cls, Object object) {
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
        throw new IllegalStateException("No registered converter found: " + cls);//throw this illegal state exception
    }
    return conv;
}

private <T> TypedStringConverter<T> findConverterQuiet(final Class<T> cls) {
    if (cls == null) {
        throw new IllegalArgumentException("Class must not be null");
    }
    TypedStringConverter<T> conv = (TypedStringConverter<T>) registered.get(cls);//relies on registered
    if (conv == CACHED_NULL) {
        return null;
    }
    if (conv == null) {
        try {
            conv = findAnyConverter(cls);
        } catch (RuntimeException ex) {
            registered.putIfAbsent(cls, CACHED_NULL);
            throw ex;
        }
        if (conv == null) {
            registered.putIfAbsent(cls, CACHED_NULL);
            return null;
        }
        registered.putIfAbsent(cls, conv);
    }
    return conv;
}


@Test
public void test_register_FunctionalInterfaces() {
    StringConvert test = new StringConvert();
    test.register(DistanceNoAnnotations.class, DISTANCE_TO_STRING_CONVERTER, DISTANCE_FROM_STRING_CONVERTER);//method a
    DistanceNoAnnotations d = new DistanceNoAnnotations(25);
    assertEquals("Distance[25m]", test.convertToString(d));//calls method b
    assertEquals(d.amount, test.convertFromString(DistanceNoAnnotations.class, "25m").amount);
    StringConverter<DistanceNoAnnotations> conv = test.findConverter(DistanceNoAnnotations.class);
    assertEquals(true, conv.getClass().getName().contains("$"));
    assertSame(conv, test.findConverter(DistanceNoAnnotations.class));
}
