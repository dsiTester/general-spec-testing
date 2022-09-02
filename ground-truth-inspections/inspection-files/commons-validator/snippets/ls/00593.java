public class RegexValidator implements Serializable {
    /**
     * Validate a value against the set of regular expressions.
     *
     * @param value The value to validate.
     * @return <code>true</code> if the value is valid
     * otherwise <code>false</code>.
     */
    public boolean isValid(String value) { // definition of a
        if (value == null) {
            return false;
        }
        for (Pattern pattern : patterns) {
            if (pattern.matcher(value).matches()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validate a value against the set of regular expressions
     * returning the array of matched groups.
     *
     * @param value The value to validate.
     * @return String array of the <i>groups</i> matched if
     * valid or <code>null</code> if invalid
     */
    public String[] match(String value) { // definition of b
        if (value == null) {
            return null;
        }
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(value);
            if (matcher.matches()) {
                int count = matcher.groupCount();
                String[] groups = new String[count];
                for (int j = 0; j < count; j++) {
                    groups[j] = matcher.group(j+1);
                }
                return groups;
            }
        }
        return null;
    }

}

public class RegexValidatorTest extends TestCase {
    /**
     * Test Null value
     */
    public void testNullValue() {

        RegexValidator validator = new RegexValidator(REGEX);
        assertEquals("Instance isValid()",  false, validator.isValid(null)); // call to a
        assertEquals("Instance validate()", null,  validator.validate(null));
        assertEquals("Instance match()",    null,  validator.match(null)); // call to b
    }

}
