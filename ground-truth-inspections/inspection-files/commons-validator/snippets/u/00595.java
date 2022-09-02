public class RegexValidator implements Serializable {
    /**
     * Validate a value against the set of regular expressions
     * returning a String value of the aggregated groups.
     *
     * @param value The value to validate.
     * @return Aggregated String value comprised of the
     * <i>groups</i> matched if valid or <code>null</code> if invalid
     */
    public String validate(String value) { // definition of a
        if (value == null) {
            return null;
        }
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(value);
            if (matcher.matches()) {
                int count = matcher.groupCount();
                if (count == 1) {
                    return matcher.group(1);
                }
                StringBuilder buffer = new StringBuilder();
                for (int j = 0; j < count; j++) {
                    String component = matcher.group(j+1);
                    if (component != null) {
                        buffer.append(component);
                    }
                }
                return buffer.toString();
            }
        }
        return null;
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
        assertEquals("Instance validate()", null,  validator.validate(null)); // call to b
        assertEquals("Instance match()",    null,  validator.match(null));
    }

}
