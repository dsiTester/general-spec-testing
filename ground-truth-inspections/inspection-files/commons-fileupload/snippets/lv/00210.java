public class MultipartStream {

    public void setHeaderEncoding(String encoding) { // definition of a
        headerEncoding = encoding;
    }

    public int readBodyData(OutputStream output)
            throws MalformedStreamException, IOException { // definition of b
        return (int) Streams.copy(newInputStream(), output, false); // throws IOException (expected)
    }

    public boolean skipPreamble() throws IOException { // called from FileItemIteratorImpl.findNextItem()
        // First delimiter may be not preceded with a CRLF.
        System.arraycopy(boundary, 2, boundary, 0, boundary.length - 2);
        boundaryLength = boundary.length - 2;
        computeBoundaryTable();
        try {
            // Discard all data up to the delimiter.
            discardBodyData(); // calls b

            // Read boundary - if succeeded, the stream contains an
            // encapsulation.
            return readBoundary();
        }
        ...
    }

    public int discardBodyData() throws MalformedStreamException, IOException {
        return readBodyData(null); // call to b
    }
}

public class FileItemIteratorImpl {
    FileItemIteratorImpl(RequestContext ctx)
        throws FileUploadException, IOException {
        ...
        multi.setHeaderEncoding(charEncoding); // call to a

        skipPreamble = true;
        findNextItem(); // calls b
    }

    private boolean findNextItem() throws IOException { // called from above
        ...
        for (;;) {
            boolean nextPart;
            if (skipPreamble) {
                nextPart = multi.skipPreamble(); // calls b
            }
            ...
            FileItemHeaders headers = getParsedHeaders(multi.readHeaders()); // whether a is called will impact the return variable of this call
            ...
        }
    }

    public String readHeaders() throws FileUploadIOException, MalformedStreamException { // called from above
        ...
        if (headerEncoding != null) { // whether a was called by this point will influence this conditional, the following control flow, and the return value of this method.
            try {
                headers = baos.toString(headerEncoding);
            } catch (UnsupportedEncodingException e) {
                // Fall back to platform default if specified encoding is not
                // supported.
                headers = baos.toString();
            }
        } else {
            headers = baos.toString();
        }

        return headers;
    }

}

public class FileUploadBase {
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
        } catch (FileUploadIOException e) {
            // unwrap encapsulated SizeException
            throw (FileUploadException) e.getCause();
        }
    }
}

public class StreamingTest {
    public void testIOException()
            throws IOException {
        byte[] request = newRequest();
        InputStream stream = new FilterInputStream(new ByteArrayInputStream(request)){
            private int num;
            @Override
            public int read() throws IOException {
                if (++num > 123) {
                    throw new IOException("123");
                }
                return super.read();
            }
            @Override
            public int read(byte[] pB, int pOff, int pLen)
                    throws IOException {
                for (int i = 0;  i < pLen;  i++) {
                    int res = read();
                    if (res == -1) {
                        return i == 0 ? -1 : i;
                    }
                    pB[pOff+i] = (byte) res;
                }
                return pLen;
            }
        };
        try {
            parseUpload(stream, request.length); // calls a and b
            fail("Expected IOException");
        } catch (FileUploadException e) {
            assertTrue(e.getCause() instanceof IOException);
            assertEquals("123", e.getCause().getMessage());
        }
    }

    private List<FileItem> parseUpload(InputStream pStream, int pLength) // called from above
            throws FileUploadException {
        String contentType = "multipart/form-data; boundary=---1234";

        FileUploadBase upload = new ServletFileUpload();
        upload.setFileItemFactory(new DiskFileItemFactory());
        HttpServletRequest request = new MockHttpServletRequest(pStream,
                pLength, contentType);

        List<FileItem> fileItems = upload.parseRequest(new ServletRequestContext(request)); // calls a and b
        return fileItems;
    }
}
