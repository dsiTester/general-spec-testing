public class DiskFileUploadTest {
    @Test
    public void testMoveFile() throws Exception {
        DiskFileUpload myUpload = new DiskFileUpload();
        myUpload.setSizeThreshold(0); // call to a
    	final String content = ...;
    	final byte[] contentBytes = content.getBytes("US-ASCII");
        final HttpServletRequest request = new MockHttpServletRequest(contentBytes, Constants.CONTENT_TYPE);
        final List<FileItem> items = myUpload.parseRequest(request); // calls b
        assertNotNull(items);
        ...
    }
}

public class DiskFileUpload {
    @Deprecated
    public void setSizeThreshold(int sizeThreshold) { // definition of a
        fileItemFactory.setSizeThreshold(sizeThreshold); // calls DiskFileItemFactory.setSizeThreshold()
    }
}

public class FileUploadBase {
    public List<FileItem> parseRequest(RequestContext ctx)
            throws FileUploadException {
        try {
            FileItemIterator iter = getItemIterator(ctx); // call to
            ...
        }
        ...
    }

    public FileItemIterator getItemIterator(RequestContext ctx)
    throws FileUploadException, IOException { // definition of b
        try {
            return new FileItemIteratorImpl(ctx); // calls b
        } ...
    }

}
