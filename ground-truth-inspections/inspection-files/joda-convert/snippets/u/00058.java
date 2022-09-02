public interface StringConverter<T> extends ToStringConverter<T>, FromStringConverter<T> {
//this is just an empty interface
}

public interface TypedStringConverter<T> extends StringConverter<T> {
    //...
}

public String convertToString(Class<?> cls, Object object) {
    if (object == null) {
        return null;
    }
    StringConverter<Object> conv = findConverterNoGenerics(cls);
    return conv.convertToString(object);
}

final class EnumStringConverterFactory implements StringConverterFactory {
    //...

    @Override
    public String convertToString(Enum<?> en) {
        return en.name();  // avoid toString() as that can be overridden
    }

    //...
}

@Test
public void test_convertToString_inherit() {
    assertEquals("CEILING", StringConvert.INSTANCE.convertToString(RoundingMode.CEILING));
}
