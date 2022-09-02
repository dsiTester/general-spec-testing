public class DoubleMetaphone {
    public boolean isDoubleMetaphoneEqual(final String value1, final String value2) { // called from test
        return isDoubleMetaphoneEqual(value1, value2, false);
    }

    /**
     * Check if the Double Metaphone values of two {@code String} values
     * are equal, optionally using the alternate value.
     *
     * @param value1 The left-hand side of the encoded {@link String#equals(Object)}.
     * @param value2 The right-hand side of the encoded {@link String#equals(Object)}.
     * @param alternate use the alternate value if {@code true}.
     * @return {@code true} if the encoded {@code String}s are equal;
     *          {@code false} otherwise.
     */
    public boolean isDoubleMetaphoneEqual(final String value1, final String value2, final boolean alternate) { // definition of a
        return StringUtils.equals(doubleMetaphone(value1, alternate), doubleMetaphone(value2, alternate)); // doubleMetaphone() calls b
    }

    public String doubleMetaphone(String value, final boolean alternate) {
        ...
        final DoubleMetaphoneResult result = new DoubleMetaphoneResult(this.getMaxCodeLen());

        while (!result.isComplete() && index <= value.length() - 1) {
            switch (value.charAt(index)) {
            ...
            case 'N':
                result.append('N');
                index = charAt(value, index + 1) == 'N' ? index + 2 : index + 1; // call to b
                break;
            ...
            }
            ...
        }
        ...
    }

    /*
     * Gets the character at index {@code index} if available, otherwise
     * it returns {@code Character.MIN_VALUE} so that there is some sort
     * of a default.
     */
    protected char charAt(final String value, final int index) { // definition of b
        if (index < 0 || index >= value.length()) {
            return Character.MIN_VALUE;
        }
        return value.charAt(index);
    }
}

public class DoubleMetaphoneTest extends StringEncoderAbstractTest<DoubleMetaphone> {
    @Test
    public void testNTilde() {
        assertTrue(this.getStringEncoder().isDoubleMetaphoneEqual("\u00f1", "N")); // calls a
    }
}
