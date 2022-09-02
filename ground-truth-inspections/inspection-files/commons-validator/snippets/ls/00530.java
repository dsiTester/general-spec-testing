public abstract class AbstractFormatValidator implements Serializable {
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
    public boolean isStrict() { // definition of a
        return strict;
    }

    protected Object parse(String value, Format formatter) { // called from AbstractCalendarValidator.parse()

        ParsePosition pos = new ParsePosition(0);
        Object parsedValue = formatter.parseObject(value, pos);
        if (pos.getErrorIndex() > -1) {
            return null;
        }

        if (isStrict() && pos.getIndex() < value.length()) { // call to a
            return null;
        }

        if (parsedValue != null) {
            parsedValue = processParsedValue(parsedValue, formatter);
        }

        return parsedValue;

    }

}

public abstract class AbstractCalendarValidator extends AbstractFormatValidator {
    /**
     * <p>Returns a <code>DateFormat</code> for the specified Locale.</p>
     *
     * @param locale The locale a <code>DateFormat</code> is required for,
     *        system default if null.
     * @return The <code>DateFormat</code> to created.
     */
    protected Format getFormat(Locale locale) { // definition of b

        DateFormat formatter = null;
        if (dateStyle >= 0 && timeStyle >= 0) {
            if (locale == null) {
                formatter = DateFormat.getDateTimeInstance(dateStyle, timeStyle);
            } else {
                formatter = DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale);
            }
        } else if (timeStyle >= 0) {
            if (locale == null) {
                formatter = DateFormat.getTimeInstance(timeStyle);
            } else {
                formatter = DateFormat.getTimeInstance(timeStyle, locale);
            }
        } else {
            int useDateStyle = dateStyle >= 0 ? dateStyle : DateFormat.SHORT;
            if (locale == null) {
                formatter = DateFormat.getDateInstance(useDateStyle);
            } else {
                formatter = DateFormat.getDateInstance(useDateStyle, locale);
            }
        }
        formatter.setLenient(false);
        return formatter;

    }

    @Override
    protected Format getFormat(String pattern, Locale locale) { // called from AbstractCalendarValidator.format(Object, String, Locale)
        DateFormat formatter = null;
        boolean usePattern = pattern != null && !pattern.isEmpty();
        if (!usePattern) {
            formatter = (DateFormat)getFormat(locale); // call to b
        } else if (locale == null) {
            formatter = new SimpleDateFormat(pattern);
        } else {
            DateFormatSymbols symbols = new DateFormatSymbols(locale);
            formatter = new SimpleDateFormat(pattern, symbols);
        }
        formatter.setLenient(false);
        return formatter;
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
        assertEquals("Format locale",  "11/28/05", validator.format(test, Locale.US)); // calls b
    }

}
