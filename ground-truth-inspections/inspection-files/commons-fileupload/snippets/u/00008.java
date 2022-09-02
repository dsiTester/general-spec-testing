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
                    String fieldName = getFieldName(headers); // calls b
                    if (fieldName != null) {
                        ...
                        String fileName = getFileName(headers);
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

    protected String getFieldName(FileItemHeaders headers) {
        return getFieldName(headers.getHeader(CONTENT_DISPOSITION)); // call to b
    }

    /**
     * Returns the field name, which is given by the content-disposition
     * header.
     * @param pContentDisposition The content-dispositions header value.
     * @return The field jake
     */
    private String getFieldName(String pContentDisposition) { // definition of b
        String fieldName = null;
        if (pContentDisposition != null
                && pContentDisposition.toLowerCase(Locale.ENGLISH).startsWith(FORM_DATA)) {
            ParameterParser parser = new ParameterParser();
            parser.setLowerCaseNames(true);
            // Parameter parser can handle null input
            Map<String, String> params = parser.parse(pContentDisposition, ';');
            fieldName = params.get("name");
            if (fieldName != null) {
                fieldName = fieldName.trim();
            }
        }
        return fieldName;
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
