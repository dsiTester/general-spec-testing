public class PhoneticEngine {
    /**
     * Gets if multiple phonetic encodings are concatenated or if just the first one is kept.
     *
     * @return true if multiple phonetic encodings are returned, false if just the first is
     */
    public boolean isConcat() { // definition of a
        return this.concat;
    }

    /**
     * Gets the maximum number of phonemes the engine will calculate for a given input.
     *
     * @return the maximum number of phonemes
     * @since 1.7
     */
    public int getMaxPhonemes() { // definition of b
        return this.maxPhonemes;
    }

}

public class BeiderMorseEncoder implements StringEncoder {
    public void setNameType(final NameType nameType) {
        this.engine = new PhoneticEngine(nameType,
                                         this.engine.getRuleType(), // call to a
                                         this.engine.isConcat(),
                                         this.engine.getMaxPhonemes()); // call to b
    }


}

public class BeiderMorseEncoderTest extends StringEncoderAbstractTest<StringEncoder> {
    @Test
    public void testSetNameTypeAsh() {
        final BeiderMorseEncoder bmpm = new BeiderMorseEncoder();
        bmpm.setNameType(NameType.ASHKENAZI); // calls a and b
        assertEquals("Name type should have been set to ash", NameType.ASHKENAZI, bmpm.getNameType());
    }
}
