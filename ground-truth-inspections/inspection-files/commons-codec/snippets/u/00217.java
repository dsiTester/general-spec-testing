public class DoubleMetaphone {

    public class DoubleMetaphoneResult {

        public void append(final char value) { // definition of a
            appendPrimary(value);
            appendAlternate(value); // call to b
        }

        public void appendAlternate(final char value) { // definition of b
            if (this.alternate.length() < this.maxLength) {
                this.alternate.append(value);
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
