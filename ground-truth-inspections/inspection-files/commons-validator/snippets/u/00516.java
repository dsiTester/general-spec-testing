public abstract class AbstractFormatValidator implements Serializable {
    /**
     * <p>Validate using the default <code>Locale</code>.
     *
     * @param value The value validation is being performed on.
     * @return <code>true</code> if the value is valid.
     */
    public boolean isValid(String value) { // definition of a
        return isValid(value, (String)null, (Locale)null);
    }

    /**
     * <p>Validate using the specified <code>Locale</code>.
     *
     * @param value The value validation is being performed on.
     * @param locale The locale to use for the Format, defaults to the default
     * @return <code>true</code> if the value is valid.
     */
    public boolean isValid(String value, Locale locale) { // definition of b
        return isValid(value, (String)null, locale);
    }

    /**
     * <p>Validate using the specified pattern and/or <code>Locale</code>.
     *
     * @param value The value validation is being performed on.
     * @param pattern The pattern used to format the value.
     * @param locale The locale to use for the Format, defaults to the default
     * @return <code>true</code> if the value is valid.
     */
    public abstract boolean isValid(String value, String pattern, Locale locale); // called from both a and b
}

public class CalendarValidatorTest extends AbstractCalendarValidatorTest {
    public void testDateTimeStyle() {
        // Set the default Locale
        Locale origDefault = Locale.getDefault();
        Locale.setDefault(Locale.UK);

        AbstractCalendarValidator dateTimeValidator =
            new AbstractCalendarValidator(true, DateFormat.SHORT, DateFormat.SHORT) {
                private static final long serialVersionUID = 1L;

            @Override
            protected Object processParsedValue(Object value, Format formatter) {
                return value;
            }
        };
        assertTrue("validate(A) default", dateTimeValidator.isValid("31/12/05 14:23")); // call to a
        assertTrue("validate(A) locale ", dateTimeValidator.isValid("12/31/05 2:23 PM", Locale.US)); // call to b

        // Restore the original default
        Locale.setDefault(origDefault);
    }

}
