public class ItemInputStream extends InputStream implements Closeable {

    public boolean skipPreamble() throws IOException { // called from FileItemIteratorImpl
        // First delimiter may be not preceded with a CRLF.
        ...
        try {
            // Discard all data up to the delimiter.
            discardBodyData(); // call to a

            // Read boundary - if succeeded, the stream contains an
            // encapsulation.
            return readBoundary();
        } ...
    }

    public int discardBodyData() throws MalformedStreamException, IOException { // definition of a
        return readBodyData(null); // calls b
    }

    public int readBodyData(OutputStream output)
        throws MalformedStreamException, IOException {
        return (int) Streams.copy(newInputStream(), output, false); // newInputStream calls b
    }

    ItemInputStream newInputStream() { // calls b
        return new ItemInputStream();
    }

    ItemInputStream() { // calls b
        findSeparator();
    }

    private void findSeparator() { // definition of b
        pos = MultipartStream.this.findSeparator();
        if (pos == -1) {
            if (tail - head > keepRegion) {
                pad = keepRegion;
            } else {
                pad = tail - head;
            }
        }
    }

}

public class FileUploadBase {

    public List<FileItem> parseRequest(RequestContext ctx)
        throws FileUploadException { // called from StreamingTest
        List<FileItem> items = new ArrayList<FileItem>();
        boolean successful = false;
        try {
            FileItemIterator iter = getItemIterator(ctx); // calls a and b
            FileItemFactory fac = getFileItemFactory();
            final byte[] buffer = new byte[Streams.DEFAULT_BUFFER_SIZE];
            if (fac == null) {
                throw new NullPointerException("No FileItemFactory has been set.");
            }
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

    private class FileItemIteratorImpl implements FileItemIterator {
        FileItemIteratorImpl(RequestContext ctx)
            throws FileUploadException, IOException {
            ...
            findNextItem(); // calls a and b
        }

        private boolean findNextItem() throws IOException { // calls a and b
            ...
            for (;;) {
                ...
                if (skipPreamble) {
                    nextPart = multi.skipPreamble(); // calls a and b
                }
                ...
             }
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
}
