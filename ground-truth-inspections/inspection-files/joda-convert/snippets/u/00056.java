public interface StringConverter<T> extends ToStringConverter<T>, FromStringConverter<T> {
//this is just an empty interface
}

public interface TypedStringConverter<T> extends StringConverter<T> {
    //...
}

public <T> T convertFromString(Class<T> cls, String str) {
    if (str == null) {
        return null;
    }
    StringConverter<T> conv = findConverter(cls);
    return conv.convertFromString(cls, str);//method b
}
final class EnumStringConverterFactory implements StringConverterFactory {
    //...

    public Enum<?> convertFromString(Class<? extends Enum<?>> cls, String str) {//method b (also method-a)
        return RenameHandler.INSTANCE.lookupEnum((Class) cls, str);
    }

    //...
}

@Test
public void test_convertFromString_inherit() {
    assertEquals(RoundingMode.CEILING, StringConvert.INSTANCE.convertFromString(RoundingMode.class, "CEILING"));//this is both method-a and method-b
}
