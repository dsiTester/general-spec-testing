public class FileUploadBase {
    @Deprecated
    public List<FileItem> parseRequest(HttpServletRequest req) // definition of a
    throws FileUploadException {
        return parseRequest(new ServletRequestContext(req)); // calls b
    }

    public List<FileItem> parseRequest(RequestContext ctx)
            throws FileUploadException {
        ...
        try {
            FileItemIterator iter = getItemIterator(ctx); // call to b
            ...
        }
        ...
    }

    /**
     * Processes an <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>
     * compliant <code>multipart/form-data</code> stream.
     *
     * @param ctx The context for the request to be parsed.
     *
     * @return An iterator to instances of <code>FileItemStream</code>
     *         parsed from the request, in the order that they were
     *         transmitted.
     *
     * @throws FileUploadException if there are problems reading/parsing
     *                             the request or storing files.
     * @throws IOException An I/O error occurred. This may be a network
     *   error while communicating with the client or a problem while
     *   storing the uploaded content.
     */
    public FileItemIterator getItemIterator(RequestContext ctx)
    throws FileUploadException, IOException { // definition of b
        try {
            return new FileItemIteratorImpl(ctx);
        } catch (FileUploadIOException e) {
            // unwrap encapsulated SizeException
            throw (FileUploadException) e.getCause();
        }
    }
}


public class DiskFileUploadTest {
    @Test
    public void testWithInvalidRequest() { // expected exception test
        HttpServletRequest req = HttpServletRequestFactory.createInvalidHttpServletRequest();

        try {
            upload.parseRequest(req); // call to a
            fail("testWithInvalidRequest: expected exception was not thrown"); // assertion fails here
        } catch (FileUploadException expected) {
            // this exception is expected
        }
    }


    @Test
    public void testMoveFile() throws Exception {
        ...
        final HttpServletRequest request = new MockHttpServletRequest(contentBytes, Constants.CONTENT_TYPE);
        final List<FileItem> items = myUpload.parseRequest(request); // call to a
        assertNotNull(items); // assertion fails here
        ...
    }
}
