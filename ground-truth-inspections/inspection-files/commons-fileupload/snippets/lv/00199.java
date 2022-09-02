public class ItemInputStream extends InputStream implements Closeable {
    private void findSeparator() { // definition of a
        pos = MultipartStream.this.findSeparator();
        if (pos == -1) {
            if (tail - head > keepRegion) {
                pad = keepRegion;
            } else {
                pad = tail - head;
            }
        }
    }

    public void close(boolean pCloseUnderlying) throws IOException { // definition of b
        if (closed) {
            return;
        }
        if (pCloseUnderlying) {
            closed = true;
            input.close();
        } else {
            for (;;) {
                int av = available();
                if (av == 0) {
                    av = makeAvailable();
                    if (av == 0) {
                        break;
                    }
                }
                skip(av);
            }
        }
        closed = true;
    }

    public boolean skipPreamble() throws IOException { // called from FileItemIteratorImpl
        // First delimiter may be not preceded with a CRLF.
        ...
        try {
            // Discard all data up to the delimiter.
            discardBodyData(); // calls a

            // Read boundary - if succeeded, the stream contains an
            // encapsulation.
            return readBoundary();
        } ...
    }

    public int discardBodyData() throws MalformedStreamException, IOException {
        return readBodyData(null); // calls a
    }

    public int readBodyData(OutputStream output)
        throws MalformedStreamException, IOException {
        return (int) Streams.copy(newInputStream(), output, false); // calls a; Streams.copy() calls b
    }

    ItemInputStream newInputStream() { // calls a
        return new ItemInputStream();
    }

    ItemInputStream() { // calls a
        findSeparator();        // call to a
    }

    @Override
    public void close() throws IOException { // calls b
        close(false); // call to b
    }
}

public class FileUploadBase {

    public List<FileItem> parseRequest(RequestContext ctx)
        throws FileUploadException { // called from StreamingTest
        List<FileItem> items = new ArrayList<FileItem>();
        boolean successful = false;
        try {
            FileItemIterator iter = getItemIterator(ctx); // calls a
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
            return new FileItemIteratorImpl(ctx);
        } catch (FileUploadIOException e) {
            // unwrap encapsulated SizeException
            throw (FileUploadException) e.getCause();
        }
    }

}

private class FileItemIteratorImpl {
    FileItemIteratorImpl(RequestContext ctx)
                throws FileUploadException, IOException {
        ...
        findNextItem();
    }

    private boolean findNextItem() throws IOException {
        ...
        for (;;) {
            ...
            if (skipPreamble) {
                nextPart = multi.skipPreamble(); // calls a
            }
            ...
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
