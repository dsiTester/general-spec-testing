public interface CheckDigit {

    /**
     * Calculates the <i>Check Digit</i> for a code.
     *
     * @param code The code to calculate the Check Digit for.
     * The string must not include the check digit
     * @return The calculated Check Digit
     * @throws CheckDigitException if an error occurs.
     */
    String calculate(String code) throws CheckDigitException; // a
}

public abstract class ModulusCheckDigit implements CheckDigit, Serializable {
    /**
     * Calculate a modulus <i>Check Digit</i> for a code which does not yet have one.
     *
     * @param code The code for which to calculate the Check Digit;
     * the check digit should not be included
     * @return The calculated Check Digit
     * @throws CheckDigitException if an error occurs calculating the check digit
     */
    @Override
    public String calculate(String code) throws CheckDigitException { // used implementation of a
        if (code == null || code.isEmpty()) {
            throw new CheckDigitException("Code is missing");
        }
        int modulusResult = calculateModulus(code, false);
        int charValue = (modulus - modulusResult) % modulus;
        return toCheckDigit(charValue); // call to b
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
}

public abstract class AbstractCheckDigitTest extends TestCase {

    public void testCalculateInvalid() { // ABANumberCheckDigitTest extends AbstractCheckDigitTest, and does not override this method

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
                String actual = routine.calculate(removeCheckDigit(code)); // call to a
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

public class ABANumberCheckDigitTest extends AbstractCheckDigitTest {
    ...
}
