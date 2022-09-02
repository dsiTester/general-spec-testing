public class IBANValidator {

    private Map<String, Validator> createValidators(Validator[] formatMap) { // definition of a
        Map<String, Validator> m = new ConcurrentHashMap<>();
        for(Validator v : formatMap) {
            m.put(v.countryCode, v);
        }
        return m;
    }

    /**
     * Get the Validator for a given IBAN
     *
     * @param code a string starting with the ISO country code (e.g. an IBAN)
     *
     * @return the validator or {@code null} if there is not one registered.
     */
    public Validator getValidator(String code) { // definition of b
        if (code == null || code.length() < 2) { // ensure we can extract the code
            return null;
        }
        String key = code.substring(0, 2);
        return formatValidators.get(key);
    }

    /** The singleton instance which uses the default formats */
    public static final IBANValidator DEFAULT_IBAN_VALIDATOR = new IBANValidator();

    public IBANValidator(Validator[] formatMap) { // called from test
        this.formatValidators = createValidators(formatMap);
    }

    public boolean isValid(String code) {               // called from test
        Validator formatValidator = getValidator(code); // call to b
        if (formatValidator == null || code.length() != formatValidator.lengthOfIBAN || !formatValidator.validator.isValid(code)) {
            return false;
        }
        return IBANCheckDigit.IBAN_CHECK_DIGIT.isValid(code);
    }
}

public class IBANValidatorTest {
    private static final IBANValidator VALIDATOR = IBANValidator.getInstance();

    @Test
    public void testNull() {
        assertFalse("isValid(null)",  VALIDATOR.isValid(null));
    }
}
