public abstract class AbstractCheckDigitTest extends TestCase {
    /**
     * Returns the check digit (i.e. last character) for a code.
     *
     * @param code The code
     * @return The check digit
     */
    protected String checkDigit(String code) { // definition of a
        if (code == null || code.length() <= checkDigitLth) {
            return "";
        }
        int start = code.length() - checkDigitLth;
        return code.substring(start);
    }

    /**
     * Returns a code with the Check Digit (i.e. last character) removed.
     *
     * @param code The code
     * @return The code without the check digit
     */
    protected String removeCheckDigit(String code) { // definition of b
        if (code == null || code.length() <= checkDigitLth) {
            return null;
        }
        return code.substring(0, code.length() - checkDigitLth);
    }

    public void testCalculateInvalid() {

        if (log.isDebugEnabled()) {
            log.debug("testCalculateInvalid() for " + routine.getClass().getName());
        }

        // test invalid code values
        for (int i = 0; i < invalid.length; i++) {
            try {
                final String code = invalid[i];
                if (log.isDebugEnabled()) {
                    log.debug("   " + i + " Testing Invalid Check Digit, Code=[" + code + "]");
                }
                String expected = checkDigit(code); // call to a
                String actual = routine.calculate(removeCheckDigit(code)); // call to b
                // If exception not thrown, check that the digit is incorrect instead
                if (expected.equals(actual)) {
                    fail("Expected mismatch for " + code + " expected " + expected + " actual " + actual);
                }
            } catch (CheckDigitException e) {
                // possible failure messages:
                // Invalid ISBN Length ...
                // Invalid Character[ ...
                // Are there any others?
                assertTrue("Invalid Character[" +i +"]=" +  e.getMessage(), e.getMessage().startsWith("Invalid "));
// WAS                assertTrue("Invalid Character[" +i +"]=" +  e.getMessage(), e.getMessage().startsWith("Invalid Character["));
            }
        }
    }
}
