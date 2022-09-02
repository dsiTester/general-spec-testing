public class FileUploadBase {
    protected byte[] getBoundary(String contentType) { // definition of a
        ParameterParser parser = new ParameterParser();
        parser.setLowerCaseNames(true);
        // Parameter parser can handle null input
        Map<String, String> params = parser.parse(contentType, new char[] {';', ','});
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

    protected FileItemHeadersImpl newFileItemHeaders() { // definition of b
        return new FileItemHeadersImpl();
    }

    protected FileItemHeaders getParsedHeaders(String headerPart) { // calls  b
        final int len = headerPart.length();
        FileItemHeadersImpl headers = newFileItemHeaders(); // call to b
        // NOTE: can insert a call to method-a here without failure. Hence spurious spec.
        // getBoundary("multipart/form-data; boundary=---1234");
        ...
    }


    @Deprecated
    public List<FileItem> parseRequest(HttpServletRequest req)
    throws FileUploadException {  // called by test
        return parseRequest(new ServletRequestContext(req));
    }

    public List<FileItem> parseRequest(RequestContext ctx)
        throws FileUploadException {
        List<FileItem> items = new ArrayList<FileItem>();
        boolean successful = false;
        try {
            FileItemIterator iter = getItemIterator(ctx); // calls a and b
            FileItemFactory fac = getFileItemFactory();
            ...
        }
        ...
    }

    public FileItemIterator getItemIterator(RequestContext ctx)
        throws FileUploadException, IOException {
        try {
            return new FileItemIteratorImpl(ctx); // calls a and b
        } catch (FileUploadIOException e) {
            // unwrap encapsulated SizeException
            throw (FileUploadException) e.getCause();
        }
    }

}

public class FileItemIteratorImpl {
    FileItemIteratorImpl(RequestContext ctx)
        throws FileUploadException, IOException {
        ...
        boundary = getBoundary(contentType); // call to a
        if (boundary == null) {
            IOUtils.closeQuietly(input); // avoid possible resource leak
            throw new FileUploadException("the request was rejected because no multipart boundary was found"); // exception thrown here
        }
        ...
        findNextItem(); // calls b
    }

    private boolean findNextItem() throws IOException {
        ...
        for (;;) {
            ...
            FileItemHeaders headers = getParsedHeaders(multi.readHeaders()); // call to b
            ...
        }
    }

}

public class DiskFileUploadTest {
    @Test
    public void testMoveFile() throws Exception {
        DiskFileUpload myUpload = new DiskFileUpload();
        myUpload.setSizeThreshold(0);
    	final String content =
                "-----1234\r\n" +
                "Content-Disposition: form-data; name=\"file\";"
                		+ "filename=\"foo.tab\"\r\n" +
                "Content-Type: text/whatever\r\n" +
                "\r\n" +
                "This is the content of the file\n" +
                "\r\n" +
                "-----1234--\r\n";
    	final byte[] contentBytes = content.getBytes("US-ASCII");
        final HttpServletRequest request = new MockHttpServletRequest(contentBytes, Constants.CONTENT_TYPE);
        final List<FileItem> items = myUpload.parseRequest(request); // calls a and b
        assertNotNull(items);
        assertFalse(items.isEmpty());
        final DiskFileItem dfi = (DiskFileItem) items.get(0);
        final File out = File.createTempFile("install", ".tmp");
        dfi.write(out);
    }
}
