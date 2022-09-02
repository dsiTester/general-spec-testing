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
    throws FileUploadException, IOException { // called from ServletFileUpload
        try {
            return new FileItemIteratorImpl(ctx); // calls b
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
        boundary = getBoundary(contentType); // call to b
        if (boundary == null) {
            IOUtils.closeQuietly(input); // avoid possible resource leak
            throw new FileUploadException("the request was rejected because no multipart boundary was found");
        }
        ...
    }
}

public class ServletFileUpload extends FileUpload {
    public FileItemIterator getItemIterator(HttpServletRequest request)
    throws FileUploadException, IOException { // called from test
        return super.getItemIterator(new ServletRequestContext(request)); // calls b
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
