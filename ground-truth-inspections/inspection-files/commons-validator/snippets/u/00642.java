public class UrlValidator implements Serializable {

    /**
     * Validate scheme. If schemes[] was initialized to a non null,
     * then only those schemes are allowed.
     * Otherwise the default schemes are "http", "https", "ftp".
     * Matching is case-blind.
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

        if (isOff(ALLOW_ALL_SCHEMES) && !allowedSchemes.contains(scheme.toLowerCase(Locale.ENGLISH))) {
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
        if (!isValidScheme(scheme)) { // call to a
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
        if (!isValidAuthority(authority)) {
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
