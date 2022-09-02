public class FileUploadBase {

    public List<FileItem> parseRequest(RequestContext ctx)
            throws FileUploadException { // definition of a
        List<FileItem> items = new ArrayList<FileItem>();
        boolean successful = false;
        try {
            FileItemIterator iter = getItemIterator(ctx);
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
            findNextItem(); // calls a and b
        }

        private boolean findNextItem() throws IOException {
            ...
            for (;;) {
                ...
                FileItemHeaders headers = getParsedHeaders(multi.readHeaders());
                if (currentFieldName == null) {
                    // We're parsing the outer multipart
                    String fieldName = getFieldName(headers);
                    if (fieldName != null) { // this branch will not be entered if a is not called
                        String subContentType = headers.getHeader(CONTENT_TYPE);
                        ...
                            String fileName = getFileName(headers); // calls b
                            currentItem = new FileItemStreamImpl(fileName,
                                                                 fieldName, headers.getHeader(CONTENT_TYPE),
                                                                 fileName == null, getContentLength(headers));
                            ...
                     }
                    ...
                 }
            }

    }

    protected String getFileName(FileItemHeaders headers) { // definition of b
        return getFileName(headers.getHeader(CONTENT_DISPOSITION));
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
