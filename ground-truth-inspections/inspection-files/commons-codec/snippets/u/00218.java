public class DoubleMetaphone {

    public class DoubleMetaphoneResult {

        public void append(final char value) { // definition of a
            appendPrimary(value); // call to b
            appendAlternate(value);
        }

        public void appendPrimary(final char value) { // definition of b
            if (this.primary.length() < this.maxLength) {
                this.primary.append(value);
            }
        }
    }
}

public class DoubleMetaphoneTest {
    @Test
    public void testNTilde() {
        assertTrue(this.getStringEncoder().isDoubleMetaphoneEqual("\u00f1", "N")); // n-tilde
    }

}
