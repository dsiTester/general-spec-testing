private <T> StringConverter<T> findAnnotatedConverter(final Class<T> cls) {//method a
    Method toString = findToStringMethod(cls);
    if (toString == null) {
        return null;
    }
    MethodConstructorStringConverter<T> con = findFromStringConstructor(cls, toString); //method b
    MethodsStringConverter<T> mth = findFromStringMethod(cls, toString, con == null); 
    if (con == null && mth == null) {
        throw new IllegalStateException("Class annotated with @ToString but not with @FromString: " + cls.getName());
    }
    if (con != null && mth != null) {
        throw new IllegalStateException("Both method and constructor are annotated with @FromString: " + cls.getName());
    }
    return (con != null ? con : mth);
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

@Override
public StringConverter<?> findConverter(Class<?> cls) {//this is the only public method (other than the toString) in AnnotationStringConverterFactory
    return findAnnotatedConverter(cls);  //method a
}

@Test
public void test_convert_annotationMethodMethod() {
    StringConvert test = new StringConvert();
    DistanceMethodMethod d = new DistanceMethodMethod(25);
    assertEquals("25m", test.convertToString(d));
    assertEquals(d.amount, test.convertFromString(DistanceMethodMethod.class, "25m").amount);
    TypedStringConverter<DistanceMethodMethod> conv = test.findTypedConverter(DistanceMethodMethod.class);
    assertEquals(true, conv instanceof MethodsStringConverter<?>);
    assertSame(conv, test.findConverter(DistanceMethodMethod.class));
    assertEquals(DistanceMethodMethod.class, conv.getEffectiveType());
    assertEquals(true, conv.toString().startsWith("RefectionStringConverter"));
}

@SuppressWarnings("unchecked")
public TypedStringConverter<Object> findTypedConverterNoGenerics(final Class<?> cls) {
    TypedStringConverter<Object> conv = (TypedStringConverter<Object>) findConverterQuiet(cls);
    if (conv == null) {//this is where the return from method-a was passed that caused the error
        throw new IllegalStateException("No registered converter found: " + cls);
    }
    return conv;
}