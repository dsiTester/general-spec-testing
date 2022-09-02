private <T> StringConverter<T> findAnnotatedConverter(final Class<T> cls) {//method a
    Method toString = findToStringMethod(cls);  //method b is called as a result of this
    if (toString == null) {
        return null;
    }
    MethodConstructorStringConverter<T> con = findFromStringConstructor(cls, toString); //method b is called as a result of this
    MethodsStringConverter<T> mth = findFromStringMethod(cls, toString, con == null); 
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

private Class<?> eliminateEnumSubclass(Class<?> cls) {//method b, purely functional
    Class<?> sup = cls.getSuperclass();
    if (sup != null && sup.getSuperclass() == Enum.class) {
        return sup;
    }
    return cls;
}

@Test
public void test_isConvertible() {
    assertTrue(StringConvert.INSTANCE.isConvertible(Integer.class));//this results in the construction of the AnnotationStringConverterFactory, and the attempt to find a converter
    assertTrue(StringConvert.INSTANCE.isConvertible(String.class));
    assertFalse(StringConvert.INSTANCE.isConvertible(Object.class));
}

@Override
public StringConverter<?> findConverter(Class<?> cls) {//this is the only public method (other than the toString) in AnnotationStringConverterFactory
    return findAnnotatedConverter(cls);//method-a
}