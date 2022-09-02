public class ServletFileUpload extends FileUpload {
    /**
     * Constructs an uninitialised instance of this class. A factory must be
     * configured, using <code>setFileItemFactory()</code>, before attempting
     * to parse requests.
     *
     * @see FileUpload#FileUpload(FileItemFactory)
     */
    public ServletFileUpload() {
        super();
    }
}

public class FileUpload extends FileUploadBase {
    @Override
    public void setFileItemFactory(FileItemFactory factory) { // called definition of a
        this.fileItemFactory = factory;
    }
}

public class FileUploadBase {

    public FileItemIterator getItemIterator(RequestContext ctx)
    throws FileUploadException, IOException { // definition of b
        try {
            return new FileItemIteratorImpl(ctx);
        } catch (FileUploadIOException e) {
            // unwrap encapsulated SizeException
            throw (FileUploadException) e.getCause();
        }
    }

    public List<FileItem> parseRequest(RequestContext ctx)
        throws FileUploadException { // called from StreamingTest
        List<FileItem> items = new ArrayList<FileItem>();
        boolean successful = false;
        try {
            FileItemIterator iter = getItemIterator(ctx); // call to b
            // NOTE: delayed call occurs here, the state is restored just in time
            FileItemFactory fac = getFileItemFactory(); // retrieves value set by a
            final byte[] buffer = new byte[Streams.DEFAULT_BUFFER_SIZE];
            if (fac == null) {
                throw new NullPointerException("No FileItemFactory has been set."); // if state was not restored, this exception would have been thrown
            }
            ...
        }
        ...
     }

}

public class StreamingTest {
    public void testFILEUPLOAD135()
            throws IOException, FileUploadException { // invalidated test
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
        Iterator<FileItem> fileIter = fileItems.iterator();
        assertTrue(fileIter.hasNext());
        FileItem item = fileIter.next();
        assertEquals("field", item.getFieldName());
        byte[] bytes = item.get();
        assertEquals(3, bytes.length);
        assertEquals((byte)'1', bytes[0]);
        assertEquals((byte)'2', bytes[1]);
        assertEquals((byte)'3', bytes[2]);
        assertTrue(!fileIter.hasNext());
    }

    private List<FileItem> parseUpload(InputStream pStream, int pLength)
            throws FileUploadException {
        String contentType = "multipart/form-data; boundary=---1234";

        FileUploadBase upload = new ServletFileUpload();
        upload.setFileItemFactory(new DiskFileItemFactory()); // call to a
        HttpServletRequest request = new MockHttpServletRequest(pStream,
                pLength, contentType);

        List<FileItem> fileItems = upload.parseRequest(new ServletRequestContext(request)); // calls b
        return fileItems;
    }

    public void testIOException()
            throws IOException { // validated test
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
}
