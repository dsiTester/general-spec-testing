public class EmailValidator implements Serializable {
    /**
     * <p>Checks if a field has a valid e-mail address.</p>
     *
     * @param email The value validation is being performed on.  A <code>null</code>
     *              value is considered invalid.
     * @return true if the email address is valid.
     */
    public boolean isValid(String email) { // definition of a
        if (email == null) {
            return false;
        }

        if (email.endsWith(".")) { // check this first - it's cheap!
            return false;
        }

        // Check the whole email address structure
        Matcher emailMatcher = EMAIL_PATTERN.matcher(email);
        if (!emailMatcher.matches()) {
            return false;
        }

        if (!isValidUser(emailMatcher.group(1))) { // call to b
            return false;
        }

        if (!isValidDomain(emailMatcher.group(2))) {
            return false;
        }

        return true;
    }

    /**
     * Returns true if the user component of an email address is valid.
     *
     * @param user being validated
     * @return true if the user name is valid.
     */
    protected boolean isValidUser(String user) { // definition of b

        if (user == null || user.length() > MAX_USERNAME_LEN) {
            return false;
        }

        return USER_PATTERN.matcher(user).matches();
    }
}

public class GenericValidator implements Serializable {
    public static boolean isEmail(String value) {
        return EmailValidator.getInstance().isValid(value); // calls a/call to a?
    }

}
public class EmailValidatorTest {
   /**
    * Tests the e-mail validation.
    */
    @Test
    public void testEmail()  {
       assertTrue(validator.isValid("jsmith@apache.org"));
    }
}
