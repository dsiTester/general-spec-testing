public class BeiderMorseEncoder implements StringEncoder {

    /**
     * Sets the type of name. Use {@link NameType#GENERIC} unless you specifically want phonetic encodings
     * optimized for Ashkenazi or Sephardic Jewish family names.
     *
     * @param nameType
     *            the NameType in use
     */
    public void setNameType(final NameType nameType) { // definition of a
        this.engine = new PhoneticEngine(nameType,
                                         this.engine.getRuleType(),
                                         this.engine.isConcat(),
                                         this.engine.getMaxPhonemes());
    }

    /**
     * Gets the name type currently in operation.
     *
     * @return the NameType currently being used
     */
    public NameType getNameType() { // definition of b
        return this.engine.getNameType();
    }
}

public enum NameType {          // reference for the state that a is setting.

    /** Ashkenazi family names */
    ASHKENAZI("ash"),

    /** Generic names and words */
    GENERIC("gen"),

    /** Sephardic family names */
    SEPHARDIC("sep");

    private final String name;

    NameType(final String name) {
        this.name = name;
    }

    /**
     * Gets the short version of the name type.
     *
     * @return the NameType short string
     */
    public String getName() {
        return this.name;
    }
}

public class BeiderMorseEncoderTest extends StringEncoderAbstractTest<StringEncoder> {
    @Test
    public void testSetNameTypeAsh() {
        final BeiderMorseEncoder bmpm = new BeiderMorseEncoder();
        bmpm.setNameType(NameType.ASHKENAZI); // call to a
        assertEquals("Name type should have been set to ash", NameType.ASHKENAZI, bmpm.getNameType()); // call to b; assertion fails here
    }
}
