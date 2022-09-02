public class FileUploadBase {
    @Deprecated
    public List<FileItem> parseRequest(HttpServletRequest req) // definition of a
    throws FileUploadException {
        return parseRequest(new ServletRequestContext(req)); // calls b
    }

    public List<FileItem> parseRequest(RequestContext ctx)
            throws FileUploadException {
        try {
            FileItemIterator iter = getItemIterator(ctx); // calls b
            ...
        }
        ...
    }

    public FileItemIterator getItemIterator(RequestContext ctx)
    throws FileUploadException, IOException {
        try {
            return new FileItemIteratorImpl(ctx); // calls b
        } ...
    }

    private class FileItemIteratorImpl implements FileItemIterator {
        FileItemIteratorImpl(RequestContext ctx)
            throws FileUploadException, IOException { // called from above
            ...
            findNextItem();     // calls b
        }
    }

    private boolean findNextItem() throws IOException {
        ...
        for (;;) {
            ...
            FileItemHeaders headers = getParsedHeaders(multi.readHeaders()); // calls b
            ...
        }
        ...
    }

    protected FileItemHeaders getParsedHeaders(String headerPart) {
        final int len = headerPart.length();
        FileItemHeadersImpl headers = newFileItemHeaders(); // call to b
        int start = 0;
        for (;;) {
            ...
            parseHeaderLine(headers, header.toString());
        }
        return headers;
    }

    /**
     * Creates a new instance of {@link FileItemHeaders}.
     * @return The new instance.
     */
    protected FileItemHeadersImpl newFileItemHeaders() { // definition of b
        return new FileItemHeadersImpl();
    }

}


public class DiskFileUploadTest {
    @Test
    public void testMoveFile() throws Exception {
        ...
        final HttpServletRequest request = new MockHttpServletRequest(contentBytes, Constants.CONTENT_TYPE);
        final List<FileItem> items = myUpload.parseRequest(request); // call to a
        assertNotNull(items); // assertion fails here
        ...
    }
}
