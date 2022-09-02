// FileUpload extends FileUploadBase
public class ServletFileUpload extends FileUpload {
    @Override
    public List<FileItem> parseRequest(HttpServletRequest request)
    throws FileUploadException { // definition of a
        return parseRequest(new ServletRequestContext(request));
    }
}

public class FileUploadBase {

    /**
     * Retrieves the file name from the <code>Content-disposition</code>
     * header.
     *
     * @param headers A <code>Map</code> containing the HTTP request headers.
     *
     * @return The file name for the current <code>encapsulation</code>.
     * @deprecated 1.2.1 Use {@link #getFileName(FileItemHeaders)}.
     */
    @Deprecated
    protected String getFileName(Map<String, String> headers) { // definition of b
        return getFileName(getHeader(headers, CONTENT_DISPOSITION));
    }

    public List<FileItem> parseRequest(RequestContext ctx)
            throws FileUploadException { // called from a
        List<FileItem> items = new ArrayList<FileItem>();
        boolean successful = false;
        try {
            FileItemIterator iter = getItemIterator(ctx); // calls b
            ...
        }
        ...
    }

    public FileItemIterator getItemIterator(RequestContext ctx)
    throws FileUploadException, IOException {
        try {
            return new FileItemIteratorImpl(ctx); // calls b
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
        findNextItem(); // calls b
    }

    private boolean findNextItem() throws IOException {
        ...
        for (;;) {
            ...
            FileItemHeaders headers = getParsedHeaders(multi.readHeaders());
            if (currentFieldName == null) {
                // We're parsing the outer multipart
                String fieldName = getFieldName(headers);
                if (fieldName != null) {
                    ...
                    String fileName = getFileName(headers); // call to b
                    currentItem = new FileItemStreamImpl(fileName,
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

public class SizesTest {
    @Test
    public void testFileSizeLimit()
            throws IOException, FileUploadException {
        ...

        ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
        ...
        HttpServletRequest req = new MockHttpServletRequest(
                request.getBytes("US-ASCII"), Constants.CONTENT_TYPE);
        List<FileItem> fileItems = upload.parseRequest(req); // call to a
        ...

        upload = new ServletFileUpload(new DiskFileItemFactory());
        ...
        req = new MockHttpServletRequest(request.getBytes("US-ASCII"), Constants.CONTENT_TYPE);
        fileItems = upload.parseRequest(req); // call to a
        ...

        upload = new ServletFileUpload(new DiskFileItemFactory());
        ...
        req = new MockHttpServletRequest(request.getBytes("US-ASCII"), Constants.CONTENT_TYPE);
        try {
            upload.parseRequest(req); // call to a
            fail("Expected exception.");
        } catch (FileUploadBase.FileSizeLimitExceededException e) {
            assertEquals(30, e.getPermittedSize());
        }
    }
}
