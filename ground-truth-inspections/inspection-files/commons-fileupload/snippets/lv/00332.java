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
    throws FileUploadException, IOException { // definition of b; b is not defined in FileUpload
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
    public FileItemIterator getItemIterator(HttpServletRequest request)
    throws FileUploadException, IOException { // called from test
        return super.getItemIterator(new ServletRequestContext(request)); // call to b
    }
}

public class SizesTest {
    @Test
    public void testMaxSizeLimitUnknownContentLength()
            throws IOException, FileUploadException {
        final String request =
            "-----1234\r\n" +
            "Content-Disposition: form-data; name=\"file1\"; filename=\"foo1.tab\"\r\n" +
            "Content-Type: text/whatever\r\n" +
            "Content-Length: 10\r\n" +
            "\r\n" +
            "This is the content of the file\n" +
            "\r\n" +
            "-----1234\r\n" +
            "Content-Disposition: form-data; name=\"file2\"; filename=\"foo2.tab\"\r\n" +
            "Content-Type: text/whatever\r\n" +
            "\r\n" +
            "This is the content of the file\n" +
            "\r\n" +
            "-----1234--\r\n";

        ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setFileSizeMax(-1);
        upload.setSizeMax(300); // call to a

        ...
        FileItemIterator it = upload.getItemIterator(req); // calls b
        assertTrue(it.hasNext());

        FileItemStream item = it.next();
        ...
        item = it.next();

        try {
            InputStream stream = item.openStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Streams.copy(stream, baos, true); // FileUploadException supposed to be thrown here
            fail(); // assertion fails here because expected exception was not thrown
        } catch (FileUploadIOException e) {
            // expected
        }
    }

}
