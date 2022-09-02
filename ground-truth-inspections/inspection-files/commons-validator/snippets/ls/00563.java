public class DateValidator extends AbstractCalendarValidator {
    /**
     * <p>Validate/convert a <code>Date</code> using the specified
     *    <code>Locale</code> and <code>TimeZone</code>.
     *
     * @param value The value validation is being performed on.
     * @param locale The locale to use for the date format, system default if null.
     * @param timeZone The Time Zone used to parse the date, system default if null.
     * @return The parsed <code>Date</code> if valid or <code>null</code> if invalid.
     */
    public Date validate(String value, Locale locale, TimeZone timeZone) { // definition of a
        return (Date)parse(value, (String)null, locale, timeZone);
    }

    /**
     * <p>Validate/convert a <code>Date</code> using the specified
     *    <i>pattern</i> and <code>TimeZone</code>.
     *
     * @param value The value validation is being performed on.
     * @param pattern The pattern used to validate the value against, or the
     *        default for the <code>Locale</code> if <code>null</code>.
     * @param timeZone The Time Zone used to parse the date, system default if null.
     * @return The parsed <code>Date</code> if valid or <code>null</code> if invalid.
     */
    public Date validate(String value, String pattern, TimeZone timeZone) { // definition of b
        return (Date)parse(value, pattern, (Locale)null, timeZone);
    }

}

public abstract class AbstractCalendarValidator extends AbstractFormatValidator {
    /**
     * <p>Checks if the value is valid against a specified pattern.</p>
     *
     * @param value The value validation is being performed on.
     * @param pattern The pattern used to validate the value against, or the
     *        default for the <code>Locale</code> if <code>null</code>.
     * @param locale The locale to use for the date format, system default if null.
     * @param timeZone The Time Zone used to parse the date, system default if null.
     * @return The parsed value if valid or <code>null</code> if invalid.
     */
    protected Object parse(String value, String pattern, Locale locale, TimeZone timeZone) { // called from both a and b

        value = (value == null ? null : value.trim());
        if (value == null || value.isEmpty()) {
            return null;
        }
        DateFormat formatter = (DateFormat)getFormat(pattern, locale);
        if (timeZone != null) {
            formatter.setTimeZone(timeZone);
        }
        return parse(value, formatter);

    }

}

public class DateValidatorTest extends AbstractCalendarValidatorTest {
    /**
     * Test DateValidator validate Methods
     */
    public void testDateValidatorMethods() {
        Locale.setDefault(Locale.US);
        Locale locale     = Locale.GERMAN;
        String pattern    = "yyyy-MM-dd";
        String patternVal = "2005-12-31";
        String germanVal     = "31 Dez 2005";
        String germanPattern = "dd MMM yyyy";
        String localeVal  = "31.12.2005";
        String defaultVal = "12/31/05";
        String XXXX    = "XXXX";
        Date expected = createCalendar(null, 20051231, 0).getTime();

        assertEquals("validate(A) default", expected, DateValidator.getInstance().validate(defaultVal));
        assertEquals("validate(A) locale ", expected, DateValidator.getInstance().validate(localeVal, locale));
        assertEquals("validate(A) pattern", expected, DateValidator.getInstance().validate(patternVal, pattern));
        assertEquals("validate(A) both",    expected, DateValidator.getInstance().validate(germanVal, germanPattern, Locale.GERMAN));

        assertTrue("isValid(A) default", DateValidator.getInstance().isValid(defaultVal));
        assertTrue("isValid(A) locale ", DateValidator.getInstance().isValid(localeVal, locale));
        assertTrue("isValid(A) pattern", DateValidator.getInstance().isValid(patternVal, pattern));
        assertTrue("isValid(A) both",    DateValidator.getInstance().isValid(germanVal, germanPattern, Locale.GERMAN));

        assertNull("validate(B) default", DateValidator.getInstance().validate(XXXX));
        assertNull("validate(B) locale ", DateValidator.getInstance().validate(XXXX, locale));
        assertNull("validate(B) pattern", DateValidator.getInstance().validate(XXXX, pattern));
        assertNull("validate(B) both",    DateValidator.getInstance().validate("31 Dec 2005", germanPattern, Locale.GERMAN));

        assertFalse("isValid(B) default", DateValidator.getInstance().isValid(XXXX));
        assertFalse("isValid(B) locale ", DateValidator.getInstance().isValid(XXXX, locale));
        assertFalse("isValid(B) pattern", DateValidator.getInstance().isValid(XXXX, pattern));
        assertFalse("isValid(B) both",    DateValidator.getInstance().isValid("31 Dec 2005", germanPattern, Locale.GERMAN));

        // Test Time Zone
        TimeZone zone = (TimeZone.getDefault().getRawOffset() == EET.getRawOffset() ? EST : EET);
        Date expectedZone = createCalendar(zone, 20051231, 0).getTime();
        assertFalse("default/zone same "+zone, expected.getTime() == expectedZone.getTime());

        assertEquals("validate(C) default", expectedZone, DateValidator.getInstance().validate(defaultVal, zone));
        assertEquals("validate(C) locale ", expectedZone, DateValidator.getInstance().validate(localeVal, locale, zone)); // call to a
        assertEquals("validate(C) pattern", expectedZone, DateValidator.getInstance().validate(patternVal, pattern, zone)); // call to b
        assertEquals("validate(C) both",    expectedZone, DateValidator.getInstance().validate(germanVal, germanPattern, Locale.GERMAN, zone));
    }
}
