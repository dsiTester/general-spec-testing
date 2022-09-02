public abstract class ModulusCheckDigit implements CheckDigit, Serializable {
    /**
     * Calculate the modulus for a code.
     *
     * @param code The code to calculate the modulus for.
     * @param includesCheckDigit Whether the code includes the Check Digit or not.
     * @return The modulus value
     * @throws CheckDigitException if an error occurs calculating the modulus
     * for the specified code
     */
    protected int calculateModulus(String code, boolean includesCheckDigit) throws CheckDigitException { // definition of a
        int total = 0;
        for (int i = 0; i < code.length(); i++) {
            int lth = code.length() + (includesCheckDigit ? 0 : 1);
            int leftPos  = i + 1;
            int rightPos = lth - i;
            int charValue = toInt(code.charAt(i), leftPos, rightPos);
            total += weightedValue(charValue, leftPos, rightPos);
        }
        if (total == 0) {
            throw new CheckDigitException("Invalid code, sum is zero");
        }
        return total % modulus;
    }

    /**
     * Convert an integer value to a check digit.
     * <p>
     * <b>Note:</b> this implementation only handles single-digit numeric values
     * For non-numeric characters, override this method to provide
     * integer--&gt;character conversion.
     *
     * @param charValue The integer value of the character
     * @return The converted character
     * @throws CheckDigitException if integer character value
     * doesn't represent a numeric character
     */
    protected String toCheckDigit(int charValue)
            throws CheckDigitException { // definition of b
        if (charValue >= 0 && charValue <= 9) { // CHECKSTYLE IGNORE MagicNumber
            return Integer.toString(charValue);
        }
        throw new CheckDigitException("Invalid Check Digit Value =" +
                + charValue);
    }

    @Override
    public String calculate(String code) throws CheckDigitException { // called from test
        if (code == null || code.isEmpty()) {
            throw new CheckDigitException("Code is missing");
        }
        int modulusResult = calculateModulus(code, false); // call to a
        int charValue = (modulus - modulusResult) % modulus;
        return toCheckDigit(charValue); // call to b
    }

}

public abstract class AbstractCheckDigitTest extends TestCase {
    /**
     * Test calculate() for invalid values.
     */
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
                String expected = checkDigit(code);
                String actual = routine.calculate(removeCheckDigit(code)); // calls a and b
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
