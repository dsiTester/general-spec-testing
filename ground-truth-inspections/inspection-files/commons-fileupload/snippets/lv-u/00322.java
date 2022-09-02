public class FileUploadBase {
    /**
     * Sets the maximum allowed size of a single uploaded file,
     * as opposed to {@link #getSizeMax()}.
     *
     * @see #getFileSizeMax()
     * @param fileSizeMax Maximum size of a single uploaded file.
     */
    public void setFileSizeMax(long fileSizeMax) { // definition of a; a is not defined in ServletFileUpload
        this.fileSizeMax = fileSizeMax;
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

    public List<FileItem> parseRequest(RequestContext ctx)
        throws FileUploadException {
        List<FileItem> items = new ArrayList<FileItem>();
        boolean successful = false;
        try {
            FileItemIterator iter = getItemIterator(ctx); // call to b
            ...
        }
        ...
    }

    class FileItemStreamImpl implements FileItemStream {
        FileItemStreamImpl(String pName, String pFieldName,
                    String pContentType, boolean pFormField,
                    long pContentLength) throws IOException { // indirectly called from method-b
            ...
            if (fileSizeMax != -1) { // Check if limit is already exceeded
                if (pContentLength != -1
                    && pContentLength > fileSizeMax) {
                    FileSizeLimitExceededException e =
                        new FileSizeLimitExceededException(
                                                           format("The field %s exceeds its maximum permitted size of %s bytes.",
                                                                  fieldName, Long.valueOf(fileSizeMax)),
                                                           pContentLength, fileSizeMax);
                    ...
                        throw new FileUploadIOException(e); // throws expected exception from validated-test here
                }
            }
            // OK to construct stream now
            final ItemInputStream itemStream = multi.newInputStream();
            InputStream istream = itemStream;
            if (fileSizeMax != -1) {
                istream = new LimitedInputStream(istream, fileSizeMax) {
                        @Override
                        protected void raiseError(long pSizeMax, long pCount)
                            throws IOException {
                            itemStream.close(true);
                            FileSizeLimitExceededException e =
                                new FileSizeLimitExceededException(
                                                                   format("The field %s exceeds its maximum permitted size of %s bytes.",
                                                                          fieldName, Long.valueOf(pSizeMax)),
                                                                   pCount, pSizeMax);
                            ...
                            throw new FileUploadIOException(e);
                        }
                    };
            }
            stream = istream;
        }
    }
}

// FileUpload extends FileUploadBase
public class ServletFileUpload extends FileUpload {
    @Override
    public List<FileItem> parseRequest(HttpServletRequest request)
        throws FileUploadException { // called from tests
        return parseRequest(new ServletRequestContext(request)); // calls b
    }
}

public class SizesTest {
    /** Checks, whether limiting the file size works.
     */
    @Test
    public void testFileSizeLimit()
        throws IOException, FileUploadException { // validated test
        final String request = ...;

        ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setFileSizeMax(-1); // call to a
        HttpServletRequest req = new MockHttpServletRequest(
                                                            request.getBytes("US-ASCII"), Constants.CONTENT_TYPE);
        List<FileItem> fileItems = upload.parseRequest(req); // calls b
        ...

        upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setFileSizeMax(40); // call to a
        req = new MockHttpServletRequest(request.getBytes("US-ASCII"), Constants.CONTENT_TYPE);
        fileItems = upload.parseRequest(req); // calls b
        ...

        upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setFileSizeMax(30); // call to a
        req = new MockHttpServletRequest(request.getBytes("US-ASCII"), Constants.CONTENT_TYPE);
        try {
            upload.parseRequest(req); // calls b
            fail("Expected exception."); // assertion fails here because expected exception was not thrown
        } catch (FileUploadBase.FileSizeLimitExceededException e) {
            assertEquals(30, e.getPermittedSize());
        }
    }

    /** Checks, whether a faked Content-Length header is detected.
     */
    @Test
    public void testFileSizeLimitWithFakedContentLength()
            throws IOException, FileUploadException { // unknown verdict test
        final String request = ...;

        ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setFileSizeMax(-1); // call to a
        HttpServletRequest req = new MockHttpServletRequest(
                request.getBytes("US-ASCII"), Constants.CONTENT_TYPE);
        List<FileItem> fileItems = upload.parseRequest(req); // calls b
        ...

        upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setFileSizeMax(40); // call to a
        req = new MockHttpServletRequest(request.getBytes("US-ASCII"), Constants.CONTENT_TYPE);
        fileItems = upload.parseRequest(req); // calls b
        ...

        // provided Content-Length is larger than the FileSizeMax -> handled by ctor
        upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setFileSizeMax(5); // call to a
        req = new MockHttpServletRequest(request.getBytes("US-ASCII"), Constants.CONTENT_TYPE);
        try {
            upload.parseRequest(req); // calls b
            fail("Expected exception."); // assertion fails because expected exception was not thrown
        } catch (FileUploadBase.FileSizeLimitExceededException e) {
            assertEquals(5, e.getPermittedSize());
        }

        // provided Content-Length is wrong, actual content is larger -> handled by LimitedInputStream
        upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setFileSizeMax(15); // call to a; however DSI experiment couldn't get to this point.
        req = new MockHttpServletRequest(request.getBytes("US-ASCII"), Constants.CONTENT_TYPE);
        try {
            upload.parseRequest(req); // calls b; however DSI experiment couldn't get to this point.
            fail("Expected exception.");
        } catch (FileUploadBase.FileSizeLimitExceededException e) {
            assertEquals(15, e.getPermittedSize());
        }
    }

}
