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
     * Installs a validator.
     * Will replace any existing entry which has the same countryCode.
     *
     * @param countryCode the country code
     * @param length the length of the IBAN. Must be &ge; 8 and &le; 32.
     * If the length is &lt; 0, the validator is removed, and the format is not used.
     * @param format the format of the IBAN (as a regular expression)
     * @return the previous Validator, or {@code null} if there was none
     * @throws IllegalArgumentException if there is a problem
     * @throws IllegalStateException if an attempt is made to modify the singleton validator
     */
    public Validator setValidator(String countryCode, int length, String format) { // definition of b
        if (this == DEFAULT_IBAN_VALIDATOR) {
            throw new IllegalStateException("The singleton validator cannot be modified"); // throws exception here
        }
        if (length < 0) {
            return formatValidators.remove(countryCode);
        }
        return setValidator(new Validator(countryCode, length, format));
    }

}

public class IBANValidatorTest {
    private static final IBANValidator VALIDATOR = IBANValidator.getInstance(); // calls a

    @Test(expected=IllegalStateException.class)
    public void testSetDefaultValidator2() {
        assertNotNull(VALIDATOR.setValidator("GB", -1, "GB"));
    }

}
