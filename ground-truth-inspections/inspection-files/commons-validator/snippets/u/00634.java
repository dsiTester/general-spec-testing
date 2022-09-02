public class UrlValidator implements Serializable {

    /**
     * Returns true if the path is valid.  A <code>null</code> value is considered invalid.
     * @param path Path value to validate.
     * @return true if path is valid.
     */
    protected boolean isValidPath(String path) { // definition of a
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

        int slash2Count = countToken("//", path); // call to b
        if (isOff(ALLOW_2_SLASHES) && (slash2Count > 0)) {
            return false;
        }

        return true;
    }

    /**
     * Returns the number of times the token appears in the target.
     * @param token Token value to be counted.
     * @param target Target value to count tokens in.
     * @return the number of tokens.
     */
    protected int countToken(String token, String target) { // definition of b
        int tokenIndex = 0;
        int count = 0;
        while (tokenIndex != -1) {
            tokenIndex = target.indexOf(token, tokenIndex);
            if (tokenIndex > -1) {
                tokenIndex++;
                count++;
            }
        }
        return count;
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
        if (!isValidAuthority(authority)) {
            return false;
        }

        if (!isValidPath(uri.getRawPath())) { // call to a
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
