static final class EnumStringConverter implements TypedStringConverter<Enum<?>> {

    private final Class<?> effectiveType;

    EnumStringConverter(Class<?> effectiveType) {
        this.effectiveType = effectiveType;
    }

    @Override
    public String convertToString(Enum<?> en) {
        return en.name();  // avoid toString() as that can be overridden
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Enum<?> convertFromString(Class<? extends Enum<?>> cls, String str) {//method-a/method-b
        return RenameHandler.INSTANCE.lookupEnum((Class) cls, str);
    }

    @Override
    public Class<?> getEffectiveType() {
        return effectiveType;
    }
}



/**
 * Interface defining conversion to and from a {@code String}.
 * <p>
 * StringConverter is an interface and must be implemented with care.
 * Implementations must be immutable and thread-safe.
 * 
 * @param <T>  the type of the converter
 */
public interface StringConverter<T> extends ToStringConverter<T>, FromStringConverter<T> {
    //this interfaces is empty
}

public interface TypedStringConverter<T> extends StringConverter<T> {

    /**
     * Gets the effective type that the converter works on.
     * <p>
     * For example, if a class declares the {@code FromString} and  {@code ToString}
     * then the effective type of the converter is that class. If a subclass is
     * queried for a converter, then the effective type is that of the superclass.
     * 
     * @return the effective type
     */
    Class<?> getEffectiveType();

}



@Test(expected=RuntimeException.class)
public void test_Enum_invalidConstant() {
    TypedStringConverter<RoundingMode> test = StringConvert.create().findTypedConverter(RoundingMode.class);
    test.convertFromString(RoundingMode.class, "RUBBISH");//method-a/method-b
}