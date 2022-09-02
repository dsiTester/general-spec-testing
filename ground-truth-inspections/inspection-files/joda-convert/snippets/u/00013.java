private <T> MethodsStringConverter<T> findFromStringMethod(Class<T> cls, Method toString, boolean searchSuperclasses) {//method a
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
    if (searchSuperclasses) {
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

private <T> StringConverter<T> findAnnotatedConverter(final Class<T> cls) {
    Method toString = findToStringMethod(cls);  // checks superclasses
    if (toString == null) {
        return null;
    }
    MethodConstructorStringConverter<T> con = findFromStringConstructor(cls, toString);
    MethodsStringConverter<T> mth = findFromStringMethod(cls, toString, con == null); 
    if (con == null && mth == null) {//we throw this exception since we have indefinitely delayed b
        throw new IllegalStateException("Class annotated with @ToString but not with @FromString: " + cls.getName());
    }
    if (con != null && mth != null) {
        throw new IllegalStateException("Both method and constructor are annotated with @FromString: " + cls.getName());
    }
    return (con != null ? con : mth);
}