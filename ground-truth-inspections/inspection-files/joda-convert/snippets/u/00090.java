
/**
 * Mock enum used to test renames.
 */
public enum Status {

    VALID,
    INVALID;

    public static final boolean STRING_CONVERTIBLE = StringConvert.INSTANCE.isConvertible(String.class);//method a
}

public boolean isConvertible(final Class<?> cls) {
    try {
        return cls != null && findConverterQuiet(cls) != null;//method b
    } catch (RuntimeException ex) {
        return false;
    }
}

@Test
public void test_Class_withRename() {
    try {
        JDKStringConverter.CLASS.convertFromString(Class.class, "org.foo.StringConvert");
        fail();
    } catch (RuntimeException ex) {
        // expected
    }
    RenameHandler.INSTANCE.renamedType("org.foo.StringConvert", StringConvert.class);
    assertEquals(StringConvert.class, JDKStringConverter.CLASS.convertFromString(Class.class, "org.foo.StringConvert"));
}
