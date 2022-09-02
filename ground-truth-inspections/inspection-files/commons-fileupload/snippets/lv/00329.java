public class FileUploadBase {
    /**
     * Sets the progress listener.
     *
     * @param pListener The progress listener, if any. Defaults to null.
     */
    public void setProgressListener(ProgressListener pListener) { // definition of a; not defined in ServletFileUpload
        listener = pListener;
    }

    /**
     * Processes an <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>
     * compliant <code>multipart/form-data</code> stream.
     *
     * @param ctx The context for the request to be parsed.
     *
     * @return An iterator to instances of <code>FileItemStream</code>
     *         parsed from the request, in the order that they were
     *         transmitted.
     *
     * @throws FileUploadException if there are problems reading/parsing
     *                             the request or storing files.
     * @throws IOException An I/O error occurred. This may be a network
     *   error while communicating with the client or a problem while
     *   storing the uploaded content.
     */
    public FileItemIterator getItemIterator(RequestContext ctx)
    throws FileUploadException, IOException { // definition of b
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
        throws FileUploadException, IOException { // called from b
        ...
        notifier = new MultipartStream.ProgressNotifier(listener, requestSize); // this bootstraps the listener so it can be notified of events
        try {
            multi = new MultipartStream(input, boundary, notifier);
        } catch (IllegalArgumentException iae) {
            IOUtils.closeQuietly(input); // avoid possible resource leak
            throw new InvalidContentTypeException(
                                                  format("The boundary specified in the %s header is too long", CONTENT_TYPE), iae);
        }
        ...
    }

}

public class ServletFileUpload extends FileUpload {

    public FileItemIterator getItemIterator(HttpServletRequest request)
    throws FileUploadException, IOException { // called from ProgressListenerTest.runTest()
        return super.getItemIterator(new ServletRequestContext(request)); // call to b
    }


}

public class ProgressListenerTest {
    /**
     * Parse a very long file upload by using a progress listener.
     */
    @Test
    public void testProgressListener() throws Exception {
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
        upload.setProgressListener(listener); // call to a
        FileItemIterator iter = upload.getItemIterator(request); // calls b
        for (int i = 0;  i < NUM_ITEMS;  i++) {
            FileItemStream stream = iter.next();
            InputStream istream = stream.openStream();
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
        listener.checkFinished(); // NullPointerException here - check below
    }

    private class ProgressListenerImpl implements ProgressListener {

        private final long expectedContentLength;

        private final int expectedItems;

        private Long bytesRead;

        private Integer items;

        ProgressListenerImpl(long pContentLength, int pItems) {
            expectedContentLength = pContentLength;
            expectedItems = pItems;
        }

        void checkFinished(){ // throw NullPointerException becuase bytesRead and items were not set when method-a was called after method-b.
            assertEquals(expectedContentLength, bytesRead.longValue());
            assertEquals(expectedItems, items.intValue());
        }

    }

}
