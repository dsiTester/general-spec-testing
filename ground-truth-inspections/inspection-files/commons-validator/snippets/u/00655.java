public abstract class ModulusCheckDigit implements CheckDigit, Serializable {
    /**
     * Validate a modulus check digit for a code.
     *
     * @param code The code to validate
     * @return <code>true</code> if the check digit is valid, otherwise
     * <code>false</code>
     */
    @Override
    public boolean isValid(String code) { // called from test
        if (code == null || code.isEmpty()) {
            return false;
        }
        try {
            int modulusResult = calculateModulus(code, true); // call to a
            return (modulusResult == 0);
        } catch (CheckDigitException  ex) {
            return false;
        }
    }

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
            int charValue = toInt(code.charAt(i), leftPos, rightPos); // call to b
            total += weightedValue(charValue, leftPos, rightPos);
        }
        if (total == 0) {
            throw new CheckDigitException("Invalid code, sum is zero");
        }
        return total % modulus;
    }

    /**
     * Convert a character at a specified position to an integer value.
     * <p>
     * <b>Note:</b> this implementation only handlers numeric values
     * For non-numeric characters, override this method to provide
     * character--&gt;integer conversion.
     *
     * @param character The character to convert
     * @param leftPos The position of the character in the code, counting from left to right (for identifiying the position in the string)
     * @param rightPos The position of the character in the code, counting from right to left (not used here)
     * @return The integer value of the character
     * @throws CheckDigitException if character is non-numeric
     */
    protected int toInt(char character, int leftPos, int rightPos)
            throws CheckDigitException { // definition of b
        if (Character.isDigit(character)) {
            return Character.getNumericValue(character);
        }
        throw new CheckDigitException("Invalid Character[" +
                leftPos + "] = '" + character + "'");
    }
}

public abstract class AbstractCheckDigitTest extends TestCase {
    /**
     * Test missing code
     */
    public void testMissingCode() {

        // isValid() null
        assertFalse("isValid() Null", routine.isValid(null));

        // isValid() zero length
        assertFalse("isValid() Zero Length", routine.isValid(""));

        // isValid() length 1
        // Don't use 0, because that passes for Verhoef (not sure why yet)
        assertFalse("isValid() Length 1", routine.isValid("9")); // calls a; assertion fails

        // calculate() null
        try {
            routine.calculate(null);
            fail("calculate() Null - expected exception");
        } catch (Exception e) {
            assertEquals("calculate() Null", missingMessage, e.getMessage());
        }

        // calculate() zero length
        try {
            routine.calculate("");
            fail("calculate() Zero Length - expected exception");
        } catch (Exception e) {
            assertEquals("calculate() Zero Length",  missingMessage, e.getMessage());
        }
    }

}

public class ABANumberCheckDigitTest extends AbstractCheckDigitTest {
    ...
}
