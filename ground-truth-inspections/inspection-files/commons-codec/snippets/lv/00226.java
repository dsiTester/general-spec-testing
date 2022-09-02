public class DoubleMetaphone {
    public class DoubleMetaphoneResult {

        public void appendPrimary(final char value) { // definition of a
            if (this.primary.length() < this.maxLength) {
                this.primary.append(value);
            }
        }

        public String getPrimary() { // definition of b
            return this.primary.toString();
        }

        public void append(final char value) { // called from doubleMetaphone()
            appendPrimary(value); // call to a
            appendAlternate(value);
        }
    }

    public boolean isDoubleMetaphoneEqual(final String value1, final String value2) { // called from test
        return isDoubleMetaphoneEqual(value1, value2, false); // calls a and b
    }

    public boolean isDoubleMetaphoneEqual(final String value1, final String value2, final boolean alternate) {
        return StringUtils.equals(doubleMetaphone(value1, alternate), doubleMetaphone(value2, alternate)); // calls a and b
    }

    public String doubleMetaphone(String value, final boolean alternate) {
        ...

        final DoubleMetaphoneResult result = new DoubleMetaphoneResult(this.getMaxCodeLen());

        while (!result.isComplete() && index <= value.length() - 1) {
            switch (value.charAt(index)) {
            ...
            case 'N':
                result.append('N'); // calls a
                index = charAt(value, index + 1) == 'N' ? index + 2 : index + 1;
                break;
            case '\u00D1':
                // N with a tilde (spanish ene)
                result.append('N'); // calls a
                index++;
                break;
            ...
            default:
                index++;
                break;
            }
        }

        return alternate ? result.getAlternate() : result.getPrimary(); // call to b
    }

}

public class DoubleMetaphoneTest {
    @Test
    public void testNTilde() {
        assertTrue(this.getStringEncoder().isDoubleMetaphoneEqual("\u00f1", "N")); // calls a and b
    }
}
