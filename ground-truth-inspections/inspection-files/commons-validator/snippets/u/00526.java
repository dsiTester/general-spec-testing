public abstract class AbstractCalendarValidator extends AbstractFormatValidator {
    protected Object parse(String value, String pattern, Locale locale, TimeZone timeZone) { // called from test

        value = (value == null ? null : value.trim());
        if (value == null || value.isEmpty()) {
            return null;
        }
        DateFormat formatter = (DateFormat)getFormat(pattern, locale);
        if (timeZone != null) {
            formatter.setTimeZone(timeZone);
        }
        return parse(value, formatter); // call to a

    }

}

public abstract class AbstractFormatValidator implements Serializable {
    /**
     * <p>Parse the value with the specified <code>Format</code>.</p>
     *
     * @param value The value to be parsed.
     * @param formatter The Format to parse the value with.
     * @return The parsed value if valid or <code>null</code> if invalid.
     */
    protected Object parse(String value, Format formatter) { // definition of a

        ParsePosition pos = new ParsePosition(0);
        Object parsedValue = formatter.parseObject(value, pos);
        if (pos.getErrorIndex() > -1) {
            return null;
        }

        if (isStrict() && pos.getIndex() < value.length()) { // call to b
            return null;
        }

        if (parsedValue != null) {
            parsedValue = processParsedValue(parsedValue, formatter);
        }

        return parsedValue;

    }

    /**
     * <p>Indicates whether validated values should adhere
     *    strictly to the <code>Format</code> used.</p>
     *
     * <p>Typically implementations of <code>Format</code>
     *    ignore invalid characters at the end of the value
     *    and just stop parsing. For example parsing a date
     *    value of <code>01/01/20x0</code> using a pattern
     *    of <code>dd/MM/yyyy</code> will result in a year
     *    of <code>20</code> if <code>strict</code> is set
     *    to <code>false</code>, whereas setting <code>strict</code>
     *    to <code>true</code> will cause this value to fail
     *    validation.</p>
     *
     * @return <code>true</code> if strict <code>Format</code>
     *         parsing should be used.
     */
    public boolean isStrict() { // definition of b
        return strict;
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
        assertEquals("Format pattern", "28.11.05", validator.format(test, "dd.MM.yy"));
        assertEquals("Format locale",  "11/28/05", validator.format(test, Locale.US));
    }

}
