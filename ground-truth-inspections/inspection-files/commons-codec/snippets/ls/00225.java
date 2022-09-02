public class DoubleMetaphoneTest {
    public class DoubleMetaphoneResult {
        public void append(final char value) {
            appendPrimary(value);   // call to a
            appendAlternate(value); // call to b
        }

        public void appendPrimary(final char value) { // definition of a
            if (this.primary.length() < this.maxLength) {
                this.primary.append(value);
            }
        }

        public void appendAlternate(final char value) { // definition of b
            if (this.alternate.length() < this.maxLength) {
                this.alternate.append(value);
            }
        }

    }
    public String doubleMetaphone(String value, final boolean alternate) {
        ...

        final DoubleMetaphoneResult result = new DoubleMetaphoneResult(this.getMaxCodeLen());

        while (!result.isComplete() && index <= value.length() - 1) {
            switch (value.charAt(index)) {
            case 'A':
            case 'E':
            case 'I':
            case 'O':
            case 'U':
            case 'Y':
                index = handleAEIOUY(result, index);
                break;
            ...
            case '\u00C7':
                // A C with a Cedilla
                result.append('S'); // most likely call to a?
                index++;
                break;
            ...
            case 'S':
                index = handleS(value, result, index, slavoGermanic);
                break;
            ...
            default:
                index++;
                break;
            }
        }

        return alternate ? result.getAlternate() : result.getPrimary();
    }

}

public class DoubleMetaphoneTest {
    @Test
    public void testCCedilla() {
        assertTrue(this.getStringEncoder().isDoubleMetaphoneEqual("\u00e7", "S")); // c-cedilla
    }
}
