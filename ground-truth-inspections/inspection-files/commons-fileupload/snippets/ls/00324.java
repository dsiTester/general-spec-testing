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
     * Creates a new instance of {@link FileItemHeaders}.
     * @return The new instance.
     */
    protected FileItemHeadersImpl newFileItemHeaders() { // definition of b
        return new FileItemHeadersImpl();
    }

    public FileItemIterator getItemIterator(RequestContext ctx)
    throws FileUploadException, IOException { // calls b
        try {
            return new FileItemIteratorImpl(ctx);
        } catch (FileUploadIOException e) {
            // unwrap encapsulated SizeException
            throw (FileUploadException) e.getCause();
        }
    }

    protected FileItemHeaders getParsedHeaders(String headerPart) { // called from FileItemIteratorImpl.findNextItem()
        final int len = headerPart.length();
        FileItemHeadersImpl headers = newFileItemHeaders(); // call to b
        ...
        for (;;) {
            ...
            parseHeaderLine(headers, header.toString());
        }
        return headers;
    }
}

public class FileItemIteratorImpl {
    FileItemIteratorImpl(RequestContext ctx)
        throws FileUploadException, IOException {
        ...
        findNextItem(); // calls b
    }

    private boolean findNextItem() throws IOException {
        ...
        for (;;) {
            ...
            FileItemHeaders headers = getParsedHeaders(multi.readHeaders()); // calls b
            if (currentFieldName == null) {
                // We're parsing the outer multipart
                String fieldName = getFieldName(headers);
                if (fieldName != null) {
                    ...
                    String fileName = getFileName(headers);
                    currentItem = new FileItemStreamImpl(fileName, // not calling a before this in the latter two context sets prevents an exception from being thrown
                                                         fieldName, headers.getHeader(CONTENT_TYPE),
                                                         fileName == null, getContentLength(headers));
                    ...
                }
                ...
            }
            ...
        }
        ...
    }
}

// FileUpload extends FileUploadBase
public class ServletFileUpload extends FileUpload {
    public FileItemIterator getItemIterator(HttpServletRequest request)
        throws FileUploadException, IOException { // called from test
        return super.getItemIterator(new ServletRequestContext(request)); // calls b
    }
}

public class SizesTest {
    @Test
    public void testFileSizeLimitWithFakedContentLength()
            throws IOException, FileUploadException {
        final String request = ...;
        ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setFileSizeMax(-1); // call to a
        ...
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
            fail("Expected exception.");
        } catch (FileUploadBase.FileSizeLimitExceededException e) {
            assertEquals(5, e.getPermittedSize());
        }

        // provided Content-Length is wrong, actual content is larger -> handled by LimitedInputStream
        upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setFileSizeMax(15); // call to a
        req = new MockHttpServletRequest(request.getBytes("US-ASCII"), Constants.CONTENT_TYPE);
        try {
            upload.parseRequest(req); // calls b
            fail("Expected exception.");
        } catch (FileUploadBase.FileSizeLimitExceededException e) {
            assertEquals(15, e.getPermittedSize());
        }
    }
}
