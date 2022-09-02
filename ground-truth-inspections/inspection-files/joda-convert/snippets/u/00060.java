public interface StringConverterFactory {

    /**
     * Finds a converter by type.
     * 
     * @param cls  the type to lookup, not null
     * @return the converter, null if not found
     * @throws RuntimeException (or subclass) if source code is invalid
     */
    StringConverter<?> findConverter(Class<?> cls);//method a

}

@Override
public StringConverter<?> findConverter(Class<?> cls) {
    return findAnnotatedConverter(cls);  // capture generics
}

private <T> StringConverter<T> findAnnotatedConverter(final Class<T> cls) {
    Method toString = findToStringMethod(cls);  // checks superclasses
    if (toString == null) {
        return null;
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

private Method findToStringMethod(Class<?> cls) {
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
        for (Class<?> loopIfc : eliminateEnumSubclass(cls).getInterfaces()) {//method b
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

public TypedStringConverter<Object> findTypedConverterNoGenerics(final Class<?> cls) {
    TypedStringConverter<Object> conv = (TypedStringConverter<Object>) findConverterQuiet(cls);
    if (conv == null) {
        throw new IllegalStateException("No registered converter found: " + cls);//we throw this exception
    }
    return conv;
}

private <T> TypedStringConverter<T> findAnyConverter(final Class<T> cls) {
    // check factories
    for (StringConverterFactory factory : factories) {
        StringConverter<T> factoryConv = (StringConverter<T>) factory.findConverter(cls);//method a
        if (factoryConv != null) {
            return TypedAdapter.adapt(cls, factoryConv);
        }
    }
    return null;
}

public void test_convert_annotation_ToStringOnInterface() {
    StringConvert test = new StringConvert();
    Test1Class d = new Test1Class(25);
    assertEquals("25g", test.convertToString(d));
    assertEquals(d.amount, test.convertFromString(Test1Class.class, "25g").amount);
    TypedStringConverter<Test1Class> conv = test.findTypedConverter(Test1Class.class);
    assertEquals(true, conv instanceof MethodsStringConverter<?>);
    assertEquals(Test1Class.class, conv.getEffectiveType());
    assertSame(conv, test.findConverter(Test1Class.class));
    assertEquals(true, conv.toString().startsWith("RefectionStringConverter"));
}
