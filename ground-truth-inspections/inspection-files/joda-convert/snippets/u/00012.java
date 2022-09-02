private <T> MethodsStringConverter<T> findFromStringMethod(Class<T> cls, Method toString, boolean searchSuperclasses) {//method a
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

private Class<?> eliminateEnumSubclass(Class<?> cls) {//method b, completely stateless
    Class<?> sup = cls.getSuperclass();
    if (sup != null && sup.getSuperclass() == Enum.class) {
        return sup;
    }
    return cls;
}