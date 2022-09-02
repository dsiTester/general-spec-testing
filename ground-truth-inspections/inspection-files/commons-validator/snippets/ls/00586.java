public class ISBNValidator implements Serializable {
    /**
     * Check the code is either a valid ISBN-10 or ISBN-13 code.
     *
     * @param code The code to validate.
     * @return <code>true</code> if a valid ISBN-10 or
     * ISBN-13 code, otherwise <code>false</code>.
     */
    public boolean isValid(String code) { // definition of a
        return (isValidISBN13(code) || isValidISBN10(code));
    }

    /**
     * Check the code is either a valid ISBN-10 or ISBN-13 code.
     * <p>
     * If valid, this method returns the ISBN code with
     * formatting characters removed (i.e. space or hyphen).
     * <p>
     * Converts an ISBN-10 codes to ISBN-13 if
     * <code>convertToISBN13</code> is <code>true</code>.
     *
     * @param code The code to validate.
     * @return A valid ISBN code if valid, otherwise <code>null</code>.
     */
    public String validate(String code) {
        String result = validateISBN13(code);
        if (result == null) {
            result = validateISBN10(code);
            if (result != null && convert) {
                result = convertToISBN13(result);
            }
        }
        return result;
    }
}

public class ISBNValidatorTest extends TestCase {
    /**
     * Test null values
     */
    public void testNull() {
        ISBNValidator validator = ISBNValidator.getInstance();
        assertFalse("isValid",        validator.isValid(null)); // call to a
        assertFalse("isValidISBN10",  validator.isValidISBN10(null));
        assertFalse("isValidISBN13",  validator.isValidISBN13(null));
        assertNull("validate",        validator.validate(null)); // call to b
        assertNull("validateISBN10",  validator.validateISBN10(null));
        assertNull("validateISBN13",  validator.validateISBN13(null));
        assertNull("convertToISBN13", validator.convertToISBN13(null));
    }
}
