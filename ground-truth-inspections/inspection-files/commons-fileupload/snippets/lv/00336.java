public class FileUploadBase {
    /**
     * Sets the maximum allowed size of a complete request, as opposed
     * to {@link #setFileSizeMax(long)}.
     *
     * @param sizeMax The maximum allowed size, in bytes. The default value of
     *   -1 indicates, that there is no limit.
     *
     * @see #getSizeMax()
     *
     */
    public void setSizeMax(long sizeMax) { // definition of a; a is not defined in ServletFileUpload
        this.sizeMax = sizeMax;
    }

    public List<FileItem> parseRequest(RequestContext ctx)
        throws FileUploadException { // called from b
        List<FileItem> items = new ArrayList<FileItem>();
        boolean successful = false;
        try {
            FileItemIterator iter = getItemIterator(ctx);
            ...
        }
        ...
    }

    public FileItemIterator getItemIterator(RequestContext ctx)
    throws FileUploadException, IOException { // called from above
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
        throws FileUploadException, IOException { // called from FileUploadBase.getItemIterator()
        ...
        if (sizeMax >= 0) { // where calling method-a is important
            if (requestSize != -1 && requestSize > sizeMax) {
                throw new SizeLimitExceededException(
                                                     format("the request was rejected because its size (%s) exceeds the configured maximum (%s)",
                                                            Long.valueOf(requestSize), Long.valueOf(sizeMax)),
                                                     requestSize, sizeMax);
            }
            // N.B. this is eventually closed in MultipartStream processing
            input = new LimitedInputStream(ctx.getInputStream(), sizeMax) {
                    @Override
                    protected void raiseError(long pSizeMax, long pCount)
                        throws IOException {
                        FileUploadException ex = new SizeLimitExceededException(
                                                                                format("the request was rejected because its size (%s) exceeds the configured maximum (%s)",
                                                                                       Long.valueOf(pCount), Long.valueOf(pSizeMax)),
                                                                                pCount, pSizeMax);
                        throw new FileUploadIOException(ex); // test expects this exception
                    }
                };
        } else {
            input = ctx.getInputStream();
        }
        ...
    }

}

public class ServletFileUpload extends FileUpload {
    /**
     * Processes an <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>
     * compliant <code>multipart/form-data</code> stream.
     *
     * @param request The servlet request to be parsed.
     *
     * @return A list of <code>FileItem</code> instances parsed from the
     *         request, in the order that they were transmitted.
     *
     * @throws FileUploadException if there are problems reading/parsing
     *                             the request or storing files.
     */
    @Override
    public List<FileItem> parseRequest(HttpServletRequest request)
    throws FileUploadException { // definition of b
        return parseRequest(new ServletRequestContext(request));
    }
}

public class SizesTest {
    @Test
    public void testMaxSizeLimit()
            throws IOException, FileUploadException {
        final String request = ...;

        ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setFileSizeMax(-1);
        upload.setSizeMax(200); // call to a

        MockHttpServletRequest req = new MockHttpServletRequest(
                request.getBytes("US-ASCII"), Constants.CONTENT_TYPE);
        try {
            upload.parseRequest(req); // call to b
            fail("Expected exception.");
        } catch (FileUploadBase.SizeLimitExceededException e) {
            assertEquals(200, e.getPermittedSize());
        }
    }
}
