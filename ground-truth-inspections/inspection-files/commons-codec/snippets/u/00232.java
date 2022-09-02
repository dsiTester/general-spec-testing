public class DoubleMetaphone {
    /**
     * Check if the Double Metaphone values of two {@code String} values
     * are equal.
     *
     * @param value1 The left-hand side of the encoded {@link String#equals(Object)}.
     * @param value2 The right-hand side of the encoded {@link String#equals(Object)}.
     * @return {@code true} if the encoded {@code String}s are equal;
     *          {@code false} otherwise.
     * @see #isDoubleMetaphoneEqual(String,String,boolean)
     */
    public boolean isDoubleMetaphoneEqual(final String value1, final String value2) { // definition of a
        return isDoubleMetaphoneEqual(value1, value2, false);
    }

    public boolean isDoubleMetaphoneEqual(final String value1, final String value2, final boolean alternate) { // called from a
        return StringUtils.equals(doubleMetaphone(value1, alternate), doubleMetaphone(value2, alternate)); // doubleMetaphone() calls b
    }

    public String doubleMetaphone(String value, final boolean alternate) {
        ...
        final DoubleMetaphoneResult result = new DoubleMetaphoneResult(this.getMaxCodeLen());

        while (!result.isComplete() && index <= value.length() - 1) {
            switch (value.charAt(index)) {
            ...
            case 'S':
                index = handleS(value, result, index, slavoGermanic); // call to b
                break;
            ...
            }
            ...
        }
        ...
    }

    /**
     * Handles 'S' cases.
     */
    private int handleS(final String value, final DoubleMetaphoneResult result, int index,
                        final boolean slavoGermanic) { // definition of b
        if (contains(value, index - 1, 3, "ISL", "YSL")) {
            //-- special cases "island", "isle", "carlisle", "carlysle" --//
            index++;
        } ...                   // redacting the rest of b
        return index;
    }
}

public class DoubleMetaphoneTest extends StringEncoderAbstractTest<DoubleMetaphone> {
    @Test
    public void testCCedilla() {
        assertTrue(this.getStringEncoder().isDoubleMetaphoneEqual("\u00e7", "S")); // call to a
    }

}
