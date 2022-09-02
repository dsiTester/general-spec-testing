public class FileItemStream {
    InputStream openStream() throws IOException; // a
}

public class FileItemStreamImpl {
    @Override
    public InputStream openStream() throws IOException { // only implementation of a
        if (opened) {
            throw new IllegalStateException(
                                            "The stream was already opened.");
        }
        if (((Closeable) stream).isClosed()) {
            throw new FileItemStream.ItemSkippedException();
        }
        return stream;
    }

    void close() throws IOException { // definition of b
        stream.close();
    }
}

public class FileItemIteratorImpl implements FileItemIterator {
    @Override
    public FileItemStream next() throws FileUploadException, IOException { // calls b
        // the first iteration in ProgressListenerTest.runTest, itemValid is true so the conditional exits with a short circuit
        if (eof  ||  (!itemValid && !hasNext())) { // hasNext() calls b
            throw new NoSuchElementException();
        }
        itemValid = false;
        return currentItem;
    }

    @Override
    public boolean hasNext() throws FileUploadException, IOException {
        if (eof) {
            return false;
        }
        if (itemValid) {
            return true;
        }
        try {
            return findNextItem(); // calls b
        } catch (FileUploadIOException e) {
            // unwrap encapsulated SizeException
            throw (FileUploadException) e.getCause();
        }
    }

    private boolean findNextItem() throws IOException {
        if (eof) {
            return false;
        }
        if (currentItem != null) {
            currentItem.close(); // call to b
            currentItem = null;
        }
        for (;;) {
            boolean nextPart;
            ...
                }
        ...
    }

}

public class ProgressListenerTest {
    @Test
    public void testProgressListener() throws Exception {
        final int NUM_ITEMS = 512;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ...
        MockHttpServletRequest request = new MockHttpServletRequest(contents, Constants.CONTENT_TYPE);
        runTest(NUM_ITEMS, contents.length, request); // calls a and b
        request = new MockHttpServletRequest(contents, Constants.CONTENT_TYPE){
            @Override
            public int getContentLength() {
                return -1;
            }
        };
        runTest(NUM_ITEMS, contents.length, request); // calls a and b
    }

    private void runTest(final int NUM_ITEMS, long pContentLength, MockHttpServletRequest request) throws FileUploadException, IOException {
        ServletFileUpload upload = new ServletFileUpload();
        ProgressListenerImpl listener = new ProgressListenerImpl(pContentLength, NUM_ITEMS);
        upload.setProgressListener(listener);
        FileItemIterator iter = upload.getItemIterator(request);
        for (int i = 0;  i < NUM_ITEMS;  i++) {
            FileItemStream stream = iter.next(); // calls b STARTING THE SECOND ITERATION
            InputStream istream = stream.openStream(); // call to a
            for (int j = 0;  j < 16384+i;  j++) {
                /**
                 * This used to be
                 *     assertEquals((byte) j, (byte) istream.read());
                 * but this seems to trigger a bug in JRockit, so
                 * we express the same like this:
                 */
                byte b1 = (byte) j;
                byte b2 = (byte) istream.read();
                if (b1 != b2) {
                    fail("Expected " + b1 + ", got " + b2);
                }
            }
            assertEquals(-1, istream.read());
            istream.close();
        }
        assertTrue(!iter.hasNext());
        listener.checkFinished();
    }
}

