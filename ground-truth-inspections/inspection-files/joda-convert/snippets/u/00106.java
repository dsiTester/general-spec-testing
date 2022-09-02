public <T> void registerMethodConstructor(final Class<T> cls, String toStringMethodName) {//method a
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
    Constructor<T> fromString = findFromStringConstructorByType(cls);//method b
    MethodConstructorStringConverter<T> converter = new MethodConstructorStringConverter<T>(cls, toString, fromString);
    registered.putIfAbsent(cls, converter);
}

@Test(expected=IllegalArgumentException.class)
public void test_registerMethodConstructor_noSuchConstructor() {
    StringConvert test = new StringConvert();
    test.registerMethodConstructor(Enum.class, "toString");//method a
}

