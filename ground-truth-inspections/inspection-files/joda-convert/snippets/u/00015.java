private Method findToStringMethod(Class<?> cls) {//method a
    Method matched = null;
    // find in superclass hierarchy
    Class<?> loopCls = cls;
    while (loopCls != null && matched == null) {
        Method[] methods = loopCls.getDeclaredMethods();
        for (Method method : methods) {
            if (!method.isBridge() && !method.isSynthetic()) {
                ToString toString = method.getAnnotation(ToString.class);
                if (toString != null) {
                    if (matched != null) {
                        throw new IllegalStateException("Two methods are annotated with @ToString: " + cls.getName());
                    }
                    matched = method;
                }
            }
        }
        loopCls = loopCls.getSuperclass();
    }
    // find in immediate parent interfaces
    if (matched == null) {
        for (Class<?> loopIfc : eliminateEnumSubclass(cls).getInterfaces()) {
            Method[] methods = loopIfc.getDeclaredMethods();
            for (Method method : methods) {
                if (!method.isBridge() && !method.isSynthetic()) {
                    ToString toString = method.getAnnotation(ToString.class);
                    if (toString != null) {
                        if (matched != null) {
                            throw new IllegalStateException("Two methods are annotated with @ToString on interfaces: " + cls.getName());
                        }
                        matched = method;
                    }
                }
            }
        }
    }
    return matched;
}

private <T> MethodConstructorStringConverter<T> findFromStringConstructor(Class<T> cls, Method toString) {//method b
    Constructor<T> con;
    try {
        con = cls.getDeclaredConstructor(String.class);
    } catch (NoSuchMethodException ex) {
        try {
            con = cls.getDeclaredConstructor(CharSequence.class);
        } catch (NoSuchMethodException ex2) {
            return null;
        }
    }
    FromString fromString = con.getAnnotation(FromString.class);
    if (fromString == null) {
        return null;
    }
    return new MethodConstructorStringConverter<T>(cls, toString, con);
}

private <T> StringConverter<T> findAnnotatedConverter(final Class<T> cls) {
    Method toString = findToStringMethod(cls);  //method a
    if (toString == null) {
        return null;//we return here
    }
    MethodConstructorStringConverter<T> con = findFromStringConstructor(cls, toString);//method b
    MethodsStringConverter<T> mth = findFromStringMethod(cls, toString, con == null);
    if (con == null && mth == null) {
        throw new IllegalStateException("Class annotated with @ToString but not with @FromString: " + cls.getName());
    }
    if (con != null && mth != null) {
        throw new IllegalStateException("Both method and constructor are annotated with @FromString: " + cls.getName());
    }
    return (con != null ? con : mth);
}

/**
    * Finds a converter searching annotated.
    * 
    * @param <T>  the type of the converter
    * @param cls  the class to find a method for, not null
    * @return the converter, not null
    * @throws RuntimeException if none found
    */
private <T> StringConverter<T> findAnnotatedConverter(final Class<T> cls) {
    Method toString = findToStringMethod(cls);  //this is replaced by null
    if (toString == null) {
        return null;//we return null
    }
    MethodConstructorStringConverter<T> con = findFromStringConstructor(cls, toString);
    MethodsStringConverter<T> mth = findFromStringMethod(cls, toString, con == null);  // optionally checks superclasses
    if (con == null && mth == null) {
        throw new IllegalStateException("Class annotated with @ToString but not with @FromString: " + cls.getName());
    }
    if (con != null && mth != null) {
        throw new IllegalStateException("Both method and constructor are annotated with @FromString: " + cls.getName());
    }
    return (con != null ? con : mth);
}

@SuppressWarnings("unchecked")
public TypedStringConverter<Object> findTypedConverterNoGenerics(final Class<?> cls) {
    TypedStringConverter<Object> conv = (TypedStringConverter<Object>) findConverterQuiet(cls);
    if (conv == null) {
        throw new IllegalStateException("No registered converter found: " + cls);//we throw here
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

private <T> TypedStringConverter<T> findAnyConverter(final Class<T> cls) {
    // check factories
    for (StringConverterFactory factory : factories) {
        StringConverter<T> factoryConv = (StringConverter<T>) factory.findConverter(cls);//where method-a should return a non-null converter
        if (factoryConv != null) {
            return TypedAdapter.adapt(cls, factoryConv);
        }
    }
    return null;
}

@Test
public void test_convert_annotation_ToStringOnInterface() {
    StringConvert test = new StringConvert();
    Test1Class d = new Test1Class(25);
    assertEquals("25g", test.convertToString(d));//we fail here
    assertEquals(d.amount, test.convertFromString(Test1Class.class, "25g").amount);
    TypedStringConverter<Test1Class> conv = test.findTypedConverter(Test1Class.class);
    assertEquals(true, conv instanceof MethodsStringConverter<?>);
    assertEquals(Test1Class.class, conv.getEffectiveType());
    assertSame(conv, test.findConverter(Test1Class.class));
    assertEquals(true, conv.toString().startsWith("RefectionStringConverter"));
}
