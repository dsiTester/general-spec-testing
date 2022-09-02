public class FileUploadBase {
    public List<FileItem> parseRequest(RequestContext ctx)
        throws FileUploadException { // definition of a
        List<FileItem> items = new ArrayList<FileItem>();
        boolean successful = false;
        try {
            FileItemIterator iter = getItemIterator(ctx); // call to b
            ...
        }
        ...
     }

    public FileItemIterator getItemIterator(RequestContext ctx)
    throws FileUploadException, IOException { // definition of b
        try {
            return new FileItemIteratorImpl(ctx);
        } catch (FileUploadIOException e) {
            // unwrap encapsulated SizeException
            throw (FileUploadException) e.getCause();
        }
    }

}


public class SizesTest {
    @Test
    public void testFileUpload()
            throws IOException, FileUploadException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ...
        List<FileItem> fileItems =
                Util.parseUpload(new ServletFileUpload(new DiskFileItemFactory()), baos.toByteArray()); // calls a and b
        Iterator<FileItem> fileIter = fileItems.iterator(); // NullPointerException here
        ...
    }

}

public class Util {
    public static List<FileItem> parseUpload(FileUpload upload, byte[] bytes) throws FileUploadException { // called from test
        return parseUpload(upload, bytes, Constants.CONTENT_TYPE); // calls a and b
    }

    public static List<FileItem> parseUpload(FileUpload upload, byte[] bytes, String contentType) throws FileUploadException { // calls a and b
        final HttpServletRequest request = new MockHttpServletRequest(bytes, contentType);
        List<FileItem> fileItems = upload.parseRequest(new ServletRequestContext(request)); // call to a
        return fileItems;
    }
}
