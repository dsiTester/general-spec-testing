public class FileUploadBase {
    public List<FileItem> parseRequest(RequestContext ctx) // definition of a
        throws FileUploadException {
        List<FileItem> items = new ArrayList<FileItem>();
        boolean successful = false;
        try {
            FileItemIterator iter = getItemIterator(ctx); // calls b
            ...
        }
        ...
     }

    public FileItemIterator getItemIterator(RequestContext ctx)
    throws FileUploadException, IOException { // calls b
        try {
            return new FileItemIteratorImpl(ctx); // calls b
        } catch (FileUploadIOException e) {
            // unwrap encapsulated SizeException
            throw (FileUploadException) e.getCause();
        }
    }

    private class FileItemIteratorImpl implements FileItemIterator {
        FileItemIteratorImpl(RequestContext ctx)
            throws FileUploadException, IOException { // called from a
            ...
            boundary = getBoundary(contentType); // call to b
            if (boundary == null) {
                IOUtils.closeQuietly(input); // avoid possible resource leak
                throw new FileUploadException("the request was rejected because no multipart boundary was found");
            }
            ...
        }
    }

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
}

public class SizesTest {
    @Test
    public void testFileUpload()
            throws IOException, FileUploadException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ...
        List<FileItem> fileItems =
                Util.parseUpload(new ServletFileUpload(new DiskFileItemFactory()), baos.toByteArray()); // calls a
        Iterator<FileItem> fileIter = fileItems.iterator(); // NullPointerException here
        ...
    }

}

public class Util {
    public static List<FileItem> parseUpload(FileUpload upload, byte[] bytes) throws FileUploadException { // called from test
        return parseUpload(upload, bytes, Constants.CONTENT_TYPE); // calls a
    }

    public static List<FileItem> parseUpload(FileUpload upload, byte[] bytes, String contentType) throws FileUploadException { // calls a
        final HttpServletRequest request = new MockHttpServletRequest(bytes, contentType);
        List<FileItem> fileItems = upload.parseRequest(new ServletRequestContext(request)); // call to a
        return fileItems;
    }
}
