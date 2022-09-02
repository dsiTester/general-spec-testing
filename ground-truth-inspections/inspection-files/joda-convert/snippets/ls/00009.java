@Override
public StringConverter<?> findConverter(Class<?> cls) {
    return findAnnotatedConverter(cls);  // capture generics
}

private <T> StringConverter<T> findAnnotatedConverter(final Class<T> cls) {
    Method toString = findToStringMethod(cls);  // checks superclasses
    if (toString == null) {
        return null;
    }
    MethodConstructorStringConverter<T> con = findFromStringConstructor(cls, toString);//method a
    MethodsStringConverter<T> mth = findFromStringMethod(cls, toString, con == null);  //calls method b
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
        Method fromString = findFromString(loopCls);
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
    if (searchSuperclasses) {
        for (Class<?> loopIfc : eliminateEnumSubclass(cls).getInterfaces()) {//method b
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

private <T> MethodConstructorStringConverter<T> findFromStringConstructor(Class<T> cls, Method toString) {//method a, purely functional
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


private Class<?> eliminateEnumSubclass(Class<?> cls) {//method b, purely functional
    Class<?> sup = cls.getSuperclass();
    if (sup != null && sup.getSuperclass() == Enum.class) {
        return sup;
    }
    return cls;
}

@Test(expected=IllegalStateException.class)//we expect to fail
public void test_convert_annotatedToStringNoFromString() {
    StringConvert test = new StringConvert();
    test.findConverter(DistanceToStringNoFromString.class);
}
