// eliminates enum subclass as they are pesky
private Class<?> eliminateEnumSubclass(Class<?> cls) {//method a, purely functional
    Class<?> sup = cls.getSuperclass();
    if (sup != null && sup.getSuperclass() == Enum.class) {
        return sup;
    }
    return cls;
}

/**
    * Finds the conversion method.
    * 
    * @param cls  the class to find a method for, not null
    * @param matched  the matched method, may be null
    * @return the method to call, null means not found
    * @throws RuntimeException if invalid
    */
private Method findFromString(Class<?> cls) {//method b
    // find in declared methods
    Method[] methods = cls.getDeclaredMethods();
    Method matched = null;
    for (Method method : methods) {
        if (!method.isBridge() && !method.isSynthetic()) {
            FromString fromString = method.getAnnotation(FromString.class);
            if (fromString != null) {
                if (matched != null) {
                    throw new IllegalStateException("Two methods are annotated with @FromString: " + cls.getName());
                }
                matched = method;
            }
        }
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
    Method toString = findToStringMethod(cls);  // checks superclasses, eventually calls a
    if (toString == null) {
        return null;
    }
    MethodConstructorStringConverter<T> con = findFromStringConstructor(cls, toString); 
    MethodsStringConverter<T> mth = findFromStringMethod(cls, toString, con == null);  // method b
    if (con == null && mth == null) {
        throw new IllegalStateException("Class annotated with @ToString but not with @FromString: " + cls.getName());
    }
    if (con != null && mth != null) {
        throw new IllegalStateException("Both method and constructor are annotated with @FromString: " + cls.getName());
    }
    return (con != null ? con : mth);
}

/**
    * Finds the conversion method.
    * 
    * @param cls  the class to find a method for, not null
    * @return the method to call, null means use {@code toString}
    * @throws RuntimeException if invalid
    */
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
        for (Class<?> loopIfc : eliminateEnumSubclass(cls).getInterfaces()) { //method a, null pointer exception occured here since we are trying to access the interfaces of the return value of the method we delayed
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

@Test
public void test_convert_annotation_ToStringOnInterface() {
    StringConvert test = new StringConvert();
    Test1Class d = new Test1Class(25);
    assertEquals("25g", test.convertToString(d));//method a is called here
    assertEquals(d.amount, test.convertFromString(Test1Class.class, "25g").amount);//method b is called here
    TypedStringConverter<Test1Class> conv = test.findTypedConverter(Test1Class.class);
    assertEquals(true, conv instanceof MethodsStringConverter<?>);
    assertEquals(Test1Class.class, conv.getEffectiveType());
    assertSame(conv, test.findConverter(Test1Class.class));
    assertEquals(true, conv.toString().startsWith("RefectionStringConverter"));
}
