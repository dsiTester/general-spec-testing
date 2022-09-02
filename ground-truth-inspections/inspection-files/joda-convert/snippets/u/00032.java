public <T extends Enum<T>> T lookupEnum(Class<T> type, String name) {//method a
    if (type == null) {
        throw new IllegalArgumentException("type must not be null");
    }
    if (name == null) {
        throw new IllegalArgumentException("name must not be null");
    }
    Map<String, Enum<?>> map = getEnumRenames(type);//method b
    Enum<?> value = map.get(name);
    if (value != null) {
        return type.cast(value);
    }
    return Enum.valueOf(type, name);
}

@Test
public void test_Enum() {
    TypedStringConverter<RoundingMode> test = StringConvert.create().findTypedConverter(RoundingMode.class);
    assertEquals(RoundingMode.class, test.getEffectiveType());
    assertEquals("CEILING", test.convertToString(RoundingMode.CEILING));
    assertEquals(RoundingMode.CEILING, test.convertFromString(RoundingMode.class, "CEILING"));
}
