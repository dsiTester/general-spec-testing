public class ISSNValidator implements Serializable {
    /**
     * Convert an ISSN code to an EAN-13 code.
     * <p>
     * This method requires a valid ISSN code.
     * It may contain a leading 'ISSN ' prefix,
     * as the input is passed through the {@link #validate(String)}
     * method.
     *
     * @param issn The ISSN code to convert
     * @param suffix the two digit suffix, e.g. "00"
     * @return A converted EAN-13 code or <code>null</code>
     * if the input ISSN code is not valid
     */
    public String convertToEAN13(String issn, String suffix) { // definition of a

        if (suffix == null || !suffix.matches("\\d\\d")) {
            throw new IllegalArgumentException("Suffix must be two digits: '" + suffix + "'");
        }

        Object result = validate(issn); // call to b
        if (result == null) {
            return null;
        }

        // Calculate the new EAN-13 code
        final String input = result.toString();
        String ean13 = ISSN_PREFIX + input.substring(0, input.length() -1) + suffix;
        try {
            String checkDigit = EAN13CheckDigit.EAN13_CHECK_DIGIT.calculate(ean13);
            ean13 += checkDigit;
            return ean13;
        } catch (CheckDigitException e) { // Should not happen
            throw new IllegalArgumentException("Check digit error for '" + ean13 + "' - " + e.getMessage());
        }

    }

    /**
     * Check the code is valid ISSN code.
     * <p>
     * If valid, this method returns the ISSN code with
     * the 'ISSN ' prefix removed (if it was present)
     *
     * @param code The code to validate.
     * @return A valid ISSN code if valid, otherwise <code>null</code>.
     */
    public Object validate(String code) { // definition of b
        return VALIDATOR.validate(code);
    }

}

public class ISSNValidatorTest extends TestCase {
    public void testIsValidISSNConvertNull() {
        assertNull(VALIDATOR.convertToEAN13(null, "00")); // call to a
    }

}
