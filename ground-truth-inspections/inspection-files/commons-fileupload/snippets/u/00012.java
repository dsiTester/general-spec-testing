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
                if (currentFieldName == null) {
                    String fieldName = getFieldName(headers);
                    if (fieldName != null) {
                        ...
                        String fileName = getFileName(headers); // call to b
                        currentItem = new FileItemStreamImpl(fileName,
                                fieldName, headers.getHeader(CONTENT_TYPE),
                                fileName == null, getContentLength(headers));
                        currentItem.setHeaders(headers);
                        notifier.noteItem();
                        itemValid = true;
                        return true;
                    } ...
                } ...
        ...
    }

    /**
     * Retrieves the file name from the <code>Content-disposition</code>
     * header.
     *
     * @param headers The HTTP headers object.
     *
     * @return The file name for the current <code>encapsulation</code>.
     */
    protected String getFileName(FileItemHeaders headers) { // definition of b
        return getFileName(headers.getHeader(CONTENT_DISPOSITION));
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
