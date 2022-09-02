public interface CheckDigit {
    /**
     * Validates the check digit for the code.
     *
     * @param code The code to validate, the string must include the check digit.
     * @return <code>true</code> if the check digit is valid, otherwise
     * <code>false</code>.
     */
    boolean isValid(String code); // a
}

public final class ModulusTenCheckDigit extends ModulusCheckDigit {
    /**
     * Validate a modulus check digit for a code.
     * <p>
     * Note: assumes last digit is the check digit
     *
     * @param code The code to validate
     * @return <code>true</code> if the check digit is valid, otherwise
     *         <code>false</code>
     */
    @Override
    public boolean isValid(String code) { // used implementation of a
        if (code == null || code.isEmpty()) {
            return false;
        }
        if (!Character.isDigit(code.charAt(code.length() - 1))) {
            return false;
        }

        return super.isValid(code); // call to b
    }
}

public abstract class ModulusCheckDigit implements CheckDigit, Serializable {
    /**
     * Validate a modulus check digit for a code.
     *
     * @param code The code to validate
     * @return <code>true</code> if the check digit is valid, otherwise
     * <code>false</code>
     */
    @Override
    public boolean isValid(String code) { // definition of b
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
}

public abstract class AbstractCheckDigitTest extends TestCase {
    /**
     * Test zero sum
     */
    public void testZeroSum() {

        assertFalse("isValid() Zero Sum", routine.isValid(zeroSum)); // call to a

        try {
            routine.calculate(zeroSum);
            fail("Zero Sum - expected exception");
        } catch (Exception e) {
            assertEquals("isValid() Zero Sum",  "Invalid code, sum is zero", e.getMessage());
        }

    }

}

public class ModulusTenABACheckDigitTest extends AbstractCheckDigitTest {

}
