public interface RequestContext {

    /**
     * Retrieve the input stream for the request.
     *
     * @return The input stream for the request.
     *
     * @throws IOException if a problem occurs.
     */
    InputStream getInputStream() throws IOException; // a

    /**
     * Retrieve the character encoding for the request.
     *
     * @return The character encoding for the request.
     */
    String getCharacterEncoding(); // b
}

public interface UploadContext extends RequestContext {
    ...
}

public class ServletRequestContext implements UploadContext {

    /**
     * Retrieve the input stream for the request.
     *
     * @return The input stream for the request.
     *
     * @throws IOException if a problem occurs.
     */
    @Override
    public InputStream getInputStream() throws IOException { // used implementation of a
        return request.getInputStream();
    }

    /**
     * Retrieve the character encoding for the request.
     *
     * @return The character encoding for the request.
     */
    @Override
    public String getCharacterEncoding() { // used implementation of b
        return request.getCharacterEncoding();
    }
}

public class FileUploadBase {
    @Deprecated
    public List<FileItem> parseRequest(HttpServletRequest req)
        throws FileUploadException {
        return parseRequest(new ServletRequestContext(req));
    }

    public List<FileItem> parseRequest(RequestContext ctx)
            throws FileUploadException {
        List<FileItem> items = new ArrayList<FileItem>();
        boolean successful = false;
        try {
            FileItemIterator iter = getItemIterator(ctx); // calls a and b
            ...
        }
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

public class FileItemIteratorImpl {
    FileItemIteratorImpl(RequestContext ctx)
        throws FileUploadException, IOException {
        ...
        InputStream input; // N.B. this is eventually closed in MultipartStream processing
        if (sizeMax >= 0) {
            ...
        } else {
            input = ctx.getInputStream(); // call to a
            // NOTE: replace the above with the below to demonstrate that this is a spurious spec
            // input = null;
        }

        String charEncoding = headerEncoding;
        if (charEncoding == null) {
            charEncoding = ctx.getCharacterEncoding(); // call to b
        }
        // NOTE: uncomment the following to demonstrate that this is a spurious spec
        // input = ctx.getInputStream();
        ...
    }
}

public class DiskFileUploadTest {
    /** Proposed test for FILEUPLOAD-293. As of yet, doesn't reproduce the problem.
     */
    @Test
    public void testMoveFile() throws Exception { // validated test
        DiskFileUpload myUpload = new DiskFileUpload();
        myUpload.setSizeThreshold(0);
    	final String content = 
            "-----1234\r\n" +
            "Content-Disposition: form-data; name=\"file\";"
            + "filename=\"foo.tab\"\r\n" +
            "Content-Type: text/whatever\r\n" +
            "\r\n" +
            "This is the content of the file\n" +
            "\r\n" +
            "-----1234--\r\n";
    	final byte[] contentBytes = content.getBytes("US-ASCII");
        final HttpServletRequest request = new MockHttpServletRequest(contentBytes, Constants.CONTENT_TYPE);
        final List<FileItem> items = myUpload.parseRequest(request); // calls a and b
        assertNotNull(items);
        assertFalse(items.isEmpty());
        final DiskFileItem dfi = (DiskFileItem) items.get(0);
        final File out = File.createTempFile("install", ".tmp");
        dfi.write(out);
    }

    @Test
    public void testWithInvalidRequest() { // invalidated test
        HttpServletRequest req = HttpServletRequestFactory.createInvalidHttpServletRequest();

        try {
            upload.parseRequest(req); // calls a and b
            fail("testWithInvalidRequest: expected exception was not thrown");
        } catch (FileUploadException expected) {
            // this exception is expected
        }
    }
}
