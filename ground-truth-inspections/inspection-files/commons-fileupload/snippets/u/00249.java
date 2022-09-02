public interface RequestContext {

    /**
     * Retrieve the content type of the request.
     *
     * @return The content type of the request.
     */
    String getContentType(); // a

}

public interface UploadContext extends RequestContext {
    /**
     * Retrieve the content length of the request.
     *
     * @return The content length of the request.
     * @since 1.3
     */
    long contentLength(); // b
}

public class ServletRequestContext implements UploadContext {

    /**
     * Retrieve the content type of the request.
     *
     * @return The content type of the request.
     */
    @Override
    public String getContentType() { // used implementation of a
        return request.getContentType();
    }

    /**
     * Retrieve the content length of the request.
     *
     * @return The content length of the request.
     * @since 1.3
     */
    @Override
    public long contentLength() { // used implementation of b
        long size;
        try {
            size = Long.parseLong(request.getHeader(FileUploadBase.CONTENT_LENGTH));
        } catch (NumberFormatException e) {
            size = request.getContentLength();
        }
        return size;
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
        if (ctx == null) {
            throw new NullPointerException("ctx parameter");
        }

        String contentType = ctx.getContentType(); // call to a
        if ((null == contentType)
            || (!contentType.toLowerCase(Locale.ENGLISH).startsWith(MULTIPART))) {
            throw new InvalidContentTypeException( // exception thrown here
                                                  format("the request doesn't contain a %s or %s stream, content type header is %s",
                                                         MULTIPART_FORM_DATA, MULTIPART_MIXED, contentType));
        }
        @SuppressWarnings("deprecation") // still has to be backward compatible
            final int contentLengthInt = ctx.getContentLength();

        final long requestSize = UploadContext.class.isAssignableFrom(ctx.getClass())
            // Inline conditional is OK here CHECKSTYLE:OFF
            ? ((UploadContext) ctx).contentLength() // call to b
            : contentLengthInt;
        // CHECKSTYLE:ON

        ...
    }
}

public class DiskFileUploadTest {
    /** Proposed test for FILEUPLOAD-293. As of yet, doesn't reproduce the problem.
     */
    @Test
    public void testMoveFile() throws Exception {
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
}
