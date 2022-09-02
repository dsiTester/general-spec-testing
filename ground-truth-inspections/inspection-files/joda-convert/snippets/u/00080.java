public <T> void registerMethodConstructor(final Class<T> cls, String toStringMethodName) {
    if (cls == null) {
        throw new IllegalArgumentException("Class must not be null");
    }
    if (toStringMethodName == null) {
        throw new IllegalArgumentException("Method name must not be null");
    }
    if (this == INSTANCE) {
        throw new IllegalStateException("Global singleton cannot be extended");
    }
    Method toString = findToStringMethod(cls, toStringMethodName);
    Constructor<T> fromString = findFromStringConstructorByType(cls);//method a, replaced with null
    MethodConstructorStringConverter<T> converter = new MethodConstructorStringConverter<T>(cls, toString, fromString);
    registered.putIfAbsent(cls, converter);
}

MethodsStringConverter(Class<T> cls, Method toString, Method fromString, Class<?> effectiveType) {
    super(cls, toString);
    if (Modifier.isStatic(fromString.getModifiers()) == false) {//NPE
        throw new IllegalStateException("FromString method must be static: " + fromString);
    }
    if (fromString.getParameterTypes().length != 1) {
        throw new IllegalStateException("FromString method must have one parameter: " + fromString);
    }
    Class<?> param = fromString.getParameterTypes()[0];
    if (param != String.class && param != CharSequence.class) {
        throw new IllegalStateException("FromString method must take a String or CharSequence: " + fromString);
    }
    if (fromString.getReturnType().isAssignableFrom(cls) == false && cls.isAssignableFrom(fromString.getReturnType()) == false) {
        throw new IllegalStateException("FromString method must return specified class or a supertype: " + fromString);
    }
    this.fromString = fromString;
    this.effectiveType = effectiveType;
}

private <T> Constructor<T> findFromStringConstructorByType(Class<T> cls) {
    try {
        return cls.getDeclaredConstructor(String.class);
    } catch (NoSuchMethodException ex) {
        try {
            return cls.getDeclaredConstructor(CharSequence.class);
        } catch (NoSuchMethodException ex2) {
            throw new IllegalArgumentException("Constructor not found", ex2);
        }
    }
}

public <T> T convertFromString(Class<T> cls, String str) {//method b
    if (str == null) {
        return null;
    }
    StringConverter<T> conv = findConverter(cls);
    return conv.convertFromString(cls, str);
}

@Test
public void test_registerMethodsCharSequence() {
    StringConvert test = new StringConvert();
    test.registerMethods(DistanceNoAnnotationsCharSequence.class, "toString", "parse");//calls method a
    DistanceNoAnnotationsCharSequence d = new DistanceNoAnnotationsCharSequence(25);
    assertEquals("Distance[25m]", test.convertToString(d));
    assertEquals(d.amount, test.convertFromString(DistanceNoAnnotationsCharSequence.class, "25m").amount);
    StringConverter<DistanceNoAnnotationsCharSequence> conv = test.findConverter(DistanceNoAnnotationsCharSequence.class);
    assertEquals(true, conv instanceof MethodsStringConverter<?>);
    assertSame(conv, test.findConverter(DistanceNoAnnotationsCharSequence.class));
}