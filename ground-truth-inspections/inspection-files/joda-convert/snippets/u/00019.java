                                                //inheritance
static final class EnumStringConverter implements TypedStringConverter<Enum<?>> {

    private final Class<?> effectiveType;

    EnumStringConverter(Class<?> effectiveType) {
        this.effectiveType = effectiveType;
    }

    @Override
    public String convertToString(Enum<?> en) {//method a, pure getter
        return en.name();  // avoid toString() as that can be overridden
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Enum<?> convertFromString(Class<? extends Enum<?>> cls, String str) {//method b
        return RenameHandler.INSTANCE.lookupEnum((Class) cls, str);
    }

    @Override
    public Class<?> getEffectiveType() {
        return effectiveType;
    }
}

public void test_Enum() {
    TypedStringConverter<RoundingMode> test = StringConvert.create().findTypedConverter(RoundingMode.class);
    assertEquals(RoundingMode.class, test.getEffectiveType());
    assertEquals("CEILING", test.convertToString(RoundingMode.CEILING));//we fail this assertion because we delayed a
    assertEquals(RoundingMode.CEILING, test.convertFromString(RoundingMode.class, "CEILING"));
}
