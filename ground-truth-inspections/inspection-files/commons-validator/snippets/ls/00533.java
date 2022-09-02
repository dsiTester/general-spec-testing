public abstract class AbstractFormatValidator implements Serializable {

    /**
     * <p>Process the parsed value, performing any further validation
     *    and type conversion required.</p>
     *
     * @param value The parsed object created.
     * @param formatter The Format used to parse the value with.
     * @return The parsed value converted to the appropriate type
     *         if valid or <code>null</code> if invalid.
     */
    protected abstract Object processParsedValue(Object value, Format formatter); // a

    /**
     * <p>Format an object into a <code>String</code> using
     * the specified pattern.</p>
     *
     * @param value The value validation is being performed on.
     * @param pattern The pattern used to format the value.
     * @return The value formatted as a <code>String</code>.
     */
    public String format(Object value, String pattern) { // definition of b
        return format(value, pattern, (Locale)null);
    }

    protected Object parse(String value, Format formatter) { // called from AbstractCalendarValidator.parse()

        ParsePosition pos = new ParsePosition(0);
        Object parsedValue = formatter.parseObject(value, pos); // replacement value
        if (pos.getErrorIndex() > -1) {
            return null;
        }

        if (isStrict() && pos.getIndex() < value.length()) {
            return null;
        }

        if (parsedValue != null) {
            parsedValue = processParsedValue(parsedValue, formatter); // call to a
        }

        return parsedValue;

    }
}


public class DateValidator extends AbstractCalendarValidator {
    /**
     * <p>Returns the parsed <code>Date</code> unchanged.</p>
     *
     * @param value The parsed <code>Date</code> object created.
     * @param formatter The Format used to parse the value with.
     * @return The parsed value converted to a <code>Calendar</code>.
     */
    @Override
    protected Object processParsedValue(Object value, Format formatter) { // used implementation of a
        return value;
    }
}


public class DateValidatorTest extends AbstractCalendarValidatorTest {
    ...
}

public abstract class AbstractCalendarValidatorTest extends TestCase {
    /**
     * Test Invalid Dates with "locale" validation
     */
    public void testFormat() {

        // Create a Date or Calendar
        Object test = validator.parse("2005-11-28", "yyyy-MM-dd", null, null); // calls a
        assertNotNull("Test Date ", test); // assertion fails here
        assertEquals("Format pattern", "28.11.05", validator.format(test, "dd.MM.yy")); // call to b
        assertEquals("Format locale",  "11/28/05", validator.format(test, Locale.US));
    }

}
