public interface RequestContext {

    /**
     * Retrieve the content length of the request.
     *
     * @return The content length of the request.
     * @deprecated 1.3 Use {@link UploadContext#contentLength()} instead
     */
    @Deprecated
    int getContentLength(); // a

    /**
     * Retrieve the input stream for the request.
     *
     * @return The input stream for the request.
     *
     * @throws IOException if a problem occurs.
     */
    InputStream getInputStream() throws IOException; // b

}

public interface UploadContext extends RequestContext {
    ...
}

public class ServletRequestContext implements UploadContext {

    /**
     * Retrieve the content length of the request.
     *
     * @return The content length of the request.
     * @deprecated 1.3 Use {@link #contentLength()} instead
     */
    @Override
    @Deprecated
    public int getContentLength() { // used implementation of a
        return request.getContentLength();
    }

    @Override
    public InputStream getInputStream() throws IOException { // used implementation of b
        return request.getInputStream();
    }
}

public class FileUploadBase {
    @Deprecated
    public List<FileItem> parseRequest(HttpServletRequest req)
    throws FileUploadException { // called from test
        return parseRequest(new ServletRequestContext(req)); // calls a and b
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

private class FileItemIteratorImpl {
    FileItemIteratorImpl(RequestContext ctx)
        throws FileUploadException, IOException {
        ...
        @SuppressWarnings("deprecation") // still has to be backward compatible
            final int contentLengthInt = ctx.getContentLength(); // call to a
        // NOTE: below conditional expression essentially discards the return value of a
        final long requestSize = UploadContext.class.isAssignableFrom(ctx.getClass()) 
            // Inline conditional is OK here CHECKSTYLE:OFF
            ? ((UploadContext) ctx).contentLength()
            : contentLengthInt;
        // CHECKSTYLE:ON
        InputStream input; // N.B. this is eventually closed in MultipartStream processing
        if (sizeMax >= 0) {
            ...
        } else {
            input = ctx.getInputStream(); // call to b
        }
        ...
    }
}

public class DiskFileUploadTest {

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
