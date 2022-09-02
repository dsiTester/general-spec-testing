public class DomainValidator implements Serializable {
    /**
     * Returns true if the specified <code>String</code> parses
     * as a valid domain name with a recognized top-level domain.
     * The parsing is case-insensitive.
     * @param domain the parameter to check for domain name syntax
     * @return true if the parameter is a valid domain name
     */
    public boolean isValid(String domain) { // definition of a
        if (domain == null) {
            return false;
        }
        domain = unicodeToASCII(domain);
        // hosts must be equally reachable via punycode and Unicode
        // Unicode is never shorter than punycode, so check punycode
        // if domain did not convert, then it will be caught by ASCII
        // checks in the regexes below
        if (domain.length() > MAX_DOMAIN_LENGTH) {
            return false;
        }
        String[] groups = domainRegex.match(domain);
        if (groups != null && groups.length > 0) {
            return isValidTld(groups[0]); // call to b in unknown test (invalidated test doesn't cover this line)
        }
        return allowLocal && hostnameRegex.isValid(domain);
    }

    /**
     * Returns true if the specified <code>String</code> matches any
     * IANA-defined top-level domain. Leading dots are ignored if present.
     * The search is case-insensitive.
     * <p>
     * If allowLocal is true, the TLD is checked using {@link #isValidLocalTld(String)}.
     * The TLD is then checked against {@link #isValidInfrastructureTld(String)},
     * {@link #isValidGenericTld(String)} and {@link #isValidCountryCodeTld(String)}
     * @param tld the parameter to check for TLD status, not null
     * @return true if the parameter is a TLD
     */
    public boolean isValidTld(String tld) { // definition of b
        if(allowLocal && isValidLocalTld(tld)) {
            return true;
        }
        return isValidInfrastructureTld(tld)
                || isValidGenericTld(tld)
                || isValidCountryCodeTld(tld);
    }
}

public class EmailValidator implements Serializable {
    protected boolean isValidDomain(String domain) {
        // see if domain is an IP address in brackets
        Matcher ipDomainMatcher = IP_DOMAIN_PATTERN.matcher(domain);

        if (ipDomainMatcher.matches()) {
            InetAddressValidator inetAddressValidator =
                    InetAddressValidator.getInstance();
            return inetAddressValidator.isValid(ipDomainMatcher.group(1));
        }
        // Domain is symbolic name
        if (allowTld) {
            return domainValidator.isValid(domain) || (!domain.startsWith(".") && domainValidator.isValidTld(domain)); // call to a; call to b
        }
        return domainValidator.isValid(domain);
    }

}

public class EmailValidatorTest {
    /**
     * Tests the e-mail validation with a user at a TLD
     *
     * http://tools.ietf.org/html/rfc5321#section-2.3.5
     * (In the case of a top-level domain used by itself in an
     * email address, a single string is used without any dots)
     */
    @Test
    public void testEmailAtTLD() { // invalidated test
        EmailValidator val = EmailValidator.getInstance(false, true);
        assertTrue(val.isValid("test@com")); // calls a
    }
}

public class DomainValidatorTest extends TestCase {
    public void testIDN() {     // unknown test
       assertTrue("b\u00fccher.ch in IDN should validate", validator.isValid("www.xn--bcher-kva.ch")); // call to a; assertion fails here
    }
}
