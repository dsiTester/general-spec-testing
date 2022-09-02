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
     * Retrieves the boundary from the <code>Content-type</code> header.
     *
     * @param contentType The value of the content type header from which to
     *                    extract the boundary value.
     *
     * @return The boundary, as a byte array.
     */
    protected byte[] getBoundary(String contentType) { // definition of b
        ParameterParser parser = new ParameterParser();
        parser.setLowerCaseNames(true);
        // Parameter parser can handle null input
        Map<String, String> params = parser.parse(contentType, new char[] {';', ','});
        String boundaryStr = params.get("boundary");

        if (boundaryStr == null) {
            return null;
        }
        byte[] boundary;
        try {
            boundary = boundaryStr.getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            boundary = boundaryStr.getBytes(); // Intentionally falls back to default charset
        }
        return boundary;
    }

    public FileItemIterator getItemIterator(RequestContext ctx)
    throws FileUploadException, IOException { // called from ServletFileUpload.getItemIterator()
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
        boundary = getBoundary(contentType); // call to b
        // NOTE: state restoration happened here
        if (boundary == null) {
            IOUtils.closeQuietly(input); // avoid possible resource leak
            throw new FileUploadException("the request was rejected because no multipart boundary was found");
        }

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
        return super.getItemIterator(new ServletRequestContext(request)); // calls b
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
