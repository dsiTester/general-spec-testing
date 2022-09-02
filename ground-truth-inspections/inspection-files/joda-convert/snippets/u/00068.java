private <T> TypedStringConverter<T> findAnyConverter(final Class<T> cls) {//method b
    // check factories
    for (StringConverterFactory factory : factories) {
        StringConverter<T> factoryConv = (StringConverter<T>) factory.findConverter(cls);//method a
        if (factoryConv != null) {
            return TypedAdapter.adapt(cls, factoryConv);
        }
    }
    return null;
}

public String convertToString(Object object) {
    if (object == null) {
        return null;
    }
    Class<?> cls = object.getClass();
    StringConverter<Object> conv = findConverterNoGenerics(cls);
    return conv.convertToString(object);
}

public StringConverter<Object> findConverterNoGenerics(final Class<?> cls) {
    return findTypedConverterNoGenerics(cls);
}

public TypedStringConverter<Object> findTypedConverterNoGenerics(final Class<?> cls) {
    TypedStringConverter<Object> conv = (TypedStringConverter<Object>) findConverterQuiet(cls);
    if (conv == null) {
        throw new IllegalStateException("No registered converter found: " + cls);//we throw this exception
    }
    return conv;
}

private <T> TypedStringConverter<T> findConverterQuiet(final Class<T> cls) {
    if (cls == null) {
        throw new IllegalArgumentException("Class must not be null");
    }
    TypedStringConverter<T> conv = (TypedStringConverter<T>) registered.get(cls);
    if (conv == CACHED_NULL) {
        return null;
    }
    if (conv == null) {
        try {
            conv = findAnyConverter(cls);//method b
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

public <T> StringConverter<T> findConverter(final Class<T> cls) {//method a
    return findTypedConverter(cls);
}

@Test
public void test_shortArray() {
    doTest(new short[0], "");
    doTest(new short[] {5}, "5");
    doTest(new short[] {-1234, 5678}, "-1234,5678");
}

private void doTest(short[] array, String str) {
    StringConvert test = new StringConvert(true, NumericArrayStringConverterFactory.INSTANCE);
    assertEquals(str, test.convertToString(array));//this calls method a
    assertEquals(str, test.convertToString(short[].class, array));
    assertTrue(Arrays.equals(array, test.convertFromString(short[].class, str)));
}

public StringConverter<?> findConverter(Class<?> cls) {//if an array is a primitive we do not need to bother trying to call method-b
    if (cls.isArray() && cls.getComponentType().isPrimitive()) {
        if (cls == long[].class) {
            return LongArrayStringConverter.INSTANCE;
        }
        if (cls == int[].class) {
            return IntArrayStringConverter.INSTANCE;
        }
        if (cls == short[].class) {
            return ShortArrayStringConverter.INSTANCE;
        }
        if (cls == double[].class) {
            return DoubleArrayStringConverter.INSTANCE;
        }
        if (cls == float[].class) {
            return FloatArrayStringConverter.INSTANCE;
        }
    }
    return null;
}
