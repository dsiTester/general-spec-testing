public interface RequestContext {
    /**
     * Retrieve the content type of the request.
     *
     * @return The content type of the request.
     */
    String getContentType(); // a

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
     * Retrieve the content type of the request.
     *
     * @return The content type of the request.
     */
    @Override
    public String getContentType() { // used implementation of a
        return request.getContentType();
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

        ...
        String charEncoding = headerEncoding;
        if (charEncoding == null) {
            charEncoding = ctx.getCharacterEncoding(); // call to b
        }
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
