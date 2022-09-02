public class ParameterParser {
    public Map<String, String> parse(final String str, char[] separators) { // definition of a
        if (separators == null || separators.length == 0) {
            return new HashMap<String, String>();
        }
        char separator = separators[0];
        if (str != null) {
            int idx = str.length();
            for (char separator2 : separators) {
                int tmp = str.indexOf(separator2);
                if (tmp != -1 && tmp < idx) {
                    idx = tmp;
                    separator = separator2;
                }
            }
        }
        return parse(str, separator); // calls b
    }

    public Map<String, String> parse(final String str, char separator) { // called from a
        if (str == null) {
            return new HashMap<String, String>();
        }
        return parse(str.toCharArray(), separator); // calls b
    }

    public Map<String, String> parse(final char[] charArray, char separator) {
        if (charArray == null) {
            return new HashMap<String, String>();
        }
        return parse(charArray, 0, charArray.length, separator); // calls b
    }

    public Map<String, String> parse(
                                     final char[] charArray,
                                     int offset,
                                     int length,
                                     char separator) {

        if (charArray == null) {
            return new HashMap<String, String>();
        }
        ...
        while (hasChar()) {
            paramName = parseToken(new char[] { // calls b
                    '=', separator });
            paramValue = null;
            ...
        }
    }

    private String parseToken(final char[] terminators) {
        char ch;
        i1 = pos;
        i2 = pos;
        while (hasChar()) {
            ch = chars[pos];
            if (isOneOf(ch, terminators)) {
                break;
            }
            i2++;
            pos++;
        }
        return getToken(false); // call to b
    }

    private String getToken(boolean quoted) { // definition of b
        // Trim leading white spaces
        while ((i1 < i2) && (Character.isWhitespace(chars[i1]))) {
            i1++;
        }
        // Trim trailing white spaces
        while ((i2 > i1) && (Character.isWhitespace(chars[i2 - 1]))) {
            i2--;
        }
        // Strip away quotation marks if necessary
        if (quoted
            && ((i2 - i1) >= 2)
            && (chars[i1] == '"')
            && (chars[i2 - 1] == '"')) {
            i1++;
            i2--;
        }
        String result = null;
        if (i2 > i1) {
            result = new String(chars, i1, i2 - i1);
        }
        return result;
    }

}

public class FileUploadBase {

    @Deprecated
    public List<FileItem> parseRequest(HttpServletRequest req)
    throws FileUploadException { // called from test
        return parseRequest(new ServletRequestContext(req));
    }

    public List<FileItem> parseRequest(RequestContext ctx)
        throws FileUploadException {
        List<FileItem> items = new ArrayList<FileItem>();
        boolean successful = false;
        try {
            FileItemIterator iter = getItemIterator(ctx); // calls a and b
            ...
        }
        ...
     }

    public FileItemIterator getItemIterator(RequestContext ctx)
    throws FileUploadException, IOException {
        try {
            return new FileItemIteratorImpl(ctx); // calls b
        } catch (FileUploadIOException e) {
            // unwrap encapsulated SizeException
            throw (FileUploadException) e.getCause();
        }
    }

    private class FileItemIteratorImpl implements FileItemIterator {
        FileItemIteratorImpl(RequestContext ctx)
            throws FileUploadException, IOException {
            ...
            boundary = getBoundary(contentType); // calls a
            if (boundary == null) {
                IOUtils.closeQuietly(input); // avoid possible resource leak
                throw new FileUploadException("the request was rejected because no multipart boundary was found");
            }
            ...
        }

    }

    protected byte[] getBoundary(String contentType) { // called from FileItemIteratorImpl()
        ParameterParser parser = new ParameterParser();
        parser.setLowerCaseNames(true);
        // Parameter parser can handle null input
        Map<String, String> params = parser.parse(contentType, new char[] {';', ','}); // call to a
        String boundaryStr = params.get("boundary"); // NullPointerException here

        ...
        return boundary;
    }

}

public class DiskFileUploadTest {
    @Test
    public void testWithInvalidRequest() {
        HttpServletRequest req = HttpServletRequestFactory.createInvalidHttpServletRequest();

        try {
            upload.parseRequest(req); // calls a and b
            fail("testWithInvalidRequest: expected exception was not thrown");
        } catch (FileUploadException expected) {
            // this exception is expected
        }
    }
}
