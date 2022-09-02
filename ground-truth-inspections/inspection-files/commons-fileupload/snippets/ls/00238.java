public class ParameterParser {

    public void setLowerCaseNames(boolean b) { // definition of a
        this.lowerCaseNames = b;
    }

    public Map<String, String> parse(final String str, char[] separators) { // definition of b
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
        return parse(str, separator);
    }

    public Map<String, String> parse(final String str, char separator) { // called from b
        if (str == null) {
            return new HashMap<String, String>();
        }
        return parse(str.toCharArray(), separator);
    }

    public Map<String, String> parse(final char[] charArray, char separator) { // called from b
        if (charArray == null) {
            return new HashMap<String, String>();
        }
        return parse(charArray, 0, charArray.length, separator);
    }

    public Map<String, String> parse(
                                     final char[] charArray,
                                     int offset,
                                     int length,
                                     char separator) { // called from b

        if (charArray == null) {
            return new HashMap<String, String>();
        }
        HashMap<String, String> params = new HashMap<String, String>();
        this.chars = charArray;
        this.pos = offset;
        this.len = length;

        String paramName = null;
        String paramValue = null;
        while (hasChar()) {
            paramName = parseToken(new char[] {
                    '=', separator });
            paramValue = null;
            ...
            if ((paramName != null) && (paramName.length() > 0)) {
                if (this.lowerCaseNames) { // calling method-a before method-b is important because of this conditional
                    paramName = paramName.toLowerCase(Locale.ENGLISH);
                }
                params.put(paramName, paramValue);
            }
        }
        return params;
    }
}

public class FileUploadBase {

    @Deprecated
    public List<FileItem> parseRequest(HttpServletRequest req)
    throws FileUploadException { // called from test
        return parseRequest(new ServletRequestContext(req)); // calls a and b
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
            return new FileItemIteratorImpl(ctx); // calls a and b
        } ...
    }

    protected byte[] getBoundary(String contentType) { // called from FileItemIteratorImpl()
        ParameterParser parser = new ParameterParser();
        parser.setLowerCaseNames(true); // call to a
        // Parameter parser can handle null input
        Map<String, String> params = parser.parse(contentType, new char[] {';', ','}); // call to b
        String boundaryStr = params.get("boundary");

        if (boundaryStr == null) {
            return null;
        }
        byte[] boundary;
        try {
            boundary = boundaryStr.getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            boundary = boundaryStr.getBytes(); // Intentionally falls back to default charset
        }
        return boundary;
    }
}

private class FileItemIteratorImpl {
    FileItemIteratorImpl(RequestContext ctx)
        throws FileUploadException, IOException {
        ...
        boundary = getBoundary(contentType); // calls a and b
        if (boundary == null) {
            IOUtils.closeQuietly(input); // avoid possible resource leak
            throw new FileUploadException("the request was rejected because no multipart boundary was found");
        }
        ...
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

