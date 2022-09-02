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
        return isDoubleMetaphoneEqual(value1, value2, false); // call to b
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
    public boolean isDoubleMetaphoneEqual(final String value1, final String value2, final boolean alternate) { // definition of b
        return StringUtils.equals(doubleMetaphone(value1, alternate), doubleMetaphone(value2, alternate));
    }
}

public class DoubleMetaphoneTest extends StringEncoderAbstractTest<DoubleMetaphone> {
    @Test
    public void testNTilde() {
        assertTrue(this.getStringEncoder().isDoubleMetaphoneEqual("\u00f1", "N")); // call to a
    }
}
