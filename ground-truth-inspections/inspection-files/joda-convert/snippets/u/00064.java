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
    MethodsStringConverter<T> mth = findFromStringMethod(cls, toString, con == null);//method b
    if (con == null && mth == null) {
        throw new IllegalStateException("Class annotated with @ToString but not with @FromString: " + cls.getName());
    }
    if (con != null && mth != null) {
        throw new IllegalStateException("Both method and constructor are annotated with @FromString: " + cls.getName());
    }
    return (con != null ? con : mth);
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