public class FileUploadBase {

    public List<FileItem> parseRequest(RequestContext ctx)
            throws FileUploadException { // definition of a
        List<FileItem> items = new ArrayList<FileItem>();
        boolean successful = false;
        try {
            FileItemIterator iter = getItemIterator(ctx); // calls b
            ...
        }
        ...
    }

    public FileItemIterator getItemIterator(RequestContext ctx)
    throws FileUploadException, IOException { // called from a
        try {
            return new FileItemIteratorImpl(ctx); // calls b
        } catch (FileUploadIOException e) {
            // unwrap encapsulated SizeException
            throw (FileUploadException) e.getCause();
        }
    }

    private class FileItemIteratorImpl implements FileItemIterator {
        FileItemIteratorImpl(RequestContext ctx)
            throws FileUploadException, IOException { // called from FileUploadBase.getItemIterator, calls findNextItem().
            ...
            findNextItem(); // calls b
        }

        private boolean findNextItem() throws IOException {
            ...
            for (;;) {
                ...
                FileItemHeaders headers = getParsedHeaders(multi.readHeaders());
                if (currentFieldName == null) {
                    // We're parsing the outer multipart
                    String fieldName = getFieldName(headers); // calls b
                    if (fieldName != null) {
                        ...
                    }
                } else {
                    String fileName = getFileName(headers);
                    if (fileName != null) {
                        ...
                    }
                }
                ...
             }
        }

    }

    protected String getFieldName(FileItemHeaders headers) { // called from FileItemIteratorImpl.findNextItem()
        return getFieldName(headers.getHeader(CONTENT_DISPOSITION)); // call to b
    }

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

public class StreamingTest {
    public void testFILEUPLOAD135()
            throws IOException, FileUploadException {
        byte[] request = newShortRequest();
        final ByteArrayInputStream bais = new ByteArrayInputStream(request);
        List<FileItem> fileItems = parseUpload(new InputStream() {
            @Override
            public int read()
            throws IOException
            {
                return bais.read();
            }
            @Override
            public int read(byte b[], int off, int len) throws IOException
            {
                return bais.read(b, off, Math.min(len, 3));
            }

        }, request.length); // calls a and b
        Iterator<FileItem> fileIter = fileItems.iterator(); // NullPointerException here
        assertTrue(fileIter.hasNext());
        ...
    }

    private List<FileItem> parseUpload(InputStream pStream, int pLength)
            throws FileUploadException { // called from above
        String contentType = "multipart/form-data; boundary=---1234";

        FileUploadBase upload = new ServletFileUpload();
        upload.setFileItemFactory(new DiskFileItemFactory());
        HttpServletRequest request = new MockHttpServletRequest(pStream,
                pLength, contentType);

        List<FileItem> fileItems = upload.parseRequest(new ServletRequestContext(request)); // call to a; calls b
        return fileItems;
    }
}
