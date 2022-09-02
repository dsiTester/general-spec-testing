public class MultipartStream {

    public void setHeaderEncoding(String encoding) { // definition of a
        headerEncoding = encoding;
    }

    protected int findSeparator() { // definition of b

        int bufferPos = this.head;
        int tablePos = 0;

        while (bufferPos < this.tail) {
            while (tablePos >= 0 && buffer[bufferPos] != boundary[tablePos]) {
                tablePos = boundaryTable[tablePos];
            }
            bufferPos++;
            tablePos++;
            if (tablePos == boundaryLength) {
                return bufferPos - boundaryLength;
            }
        }
        return -1;
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
        return readBodyData(null); // calls b
    }

    public int readBodyData(OutputStream output)
            throws MalformedStreamException, IOException {
        return (int) Streams.copy(newInputStream(), output, false); // calls b; throws IOException (expected)
    }

    ItemInputStream newInputStream() {
        return new ItemInputStream();
    }

    ItemInputStream() {
        findSeparator();
    }

    /**
     * Called for finding the separator.
     */
    private void findSeparator() {
        pos = MultipartStream.this.findSeparator(); // call to b
        if (pos == -1) {
            if (tail - head > keepRegion) {
                pad = keepRegion;
            } else {
                pad = tail - head;
            }
        }
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

    /**
     * Called for finding the next item, if any.
     *
     * @return True, if an next item was found, otherwise false.
     * @throws IOException An I/O error occurred.
     */
    private boolean findNextItem() throws IOException {
        ...
        for (;;) {
            boolean nextPart;
            if (skipPreamble) {
                nextPart = multi.skipPreamble(); // calls b
            }
            ...
        }
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
