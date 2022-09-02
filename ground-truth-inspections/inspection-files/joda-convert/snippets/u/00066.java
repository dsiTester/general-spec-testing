public String convertToString(Class<?> cls, Object object) {
    if (object == null) {
        return null;
    }
    StringConverter<Object> conv = findConverterNoGenerics(cls);
    return conv.convertToString(object);
}

public StringConverter<Object> findConverterNoGenerics(final Class<?> cls) {
    return findTypedConverterNoGenerics(cls);
}

public TypedStringConverter<Object> findTypedConverterNoGenerics(final Class<?> cls) {
    TypedStringConverter<Object> conv = (TypedStringConverter<Object>) findConverterQuiet(cls);
    if (conv == null) {
        throw new IllegalStateException("No registered converter found: " + cls);
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

private <T> TypedStringConverter<T> findAnyConverter(final Class<T> cls) {//method a
    // check factories
    for (StringConverterFactory factory : factories) {
        StringConverter<T> factoryConv = (StringConverter<T>) factory.findConverter(cls);
        if (factoryConv != null) {
            return TypedAdapter.adapt(cls, factoryConv);
        }
    }
    return null;
}

public StringConverter<?> findConverter(Class<?> cls) {
    return findAnnotatedConverter(cls); 
}


private <T> StringConverter<T> findAnnotatedConverter(final Class<T> cls) {
    Method toString = findToStringMethod(cls);
    if (toString == null) {
        return null;
    }
    MethodConstructorStringConverter<T> con = findFromStringConstructor(cls, toString);
    MethodsStringConverter<T> mth = findFromStringMethod(cls, toString, con == null);
    if (con == null && mth == null) {
        throw new IllegalStateException("Class annotated with @ToString but not with @FromString: " + cls.getName());
    }
    if (con != null && mth != null) {
        throw new IllegalStateException("Both method and constructor are annotated with @FromString: " + cls.getName());
    }
    return (con != null ? con : mth);
}

public <T> TypedStringConverter<T> findTypedConverter(final Class<T> cls) {
    TypedStringConverter<T> conv = findConverterQuiet(cls);
    if (conv == null) {
        throw new IllegalStateException("No registered converter found: " + cls);
    }
    return conv;
}

public TypedStringConverter<Object> findTypedConverterNoGenerics(final Class<?> cls) {
    TypedStringConverter<Object> conv = (TypedStringConverter<Object>) findConverterQuiet(cls);
    if (conv == null) {
        throw new IllegalStateException("No registered converter found: " + cls);//we throw this exception
    }
    return conv;
}

@Test
public void test_convert_annotation_ToStringOnInterface() {
    StringConvert test = new StringConvert();
    Test1Class d = new Test1Class(25);
    assertEquals("25g", test.convertToString(d));//method a is called from here
    assertEquals(d.amount, test.convertFromString(Test1Class.class, "25g").amount);//method b
    TypedStringConverter<Test1Class> conv = test.findTypedConverter(Test1Class.class);
    assertEquals(true, conv instanceof MethodsStringConverter<?>);
    assertEquals(Test1Class.class, conv.getEffectiveType());
    assertSame(conv, test.findConverter(Test1Class.class));
    assertEquals(true, conv.toString().startsWith("RefectionStringConverter"));
}
