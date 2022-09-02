public class EmailValidator implements Serializable {
    /**
     * <p>Checks if a field has a valid e-mail address.</p>
     *
     * @param email The value validation is being performed on.  A <code>null</code>
     *              value is considered invalid.
     * @return true if the email address is valid.
     */
    public boolean isValid(String email) { // calls a and b
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

        if (!isValidUser(emailMatcher.group(1))) { // call to a
            return false;                          // exits here in DSI experiment due to return value replacement
        }

        if (!isValidDomain(emailMatcher.group(2))) { // call to b
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
    protected boolean isValidUser(String user) { // definition of a

        if (user == null || user.length() > MAX_USERNAME_LEN) {
            return false;
        }

        return USER_PATTERN.matcher(user).matches();
    }

    /**
     * Returns true if the domain component of an email address is valid.
     *
     * @param domain being validated, may be in IDN format
     * @return true if the email address's domain is valid.
     */
    protected boolean isValidDomain(String domain) { // definition of b
        // see if domain is an IP address in brackets
        Matcher ipDomainMatcher = IP_DOMAIN_PATTERN.matcher(domain);

        if (ipDomainMatcher.matches()) {
            InetAddressValidator inetAddressValidator =
                    InetAddressValidator.getInstance();
            return inetAddressValidator.isValid(ipDomainMatcher.group(1));
        }
        // Domain is symbolic name
        if (allowTld) {
            System.out.println(domain);
            System.out.println(domainValidator.isValid(domain));
            return domainValidator.isValid(domain) || (!domain.startsWith(".") && domainValidator.isValidTld(domain));
        }
        return domainValidator.isValid(domain);
    }

}

public class GenericValidator implements Serializable {
    public static boolean isEmail(String value) {
        return EmailValidator.getInstance().isValid(value); // calls a and b
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
