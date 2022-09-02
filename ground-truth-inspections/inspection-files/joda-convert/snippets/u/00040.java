/**
 * Mock enum used to test renames.
 */
public enum Status {

    VALID,
    INVALID;

    public static final boolean STRING_CONVERTIBLE = StringConvert.INSTANCE.isConvertible(String.class);
}

public <T> T convertFromString(Class<T> cls, String str) {//method a
    if (str == null) {
        return null;
    }
    StringConverter<T> conv = findConverter(cls);
    return conv.convertFromString(cls, str);
}

public boolean isConvertible(final Class<?> cls) {//method b
    try {
        return cls != null && findConverterQuiet(cls) != null;
    } catch (RuntimeException ex) {
        return false;
    }
}


@Test
public void test_convertFromString_inherit() {
    assertEquals(RoundingMode.CEILING, StringConvert.INSTANCE.convertFromString(RoundingMode.class, "CEILING")); // call to method-a
}
