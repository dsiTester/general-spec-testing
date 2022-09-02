public class FileUploadBase {

    public List<FileItem> parseRequest(RequestContext ctx)
            throws FileUploadException { // called from Util.parseUpload
        List<FileItem> items = new ArrayList<FileItem>();
        boolean successful = false;
        try {
            FileItemIterator iter = getItemIterator(ctx); // call to a; calls b
            FileItemFactory fac = getFileItemFactory();
            ...
            while (iter.hasNext()) { // NullPointerException here
                ...
            }
        }
        ...
    }

    public FileItemIterator getItemIterator(RequestContext ctx)
        throws FileUploadException, IOException { // definition of a
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
            findNextItem(); // calls b
        }

        private boolean findNextItem() throws IOException { // calls b
            for (;;) {
                ...
                FileItemHeaders headers = getParsedHeaders(multi.readHeaders()); // calls b
                if (currentFieldName == null) {
                    ...
                }
                ...
            }

    }

    protected FileItemHeaders getParsedHeaders(String headerPart) { // called from above
        final int len = headerPart.length();
        FileItemHeadersImpl headers = newFileItemHeaders();
        for (;;) {
            ...
            parseHeaderLine(headers, header.toString()); // call to b
        }
        ...
    }

    /**
     * Reads the next header line.
     * @param headers String with all headers.
     * @param header Map where to store the current header.
     */
    private void parseHeaderLine(FileItemHeadersImpl headers, String header) { // definition of b
        final int colonOffset = header.indexOf(':');
        if (colonOffset == -1) {
            // This header line is malformed, skip it.
            return;
        }
        String headerName = header.substring(0, colonOffset).trim();
        String headerValue =
            header.substring(header.indexOf(':') + 1).trim();
        headers.addHeader(headerName, headerValue);
    }

}

public class ServletFileUpload {
    @Override
    public List<FileItem> parseRequest(HttpServletRequest request)
        throws FileUploadException { // called from test
        return parseRequest(new ServletRequestContext(request)); // calls a and b
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
        ...
    }

    private List<FileItem> parseUpload(InputStream pStream, int pLength)
            throws FileUploadException { // called from test
        String contentType = "multipart/form-data; boundary=---1234";

        FileUploadBase upload = new ServletFileUpload();
        upload.setFileItemFactory(new DiskFileItemFactory());
        HttpServletRequest request = new MockHttpServletRequest(pStream,
                pLength, contentType);

        List<FileItem> fileItems = upload.parseRequest(new ServletRequestContext(request)); // calls a and b
        return fileItems;
    }
}
