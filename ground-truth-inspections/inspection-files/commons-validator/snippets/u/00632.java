public class UrlValidator implements Serializable {
    /**
     * Returns true if the authority is properly formatted.  An authority is the combination
     * of hostname and port.  A <code>null</code> authority value is considered invalid.
     * Note: this implementation validates the domain unless a RegexValidator was provided.
     * If a RegexValidator was supplied and it matches, then the authority is regarded
     * as valid with no further checks, otherwise the method checks against the
     * AUTHORITY_PATTERN and the DomainValidator (ALLOW_LOCAL_URLS)
     * @param authority Authority value to validate, alllows IDN
     * @return true if authority (hostname and port) is valid.
     */
    protected boolean isValidAuthority(String authority) { // definition of a
        if (authority == null) {
            return false;
        }

        // check manual authority validation if specified
        if (authorityValidator != null && authorityValidator.isValid(authority)) {
            return true;
        }
        // convert to ASCII if possible
        final String authorityASCII = DomainValidator.unicodeToASCII(authority);

        Matcher authorityMatcher = AUTHORITY_PATTERN.matcher(authorityASCII);
        if (!authorityMatcher.matches()) {
            return false;
        }

        // We have to process IPV6 separately because that is parsed in a different group
        String ipv6 = authorityMatcher.group(PARSE_AUTHORITY_IPV6);
        if (ipv6 != null) {
            InetAddressValidator inetAddressValidator = InetAddressValidator.getInstance();
                if (!inetAddressValidator.isValidInet6Address(ipv6)) {
                    return false;
                }
        } else {
            String hostLocation = authorityMatcher.group(PARSE_AUTHORITY_HOST_IP);
            // check if authority is hostname or IP address:
            // try a hostname first since that's much more likely
            if (!this.domainValidator.isValid(hostLocation)) {
                // try an IPv4 address
                InetAddressValidator inetAddressValidator = InetAddressValidator.getInstance();
                if (!inetAddressValidator.isValidInet4Address(hostLocation)) {
                    // isn't IPv4, so the URL is invalid
                    return false;
                }
            }
            String port = authorityMatcher.group(PARSE_AUTHORITY_PORT);
            if (port != null && !port.isEmpty()) {
                try {
                    int iPort = Integer.parseInt(port);
                    if (iPort < 0 || iPort > MAX_UNSIGNED_16_BIT_INT) {
                        return false;
                    }
                } catch (NumberFormatException nfe) {
                    return false; // this can happen for big numbers
                }
            }
        }

        String extra = authorityMatcher.group(PARSE_AUTHORITY_EXTRA);
        if (extra != null && !extra.trim().isEmpty()){
            return false;
        }

        return true;
    }

    /**
     * Returns true if the path is valid.  A <code>null</code> value is considered invalid.
     * @param path Path value to validate.
     * @return true if path is valid.
     */
    protected boolean isValidPath(String path) { // definition of b
        if (path == null) {
            return false;
        }

        if (!PATH_PATTERN.matcher(path).matches()) {
            return false;
        }

        try {
            // Don't omit host otherwise leading path may be taken as host if it starts with //
            URI uri = new URI(null,"localhost",path,null);
            String norm = uri.normalize().getPath();
            if (norm.startsWith("/../") // Trying to go via the parent dir
             || norm.equals("/..")) {   // Trying to go to the parent dir
                return false;
            }
        } catch (URISyntaxException e) {
            return false;
        }

        int slash2Count = countToken("//", path);
        if (isOff(ALLOW_2_SLASHES) && (slash2Count > 0)) {
            return false;
        }

        return true;
    }


    public boolean isValid(String value) { // called from test
        if (value == null) {
            return false;
        }

        URI uri; // ensure value is a valid URI
        try {
            uri = new URI(value);
        } catch (URISyntaxException e) {
            return false;
        }
        // OK, perfom additional validation

        String scheme = uri.getScheme();
        if (!isValidScheme(scheme)) {
            return false;
        }

        String authority = uri.getRawAuthority();
        if ("file".equals(scheme) && (authority == null || "".equals(authority))) {// Special case - file: allows an empty authority
            return true; // this is a local file - nothing more to do here
        }
        if ("file".equals(scheme) && authority != null && authority.contains(":")) {
            return false;
        }
        // Validate the authority
        if (!isValidAuthority(authority)) { // call to a
            return false;
        }

        if (!isValidPath(uri.getRawPath())) { // call to b
            return false;
        }

        if (!isValidQuery(uri.getRawQuery())) {
            return false;
        }

        if (!isValidFragment(uri.getRawFragment())) {
            return false;
        }

        return true;
    }


}

public class UrlValidatorTest {
   @Test
   public void testValidator202() {
       String[] schemes = {"http","https"};
       UrlValidator urlValidator = new UrlValidator(schemes, UrlValidator.NO_FRAGMENTS);
       assertTrue(urlValidator.isValid("http://l.l.l.l.l.l.l.l.l.l.l.l.l.l.l.l.l.l.l.l.l.l.l.l.l.l.l.l.l.l.l.l.l.l.l.l.l.l.l.l.l.l.l.l.l.l.l.l.l.l.l.l.l.l.l.l.l.l.l.org")); // calls a and b
   }

}
