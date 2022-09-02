public String convertToString(Enum<?> en) {//method a, pure function
    return en.name();  // avoid toString() as that can be overridden
}

public Enum<?> convertFromString(Class<? extends Enum<?>> cls, String str) {//method b
    return RenameHandler.INSTANCE.lookupEnum((Class) cls, str);
}


public void test_Enum() {
    TypedStringConverter<RoundingMode> test = StringConvert.create().findTypedConverter(RoundingMode.class);
    assertEquals(RoundingMode.class, test.getEffectiveType());
    assertEquals("CEILING", test.convertToString(RoundingMode.CEILING));//we fail this assertion because we delayed a
    assertEquals(RoundingMode.CEILING, test.convertFromString(RoundingMode.class, "CEILING"));
}
