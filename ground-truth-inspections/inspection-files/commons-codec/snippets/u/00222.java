public class DoubleMetaphone {
    public class DoubleMetaphoneResult {
        public void append(final String primary, final String alternate) { // definition of a
            appendPrimary(primary);
            appendAlternate(alternate); // call to b
        }

        public void appendAlternate(final String value) { // definition of b
            final int addChars = this.maxLength - this.alternate.length();
            if (value.length() <= addChars) {
                this.alternate.append(value);
            } else {
                this.alternate.append(value.substring(0, addChars));
            }
        }
    }

    private int handleW(final String value, final DoubleMetaphoneResult result, int index) {
        if (contains(value, index, 2, "WR")) {
            ...
        } else {
            if (index == 0 && (isVowel(charAt(value, index + 1)) ||
                               contains(value, index, 2, "WH"))) {
                ...
            } ...
            else if (contains(value, index, 4, "WICZ", "WITZ")) {
                //-- Polish e.g. "filipowicz" --//
                result.append("TS", "FX"); // call to a
                index += 4;
            } ...
        }
        return index;
    }

}

public class DoubleMetaphoneTest {
    @Test
    public void testIsDoubleMetaphoneEqualBasic() {
        final String[][] testFixture = new String[][] { {
                "", "" }, {
                "Case", "case" }, {
                "CASE", "Case" }, {
                "caSe", "cAsE" }, {
                "cookie", "quick" }, {
                "quick", "cookie" }, {
                "Brian", "Bryan" }, {
                "Auto", "Otto" }, {
                "Steven", "Stefan" }, {
                "Philipowitz", "Filipowicz" } // this case failed
        };
        doubleMetaphoneEqualTest(testFixture, false); // this case failed
        doubleMetaphoneEqualTest(testFixture, true);
    }

    public void doubleMetaphoneEqualTest(final String[][] pairs, final boolean useAlternate) {
        this.validateFixture(pairs);
        for (final String[] pair : pairs) {
            final String name0 = pair[0];
            final String name1 = pair[1];
            final String failMsg = "Expected match between " + name0 + " and " + name1 + " (use alternate: " + useAlternate + ")";
            assertTrue(failMsg, this.getStringEncoder().isDoubleMetaphoneEqual(name0, name1, useAlternate)); // fail here
            assertTrue(failMsg, this.getStringEncoder().isDoubleMetaphoneEqual(name1, name0, useAlternate));
            if (!useAlternate) {
                assertTrue(failMsg, this.getStringEncoder().isDoubleMetaphoneEqual(name0, name1));
                assertTrue(failMsg, this.getStringEncoder().isDoubleMetaphoneEqual(name1, name0));
            }
        }
    }
}
