public class DomainValidator implements Serializable {

    /**
     * Does this instance allow local addresses?
     *
     * @return true if local addresses are allowed.
     * @since 1.7
     */
    public boolean isAllowLocal() { // definition of a
        return this.allowLocal;
    }

    /**
     * Returns true if the specified <code>String</code> matches any
     * widely used "local" domains (localhost or localdomain). Leading dots are
     * ignored if present. The search is case-insensitive.
     * @param lTld the parameter to check for local TLD status, not null
     * @return true if the parameter is an local TLD
     */
    public boolean isValidLocalTld(String lTld) { // definition of b
        final String key = chompLeadingDot(unicodeToASCII(lTld).toLowerCase(Locale.ENGLISH));
        return (arrayContains(LOCAL_TLDS, key) || arrayContains(mylocalTLDsPlus, key))
                && !arrayContains(mylocalTLDsMinus, key);
    }

    public boolean isValidTld(String tld) {      // called from DomainValidator.isValidDomain()
        if(allowLocal && isValidLocalTld(tld)) { // call to b
            return true;
        }
        return isValidInfrastructureTld(tld)
                || isValidGenericTld(tld)
                || isValidCountryCodeTld(tld);
    }

}

public class EmailValidator implements Serializable {
    public EmailValidator(boolean allowLocal, boolean allowTld, DomainValidator domainValidator) { // called from test
        this.allowTld = allowTld;
        if (domainValidator == null) {
            throw new IllegalArgumentException("DomainValidator cannot be null");
        }
        if (domainValidator.isAllowLocal() != allowLocal) { // call to a
            throw new IllegalArgumentException("DomainValidator must agree with allowLocal setting"); // throws exception here
        }
        this.domainValidator = domainValidator;
    }

}

public class EmailValidatorTest {
    @Test
    public void testValidator473_4() { // Show that can override domain validation
        assertFalse(validator.isValidDomain("test.local"));
        List<DomainValidator.Item> items = new ArrayList<>();
        items.add(new DomainValidator.Item(DomainValidator.ArrayType.GENERIC_PLUS, new String[]{"local"}));
        EmailValidator val = new EmailValidator(true, false, DomainValidator.getInstance(true, items)); // calls a
        assertTrue(val.isValidDomain("test.local")); // calls b
        // NOTE: one can call a after b by uncommenting the following line:
        // System.out.println(val.isAllowLocal());
    }

}
