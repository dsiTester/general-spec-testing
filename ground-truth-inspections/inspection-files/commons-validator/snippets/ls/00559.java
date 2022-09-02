public final class CodeValidator implements Serializable {
    /**
     * Return the <i>regular expression</i> validator.
     * <p>
     * <b>N.B.</b> Optional, if not set no regular
     * expression validation will be performed on the code.
     *
     * @return The regular expression validator
     */
    public RegexValidator getRegexValidator() { // definition of a
        return regexValidator;
    }

    /**
     * Return the minimum length of the code.
     * <p>
     * <b>N.B.</b> Optional, if less than zero the
     * minimum length will not be checked.
     *
     * @return The minimum length of the code or
     * <code>-1</code> if the code has no minimum length
     */
    public int getMinLength() { // definition of b
        return minLength;
    }
}

public class CodeValidatorTest extends TestCase {
    /**
     * Test Regular Expression.
     */
    public void testConstructors() {
        CodeValidator validator;
        RegexValidator regex = new RegexValidator("^[0-9]*$");

        // Constructor 1
        validator = new CodeValidator(regex, EAN13CheckDigit.EAN13_CHECK_DIGIT);
        assertEquals("Constructor 1 - regex",      regex, validator.getRegexValidator()); // call to a
        assertEquals("Constructor 1 - min length", -1, validator.getMinLength()); // call to b
        assertEquals("Constructor 1 - max length", -1, validator.getMaxLength());
        assertEquals("Constructor 1 - check digit", EAN13CheckDigit.EAN13_CHECK_DIGIT, validator.getCheckDigit());

        // Constructor 2
        validator = new CodeValidator(regex, 13, EAN13CheckDigit.EAN13_CHECK_DIGIT);
        assertEquals("Constructor 2 - regex",      regex, validator.getRegexValidator()); // call to a
        assertEquals("Constructor 2 - min length", 13, validator.getMinLength()); // call to b
        assertEquals("Constructor 2 - max length", 13, validator.getMaxLength());
        assertEquals("Constructor 2 - check digit", EAN13CheckDigit.EAN13_CHECK_DIGIT, validator.getCheckDigit());

        // Constructor 3
        validator = new CodeValidator(regex, 10, 20, EAN13CheckDigit.EAN13_CHECK_DIGIT);
        assertEquals("Constructor 3 - regex",      regex, validator.getRegexValidator()); // call to a
        assertEquals("Constructor 3 - min length", 10, validator.getMinLength()); // call to b
        assertEquals("Constructor 3 - max length", 20, validator.getMaxLength());
        assertEquals("Constructor 3 - check digit", EAN13CheckDigit.EAN13_CHECK_DIGIT, validator.getCheckDigit());

        // constructor 4
        validator = new CodeValidator("^[0-9]*$", EAN13CheckDigit.EAN13_CHECK_DIGIT);
        assertEquals("Constructor 4 - regex",      "RegexValidator{^[0-9]*$}", validator.getRegexValidator().toString()); // call to a
        assertEquals("Constructor 4 - min length", -1, validator.getMinLength()); // call to b
        assertEquals("Constructor 4 - max length", -1, validator.getMaxLength());
        assertEquals("Constructor 4 - check digit", EAN13CheckDigit.EAN13_CHECK_DIGIT, validator.getCheckDigit());

        // Constructor 5
        validator = new CodeValidator("^[0-9]*$", 13, EAN13CheckDigit.EAN13_CHECK_DIGIT);
        assertEquals("Constructor 5 - regex",      "RegexValidator{^[0-9]*$}", validator.getRegexValidator().toString()); // call to a
        assertEquals("Constructor 5 - min length", 13, validator.getMinLength()); // call to b
        assertEquals("Constructor 5 - max length", 13, validator.getMaxLength());
        assertEquals("Constructor 5 - check digit", EAN13CheckDigit.EAN13_CHECK_DIGIT, validator.getCheckDigit());

        // Constructor 6
        validator = new CodeValidator("^[0-9]*$", 10, 20, EAN13CheckDigit.EAN13_CHECK_DIGIT);
        assertEquals("Constructor 6 - regex",      "RegexValidator{^[0-9]*$}", validator.getRegexValidator().toString()); // call to a
        assertEquals("Constructor 6 - min length", 10, validator.getMinLength()); // call to b
        assertEquals("Constructor 6 - max length", 20, validator.getMaxLength());
        assertEquals("Constructor 6 - check digit", EAN13CheckDigit.EAN13_CHECK_DIGIT, validator.getCheckDigit());
    }

}
