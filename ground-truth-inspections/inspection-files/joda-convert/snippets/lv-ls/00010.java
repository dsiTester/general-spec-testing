private <T> MethodConstructorStringConverter<T> findFromStringConstructor(Class<T> cls, Method toString) {//method a
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
    Method toString = findToStringMethod(cls);  // checks superclasses
    if (toString == null) {
        return null;
    }
    MethodConstructorStringConverter<T> con = findFromStringConstructor(cls, toString); //method a
    MethodsStringConverter<T> mth = findFromStringMethod(cls, toString, con == null); //this calls method b, notice the boolean con==null
    if (con == null && mth == null) {
        throw new IllegalStateException("Class annotated with @ToString but not with @FromString: " + cls.getName());
    }
    if (con != null && mth != null) {
        throw new IllegalStateException("Both method and constructor are annotated with @FromString: " + cls.getName());
    }
    return (con != null ? con : mth);
}

private <T> MethodsStringConverter<T> findFromStringMethod(Class<T> cls, Method toString, boolean searchSuperclasses) {
    // find in superclass hierarchy
    Class<?> loopCls = cls;
    while (loopCls != null) {
        Method fromString = findFromString(loopCls);//method b
        if (fromString != null) {
            return new MethodsStringConverter<T>(cls, toString, fromString, loopCls);
        }
        if (searchSuperclasses == false) {
            break;
        }
        loopCls = loopCls.getSuperclass();
    }
    // find in immediate parent interfaces
    MethodsStringConverter<T> matched = null;
    if (searchSuperclasses) {//we skip this because we replaced method-a with null
        for (Class<?> loopIfc : eliminateEnumSubclass(cls).getInterfaces()) {
            Method fromString = findFromString(loopIfc);
            if (fromString != null) {
                if (matched != null) {
                    throw new IllegalStateException("Two different interfaces are annotated with " +
                        "@FromString or @FromStringFactory: " + cls.getName());
                }
                matched = new MethodsStringConverter<T>(cls, toString, fromString, loopIfc);
            }
        }
    }
    return matched;
}

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
    // check for factory
    FromStringFactory factory = cls.getAnnotation(FromStringFactory.class);
    if (factory != null) {
        if (matched != null) {
            throw new IllegalStateException("Class annotated with @FromString and @FromStringFactory: " + cls.getName());
        }
        Method[] factoryMethods = factory.factory().getDeclaredMethods();
        for (Method method : factoryMethods) {
            if (!method.isBridge() && !method.isSynthetic()) {
                // handle factory containing multiple FromString for different types
                if (cls.isAssignableFrom(method.getReturnType())) {
                    FromString fromString = method.getAnnotation(FromString.class);
                    if (fromString != null) {
                        if (matched != null) {
                            throw new IllegalStateException("Two methods are annotated with @FromString on the factory: " + factory.factory().getName());
                        }
                        matched = method;
                    }
                }
            }
        }
    }
    return matched;
}

@Test(expected=IllegalStateException.class)//we expect to fail
public void test_convert_annotatedMethodAndConstructor() {//lv
    StringConvert test = new StringConvert();
    test.findConverter(DistanceMethodAndConstructorAnnotations.class);
}