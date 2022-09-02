public class DoubleMetaphone {
    public class DoubleMetaphoneResult {
        public void appendPrimary(final String value) { // definition of a
            final int addChars = this.maxLength - this.primary.length();
            if (value.length() <= addChars) {
                this.primary.append(value);
            } else {
                this.primary.append(value.substring(0, addChars));
            }
        }

        public void appendAlternate(final String value) { // definition of b
            final int addChars = this.maxLength - this.alternate.length();
            if (value.length() <= addChars) {
                this.alternate.append(value);
            } else {
                this.alternate.append(value.substring(0, addChars));
            }
        }

        public void append(final String value) { // calling context 1
            appendPrimary(value);                // call to a
            appendAlternate(value);              // call to b
        }

        public void append(final String primary, final String alternate) { // calling context 2
            appendPrimary(primary); // call to a
            appendAlternate(alternate); // call to b
        }
    }

    public boolean isDoubleMetaphoneEqual(final String value1, final String value2, final boolean alternate) { // called from test
        return StringUtils.equals(doubleMetaphone(value1, alternate), doubleMetaphone(value2, alternate)); // doubleMetaphone calls a and b
    }
}

public class DoubleMetaphoneTest {
    @Test
    public void testIsDoubleMetaphoneEqualWithMATCHES() {
        this.validateFixture(MATCHES);
        for (int i = 0; i < MATCHES.length; i++) {
            final String name0 = MATCHES[i][0];
            final String name1 = MATCHES[i][1];
            final boolean match1 = this.getStringEncoder().isDoubleMetaphoneEqual(name0, name1, false);
            final boolean match2 = this.getStringEncoder().isDoubleMetaphoneEqual(name0, name1, true);
            if (match1 == false && match2 == false) {
                fail("Expected match [" + i + "] " + name0 + " and " + name1);
            }
        }
    }
}
