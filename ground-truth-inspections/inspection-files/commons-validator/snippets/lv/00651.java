public interface CheckDigit {
    /**
     * Validates the check digit for the code.
     *
     * @param code The code to validate, the string must include the check digit.
     * @return <code>true</code> if the check digit is valid, otherwise
     * <code>false</code>.
     */
    boolean isValid(String code); // a

    /**
     * Calculates the <i>Check Digit</i> for a code.
     *
     * @param code The code to calculate the Check Digit for.
     * The string must not include the check digit
     * @return The calculated Check Digit
     * @throws CheckDigitException if an error occurs.
     */
    String calculate(String code) throws CheckDigitException; // b
}

public abstract class ModulusCheckDigit implements CheckDigit, Serializable { // ABANumberCheckDigit extends ModulusCheckDigit and does not define either of these methods.

    /**
     * Validate a modulus check digit for a code.
     *
     * @param code The code to validate
     * @return <code>true</code> if the check digit is valid, otherwise
     * <code>false</code>
     */
    @Override
    public boolean isValid(String code) { // used implementation of a
        if (code == null || code.isEmpty()) {
            return false;
        }
        try {
            int modulusResult = calculateModulus(code, true);
            return (modulusResult == 0);
        } catch (CheckDigitException  ex) {
            return false;
        }
    }

    /**
     * Calculate a modulus <i>Check Digit</i> for a code which does not yet have one.
     *
     * @param code The code for which to calculate the Check Digit;
     * the check digit should not be included
     * @return The calculated Check Digit
     * @throws CheckDigitException if an error occurs calculating the check digit
     */
    @Override
    public String calculate(String code) throws CheckDigitException { // used implementation of b
        if (code == null || code.isEmpty()) {
            throw new CheckDigitException("Code is missing");
        }
        int modulusResult = calculateModulus(code, false);
        int charValue = (modulus - modulusResult) % modulus;
        return toCheckDigit(charValue);
    }

    protected int calculateModulus(String code, boolean includesCheckDigit) throws CheckDigitException { // called from b
        int total = 0;
        for (int i = 0; i < code.length(); i++) {
            int lth = code.length() + (includesCheckDigit ? 0 : 1);
            int leftPos  = i + 1;
            int rightPos = lth - i;
            int charValue = toInt(code.charAt(i), leftPos, rightPos);
            total += weightedValue(charValue, leftPos, rightPos);
        }
        if (total == 0) {
            throw new CheckDigitException("Invalid code, sum is zero"); // throws expected exception
        }
        return total % modulus;
    }
}

public abstract class AbstractCheckDigitTest extends TestCase {
    protected String zeroSum = "0000000000";

    /**
     * Test zero sum
     */
    public void testZeroSum() {

        assertFalse("isValid() Zero Sum", routine.isValid(zeroSum)); // call to a

        try {
            routine.calculate(zeroSum); // call to b
            fail("Zero Sum - expected exception");
        } catch (Exception e) {
            assertEquals("isValid() Zero Sum",  "Invalid code, sum is zero", e.getMessage());
        }

    }

}

public class ABANumberCheckDigitTest extends AbstractCheckDigitTest {
    ...
}
