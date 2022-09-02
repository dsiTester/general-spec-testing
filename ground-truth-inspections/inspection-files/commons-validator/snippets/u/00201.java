@Deprecated
public class UrlValidator implements Serializable {

    /**
     * Validate scheme. If schemes[] was initialized to a non null,
     * then only those scheme's are allowed.  Note this is slightly different
     * than for the constructor.
     * @param scheme The scheme to validate.  A <code>null</code> value is considered
     * invalid.
     * @return true if valid.
     */
    protected boolean isValidScheme(String scheme) { // definition of a
        if (scheme == null) {
            return false;
        }

        if (!SCHEME_PATTERN.matcher(scheme).matches()) {
            return false;
        }

        if (options.isOff(ALLOW_ALL_SCHEMES) && !allowedSchemes.contains(scheme)) {
            return false;
        }

        return true;
    }

    /**
     * Returns true if the given fragment is null or fragments are allowed.
     * @param fragment Fragment value to validate.
     * @return true if fragment is valid.
     */
    protected boolean isValidFragment(String fragment) { // definition of b
        if (fragment == null) {
            return true;
        }

        return options.isOff(NO_FRAGMENTS);
    }

    public boolean isValid(String value) { // called from test
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

        if (!isValidScheme(urlMatcher.group(PARSE_URL_SCHEME))) { // call to a
            return false;
        }

        if (!isValidAuthority(urlMatcher.group(PARSE_URL_AUTHORITY))) {
            return false;
        }

        if (!isValidPath(urlMatcher.group(PARSE_URL_PATH))) {
            return false;
        }

        if (!isValidQuery(urlMatcher.group(PARSE_URL_QUERY))) {
            return false;
        }

        if (!isValidFragment(urlMatcher.group(PARSE_URL_FRAGMENT))) { // call to b
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
       urlValidator.isValid("http://www.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.logoworks.comwww.log"); // calls a and b
   }

}
