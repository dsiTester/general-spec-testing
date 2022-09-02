public class IBANValidator {

    /** The singleton instance which uses the default formats */
    public static final IBANValidator DEFAULT_IBAN_VALIDATOR = new IBANValidator();

    public IBANValidator(Validator[] formatMap) { // called from test
        this.formatValidators = createValidators(formatMap);
    }

    private Map<String, Validator> createValidators(Validator[] formatMap) { // definition of a
        Map<String, Validator> m = new ConcurrentHashMap<>();
        for(Validator v : formatMap) {
            m.put(v.countryCode, v);
        }
        return m;
    }

    /**
     * Validate an IBAN Code
     *
     * @param code The value validation is being performed on
     * @return <code>true</code> if the value is valid
     */
    public boolean isValid(String code) {               // definition of b
        Validator formatValidator = getValidator(code);
        if (formatValidator == null || code.length() != formatValidator.lengthOfIBAN || !formatValidator.validator.isValid(code)) {
            return false;
        }
        return IBANCheckDigit.IBAN_CHECK_DIGIT.isValid(code);
    }

    public Validator getValidator(String code) { // called from b
        if (code == null || code.length() < 2) { // ensure we can extract the code
            return null;
        }
        String key = code.substring(0, 2);
        return formatValidators.get(key);
    }

}

public class IBANValidatorTest {
    private static final IBANValidator VALIDATOR = IBANValidator.getInstance(); // calls a

    @Test
    public void testNull() {
        assertFalse("isValid(null)",  VALIDATOR.isValid(null)); // call to b
    }
}
