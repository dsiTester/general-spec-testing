public abstract class ModulusCheckDigit implements CheckDigit, Serializable {
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
            throws CheckDigitException { // definition of a
        if (Character.isDigit(character)) {
            return Character.getNumericValue(character);
        }
        throw new CheckDigitException("Invalid Character[" +
                leftPos + "] = '" + character + "'");
    }

    /**
     * Calculates the <i>weighted</i> value of a character in the
     * code at a specified position.
     * <p>
     * Some modulus routines weight the value of a character
     * depending on its position in the code (e.g. ISBN-10), while
     * others use different weighting factors for odd/even positions
     * (e.g. EAN or Luhn). Implement the appropriate mechanism
     * required by overriding this method.
     *
     * @param charValue The numeric value of the character
     * @param leftPos The position of the character in the code, counting from left to right
     * @param rightPos The positionof the character in the code, counting from right to left
     * @return The weighted value of the character
     * @throws CheckDigitException if an error occurs calculating
     * the weighted value
     */
    protected abstract int weightedValue(int charValue, int leftPos, int rightPos)
            throws CheckDigitException; // b

    @Override
    public boolean isValid(String code) { // called from ModulusTenCheckDigit.isValid()
        if (code == null || code.isEmpty()) {
            return false;
        }
        try {
            int modulusResult = calculateModulus(code, true);
            return (modulusResult == 0); // the replacement value for a still causes this conditional to be true
        } catch (CheckDigitException  ex) {
            return false;
        }
    }

    protected int calculateModulus(String code, boolean includesCheckDigit) throws CheckDigitException { // called from above
        int total = 0;
        for (int i = 0; i < code.length(); i++) {
            int lth = code.length() + (includesCheckDigit ? 0 : 1);
            int leftPos  = i + 1;
            int rightPos = lth - i;
            int charValue = toInt(code.charAt(i), leftPos, rightPos); // call to a
            total += weightedValue(charValue, leftPos, rightPos); // call to b
        }
        if (total == 0) {
            throw new CheckDigitException("Invalid code, sum is zero");
        }
        return total % modulus;
    }

}

public final class ModulusTenCheckDigit extends ModulusCheckDigit {

    /**
     * Calculates the <i>weighted</i> value of a character in the code at a
     * specified position.
     *
     * @param charValue The numeric value of the character.
     * @param leftPos The position of the character in the code, counting from
     *            left to right
     * @param rightPos The position of the character in the code, counting from
     *            right to left
     * @return The weighted value of the character.
     */
    @Override
    protected int weightedValue(int charValue, int leftPos, int rightPos) { // used implementation of method-b
        int pos = useRightPos ? rightPos : leftPos;
        int weight = postitionWeight[(pos - 1) % postitionWeight.length];
        int weightedValue = charValue * weight;
        if (sumWeightedDigits) {
            weightedValue = ModulusCheckDigit.sumDigits(weightedValue);
        }
        return weightedValue;
    }

}

public class ModulusTenABACheckDigitTest extends AbstractCheckDigitTest {
    public void testMissingCode() {

        // isValid() null
        assertFalse("isValid() Null", routine.isValid(null));

        // isValid() zero length
        assertFalse("isValid() Zero Length", routine.isValid(""));

        // isValid() length 1
        // Don't use 0, because that passes for Verhoef (not sure why yet)
        assertFalse("isValid() Length 1", routine.isValid("9")); // calls a and b

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
