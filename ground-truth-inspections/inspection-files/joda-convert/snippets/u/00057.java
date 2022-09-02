public interface StringConverter<T> extends ToStringConverter<T>, FromStringConverter<T> {
//this is just an empty interface
}

public void test_findConverterNoGenerics() {
    Class<?> cls = Integer.class;
    StringConverter<Object> conv = StringConvert.INSTANCE.findConverterNoGenerics(cls);
    assertEquals(Integer.valueOf(12), conv.convertFromString(cls, "12"));//this assertion failed
    assertEquals("12", conv.convertToString(12));
}

public <T> T convertFromString(Class<T> cls, String str) {//method a
    if (str == null) {
        return null;
    }
    StringConverter<T> conv = findConverter(cls);
    return conv.convertFromString(cls, str);
}

public String convertToString(Class<?> cls, Object object) {//method b
    if (object == null) {
        return null;
    }
    StringConverter<Object> conv = findConverterNoGenerics(cls);
    return conv.convertToString(object);
}
