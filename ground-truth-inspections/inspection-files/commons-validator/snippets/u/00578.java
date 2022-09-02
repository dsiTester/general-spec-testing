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

        if (!isValidUser(emailMatcher.group(1))) {
            return false;
        }

        if (!isValidDomain(emailMatcher.group(2))) { // call to b
            return false;
        }

        return true;
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
