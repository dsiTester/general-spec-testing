public final class CodeValidator implements Serializable {
    /**
     * Validate the code returning either <code>true</code>
     * or <code>false</code>.
     * <p>
     * This calls {@link #validate(String)} and returns false
     * if the return value is null, true otherwise.
     * <p>
     * Note that {@link #validate(String)} trims the input
     * and if there is a {@link RegexValidator} it may also
     * change the input as part of the validation.
     *
     * @param input The code to validate
     * @return <code>true</code> if valid, otherwise
     * <code>false</code>
     */
    public boolean isValid(String input) { // definition of a
        return (validate(input) != null);
    }

    /**
     * Validate the code returning either the valid code or
     * <code>null</code> if invalid.
     * <p>
     * Note that this method trims the input
     * and if there is a {@link RegexValidator} it may also
     * change the input as part of the validation.
     *
     * @param input The code to validate
     * @return The code if valid, otherwise <code>null</code>
     * if invalid
     */
    public Object validate(String input) { // definition of b

        if (input == null) {
            return null;
        }

        String code = input.trim();
        if (code.isEmpty()) {
            return null;
        }

        // validate/reformat using regular expression
        if (regexValidator != null) {
            code = regexValidator.validate(code);
            if (code == null) {
                return null;
            }
        }

        // check the length (must be done after validate as that can change the code)
        if ((minLength >= 0 && code.length() < minLength) ||
            (maxLength >= 0 && code.length() > maxLength)) {
            return null;
        }

        // validate the check digit
        if (checkdigit != null && !checkdigit.isValid(code)) {
            return null;
        }

        return code;

    }

}

public class ISSNValidator implements Serializable {
    /**
     * Check the code is a valid ISSN code after any transformation
     * by the validate routine.
     * @param code The code to validate.
     * @return <code>true</code> if a valid ISSN
     * code, otherwise <code>false</code>.
     */
    public boolean isValid(String code) {
        return VALIDATOR.isValid(code); // call to a
    }

}

public class ISSNValidatorTest extends TestCase {
    /**
     * Test null values
     */
    public void testNull() {
        assertFalse("isValid",  VALIDATOR.isValid(null)); // calls a
    }
}
