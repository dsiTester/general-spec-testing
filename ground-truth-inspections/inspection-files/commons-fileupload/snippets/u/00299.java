public class ServletFileUpload extends FileUpload {
    @Override
    public List<FileItem> parseRequest(HttpServletRequest request)
    throws FileUploadException { // definition of a
        return parseRequest(new ServletRequestContext(request));
    }

}

public class FileUploadBase {

    /**
     * Returns the factory class used when creating file items.
     *
     * @return The factory class for new file items.
     */
    public abstract FileItemFactory getFileItemFactory(); // b

    public List<FileItem> parseRequest(RequestContext ctx)
            throws FileUploadException { // called from a
        List<FileItem> items = new ArrayList<FileItem>();
        boolean successful = false;
        try {
            FileItemIterator iter = getItemIterator(ctx);
            FileItemFactory fac = getFileItemFactory(); // call to b
            final byte[] buffer = new byte[Streams.DEFAULT_BUFFER_SIZE];
            if (fac == null) {
                throw new NullPointerException("No FileItemFactory has been set.");
            }
            ...
        }
        ...
    }

}

public class FileUpload extends FileUploadBase {
    /**
     * Returns the factory class used when creating file items.
     *
     * @return The factory class for new file items.
     */
    @Override
    public FileItemFactory getFileItemFactory() { // used implementation of b
        return fileItemFactory;
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
