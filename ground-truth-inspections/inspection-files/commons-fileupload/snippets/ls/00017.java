public class DiskFileUploadTest {
    @Test
    public void testMoveFile() throws Exception {
        DiskFileUpload myUpload = new DiskFileUpload();
        myUpload.setSizeThreshold(0); // call to a
    	final String content = ...;
    	final byte[] contentBytes = content.getBytes("US-ASCII");
        final HttpServletRequest request = new MockHttpServletRequest(contentBytes, Constants.CONTENT_TYPE);
        final List<FileItem> items = myUpload.parseRequest(request); // callto b
        assertNotNull(items);
        ...
    }
}


public class DiskFileUpload {
    @Deprecated
    public void setSizeThreshold(int sizeThreshold) { // definition of a
        fileItemFactory.setSizeThreshold(sizeThreshold); // calls DiskFileItemFactory.setSizeThreshold()
    }

    @Deprecated
    public List<FileItem> parseRequest(HttpServletRequest req, int sizeThreshold, long sizeMax, String path)
        throws FileUploadException {
        ...
        return parseRequest(req);
    }
}

public class FileUploadBase {

    @Deprecated
    public List<FileItem> parseRequest(HttpServletRequest req)
    throws FileUploadException { // definition of b
        return parseRequest(new ServletRequestContext(req));
    }
}
