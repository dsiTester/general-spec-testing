@Deprecated
public class UrlValidator implements Serializable {
    /**
     * <p>Checks if a field has a valid url address.</p>
     *
     * @param value The value validation is being performed on.  A <code>null</code>
     * value is considered invalid.
     * @return true if the url is valid.
     */
    public boolean isValid(String value) { // definition of a
        if (value == null) {
            return false;
        }
        if (!LEGAL_ASCII_PATTERN.matcher(value).matches()) {
           return false;
        }

        // Check the whole url address structure
        Matcher urlMatcher = URL_PATTERN.matcher(value);
        if (!urlMatcher.matches()) {
            return false;
        }

        if (!isValidScheme(urlMatcher.group(PARSE_URL_SCHEME))) {
            return false;
        }

        if (!isValidAuthority(urlMatcher.group(PARSE_URL_AUTHORITY))) { // call to b
            return false;
        }

        if (!isValidPath(urlMatcher.group(PARSE_URL_PATH))) {
            return false;
        }

        if (!isValidQuery(urlMatcher.group(PARSE_URL_QUERY))) {
            return false;
        }

        if (!isValidFragment(urlMatcher.group(PARSE_URL_FRAGMENT))) {
            return false;
        }

        return true;
    }

    /**
     * Returns true if the authority is properly formatted.  An authority is the combination
     * of hostname and port.  A <code>null</code> authority value is considered invalid.
     * @param authority Authority value to validate.
     * @return true if authority (hostname and port) is valid.
     */
    protected boolean isValidAuthority(String authority) { // definition of b
        if (authority == null) {
            return false;
        }

        InetAddressValidator inetAddressValidator =
                InetAddressValidator.getInstance();

        Matcher authorityMatcher = AUTHORITY_PATTERN.matcher(authority);
        if (!authorityMatcher.matches()) {
            return false;
        }

        boolean hostname = false;
        // check if authority is IP address or hostname
        String hostIP = authorityMatcher.group(PARSE_AUTHORITY_HOST_IP);
        boolean ipV4Address = inetAddressValidator.isValid(hostIP);

        if (!ipV4Address) {
            // Domain is hostname name
            hostname = DOMAIN_PATTERN.matcher(hostIP).matches();
        }

        //rightmost hostname will never start with a digit.
        if (hostname) {
            // LOW-TECH FIX FOR VALIDATOR-202
            // TODO: Rewrite to use ArrayList and .add semantics: see VALIDATOR-203
            char[] chars = hostIP.toCharArray();
            int size = 1;
            for (char element : chars) {
                if(element == '.') {
                    size++;
                }
            }
            String[] domainSegment = new String[size];
            boolean match = true;
            int segmentCount = 0;
            int segmentLength = 0;

            while (match) {
                Matcher atomMatcher = ATOM_PATTERN.matcher(hostIP);
                match = atomMatcher.matches();
                if (match) {
                    domainSegment[segmentCount] = atomMatcher.group(1);
                    segmentLength = domainSegment[segmentCount].length() + 1;
                    hostIP =
                            (segmentLength >= hostIP.length())
                            ? ""
                            : hostIP.substring(segmentLength);

                    segmentCount++;
                }
            }
            String topLevel = domainSegment[segmentCount - 1];
            if (topLevel.length() < 2 || topLevel.length() > 4) { // CHECKSTYLE IGNORE MagicNumber (deprecated code)
                return false;
            }

            // First letter of top level must be a alpha
            if (!ALPHA_PATTERN.matcher(topLevel.substring(0, 1)).matches()) {
                return false;
            }

            // Make sure there's a host name preceding the authority.
            if (segmentCount < 2) {
                return false;
            }
        }

        if (!hostname && !ipV4Address) {
            return false;
        }

        String port = authorityMatcher.group(PARSE_AUTHORITY_PORT);
        if (port != null && !PORT_PATTERN.matcher(port).matches()) {
            return false;
        }

        String extra = authorityMatcher.group(PARSE_AUTHORITY_EXTRA);
        if (!GenericValidator.isBlankOrNull(extra)) {
            return false;
        }

        return true;
    }

}

@Deprecated
public class UrlTest extends TestCase {
   public void testValidator202() {
       String[] schemes = {"http","https"};
       UrlValidator urlValidator = new UrlValidator(schemes, UrlValidator.NO_FRAGMENTS);
       urlValidator.isValid("http://www.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.log"); // call to a
   }

}
