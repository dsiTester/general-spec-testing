public class IBANValidator {

    /**
     * Validate an IBAN Code
     *
     * @param code The value validation is being performed on
     * @return <code>true</code> if the value is valid
     */
    public boolean isValid(String code) {               // definition of a
        Validator formatValidator = getValidator(code); // call to b
        if (formatValidator == null || code.length() != formatValidator.lengthOfIBAN || !formatValidator.validator.isValid(code)) {
            return false;
        }
        return IBANCheckDigit.IBAN_CHECK_DIGIT.isValid(code);
    }

    /**
     * Get the Validator for a given IBAN
     *
     * @param code a string starting with the ISO country code (e.g. an IBAN)
     *
     * @return the validator or {@code null} if there is not one registered.
     */
    public Validator getValidator(String code) { // called from b
        if (code == null || code.length() < 2) { // ensure we can extract the code
            return null;
        }
        String key = code.substring(0, 2);
        return formatValidators.get(key);
    }

}

public class IBANValidatorTest {

    @Test
    public void testNull() {
        assertFalse("isValid(null)",  VALIDATOR.isValid(null)); // call to a
    }
}
