public class FileUploadBase {

    @Deprecated
    public List<FileItem> parseRequest(HttpServletRequest req) // definition of a
    throws FileUploadException {
        return parseRequest(new ServletRequestContext(req)); // calls b
    }

    public List<FileItem> parseRequest(RequestContext ctx)
            throws FileUploadException {
        try {
            ...
            FileItemFactory fac = getFileItemFactory(); // call to b
            ...
        }
        ...
    }

    /**
     * Returns the factory class used when creating file items.
     *
     * @return The factory class for new file items.
     */
    public abstract FileItemFactory getFileItemFactory(); // b
}

public class DiskFileUpload {
    @Override
    @Deprecated
    public FileItemFactory getFileItemFactory() { // used implementation of b
        return fileItemFactory;
    }
}


public class DiskFileUploadTest {
    @Test
    public void testMoveFile() throws Exception {
        ...
        final HttpServletRequest request = new MockHttpServletRequest(contentBytes, Constants.CONTENT_TYPE);
        final List<FileItem> items = myUpload.parseRequest(request); // call to a
        assertNotNull(items); // assertion fails here
        ...
    }
}
