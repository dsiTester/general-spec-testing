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

public final class VerhoeffCheckDigit implements CheckDigit, Serializable {
    /**
     * Calculate a Verhoeff <i>Check Digit</i> for a code.
     *
     * @param code The code to calculate the Check Digit for
     * @return The calculated Check Digit
     * @throws CheckDigitException if an error occurs calculating
     * the check digit for the specified code
     */
    @Override
    public String calculate(String code) throws CheckDigitException {
        if (code == null || code.isEmpty()) {
            throw new CheckDigitException("Code is missing");
        }
        int checksum = calculateChecksum(code, false); // call to b
        return Integer.toString(INV_TABLE[checksum]);
    }

    /**
     * Calculate the checksum.
     *
     * @param code The code to calculate the checksum for.
     * @param includesCheckDigit Whether the code includes the Check Digit or not.
     * @return The checksum value
     * @throws CheckDigitException if the code contains an invalid character (i.e. not numeric)
     */
    private int calculateChecksum(String code, boolean includesCheckDigit) throws CheckDigitException { // definition of b
        int checksum = 0;
        for (int i = 0; i < code.length(); i++) {
            int idx = code.length() - (i + 1);
            int num = Character.getNumericValue(code.charAt(idx));
            if (num < 0 || num > 9) { // CHECKSTYLE IGNORE MagicNumber
                throw new CheckDigitException("Invalid Character[" +
                        i + "] = '" + ((int)code.charAt(idx)) + "'");
            }
            int pos = includesCheckDigit ? i : i + 1;
            checksum = D_TABLE[checksum][P_TABLE[pos % 8][num]]; // CHECKSTYLE IGNORE MagicNumber
        }
        return checksum;
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
