public class ISBNValidator implements Serializable {

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
    public String validate(String code) { // definition of a
        String result = validateISBN13(code);
        if (result == null) {
            result = validateISBN10(code);
            if (result != null && convert) {
                result = convertToISBN13(result);
            }
        }
        return result;
    }

    /**
     * Convert an ISBN-10 code to an ISBN-13 code.
     * <p>
     * This method requires a valid ISBN-10 with NO formatting
     * characters.
     *
     * @param isbn10 The ISBN-10 code to convert
     * @return A converted ISBN-13 code or <code>null</code>
     * if the ISBN-10 code is not valid
     */
    public String convertToISBN13(String isbn10) { // definition of b

        if (isbn10 == null) {
            return null;
        }

        String input = isbn10.trim();
        if (input.length() != ISBN_10_LEN) {
            throw new IllegalArgumentException("Invalid length " + input.length() + " for '" + input + "'");
        }

        // Calculate the new ISBN-13 code (drop the original checkdigit)
        String isbn13 = "978" + input.substring(0, ISBN_10_LEN - 1);
        try {
            String checkDigit = isbn13Validator.getCheckDigit().calculate(isbn13);
            isbn13 += checkDigit;
            return isbn13;
        } catch (CheckDigitException e) {
            throw new IllegalArgumentException("Check digit error for '" + input + "' - " + e.getMessage());
        }

    }

}

public class ISBNValidatorTest extends TestCase {
    /**
     * Test null values
     */
    public void testNull() {
        ISBNValidator validator = ISBNValidator.getInstance();
        assertFalse("isValid",        validator.isValid(null));
        assertFalse("isValidISBN10",  validator.isValidISBN10(null));
        assertFalse("isValidISBN13",  validator.isValidISBN13(null));
        assertNull("validate",        validator.validate(null)); // call to a
        assertNull("validateISBN10",  validator.validateISBN10(null));
        assertNull("validateISBN13",  validator.validateISBN13(null));
        assertNull("convertToISBN13", validator.convertToISBN13(null)); // call to b
    }
}
