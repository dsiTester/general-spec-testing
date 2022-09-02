public abstract class ModulusCheckDigit implements CheckDigit, Serializable {
    /**
     * Validate a modulus check digit for a code.
     *
     * @param code The code to validate
     * @return <code>true</code> if the check digit is valid, otherwise
     * <code>false</code>
     */
    @Override
    public boolean isValid(String code) { // definition of a
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
}

public interface CheckDigit {

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

public final class ModulusTenCheckDigit extends ModulusCheckDigit {
    @Override
    public boolean isValid(String code) { // called from test
        if (code == null || code.isEmpty()) {
            return false;
        }
        if (!Character.isDigit(code.charAt(code.length() - 1))) {
            return false;
        }

        return super.isValid(code); // call to a
    }
}

public abstract class AbstractCheckDigitTest extends TestCase {
    public void testZeroSum() {

        assertFalse("isValid() Zero Sum", routine.isValid(zeroSum)); // calls a

        try {
            routine.calculate(zeroSum); // call to b; throws expected exception
            fail("Zero Sum - expected exception");
        } catch (Exception e) {
            assertEquals("isValid() Zero Sum",  "Invalid code, sum is zero", e.getMessage());
        }

    }

}

public class ModulusTenABACheckDigitTest extends AbstractCheckDigitTest {

}
