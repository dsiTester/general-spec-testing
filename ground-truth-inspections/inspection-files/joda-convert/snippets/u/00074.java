public <T> T convertFromString(Class<T> cls, String str) {
    if (str == null) {
        return null;
    }
    StringConverter<T> conv = findConverter(cls);//method a
    return conv.convertFromString(cls, str);//NPE here
}

public boolean isConvertible(final Class<?> cls) {//method b
    try {
        return cls != null && findConverterQuiet(cls) != null;
    } catch (RuntimeException ex) {
        return false;
    }
}

public String convertToString(Object object) {
    if (object == null) {
        return null;
    }
    Class<?> cls = object.getClass();
    StringConverter<Object> conv = findConverterNoGenerics(cls);
    return conv.convertToString(object);
}

/**
 * Mock enum used to test renames.
 */
public enum Status {

    VALID,
    INVALID;

    public static final boolean STRING_CONVERTIBLE = StringConvert.INSTANCE.isConvertible(String.class);//method b
}



@Test
public void test_convertToString_inherit() {
    assertEquals("CEILING", StringConvert.INSTANCE.convertToString(RoundingMode.CEILING));
}
