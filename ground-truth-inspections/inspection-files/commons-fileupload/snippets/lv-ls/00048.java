public interface FileItem {
    OutputStream getOutputStream() throws IOException; // a
    String getFieldName(); // b
}

public class DiskFileItem implements FileItem {
    @Override
    public OutputStream getOutputStream()
        throws IOException { // only implementation of a
        if (dfos == null) {
            File outputFile = getTempFile();
            dfos = new DeferredFileOutputStream(sizeThreshold, outputFile);
        }
        return dfos;
    }

    @Override
    public String getFieldName() { // only implementation of b
        return fieldName;
    }

}

public class StreamingTest {

    public void testFILEUPLOAD135()
            throws IOException, FileUploadException { // validated test
        byte[] request = newShortRequest();
        final ByteArrayInputStream bais = new ByteArrayInputStream(request);
        List<FileItem> fileItems = parseUpload(new InputStream() {
            ...
        }, request.length); // calls a
        Iterator<FileItem> fileIter = fileItems.iterator();
        ...
        FileItem item = fileIter.next();
        assertEquals("field", item.getFieldName()); // call to b
        ...
    }

    private List<FileItem> parseUpload(byte[] bytes) throws FileUploadException {
        return parseUpload(new ByteArrayInputStream(bytes), bytes.length); // calls a
    }

    private List<FileItem> parseUpload(InputStream pStream, int pLength)
            throws FileUploadException {
        ...
        List<FileItem> fileItems = upload.parseRequest(new ServletRequestContext(request)); // calls a
        return fileItems;
    }
}

public class SizesTest {
    @Test
    public void testFileUpload()
            throws IOException, FileUploadException { // invalidated case
        ByteArrayOutputStream baos = new ByteArrayOutputStream(); // most likely replacement value
        ...
        List<FileItem> fileItems =
                Util.parseUpload(new ServletFileUpload(new DiskFileItemFactory()), baos.toByteArray()); // calls a
        Iterator<FileItem> fileIter = fileItems.iterator();
        ...
        for (int i = 0;  i < 16384;  i += add) {
            ...
            FileItem item = fileIter.next();
            assertEquals("field" + (num++), item.getFieldName()); // call to b
            ...
        }
        assertTrue(!fileIter.hasNext());
    }
}

public class Util {

    public static List<FileItem> parseUpload(FileUpload upload, byte[] bytes) throws FileUploadException { // called from SizesTest.testFileUpload()
        return parseUpload(upload, bytes, Constants.CONTENT_TYPE); // calls a
    }

    public static List<FileItem> parseUpload(FileUpload upload, byte[] bytes, String contentType) throws FileUploadException { // called from above
        final HttpServletRequest request = new MockHttpServletRequest(bytes, contentType);
        List<FileItem> fileItems = upload.parseRequest(new ServletRequestContext(request)); // calls a
        return fileItems;
    }

}

public class FileUploadBase {
    public List<FileItem> parseRequest(RequestContext ctx)
            throws FileUploadException { // calls a
        List<FileItem> items = new ArrayList<FileItem>();
        ...
        try {
            FileItemIterator iter = getItemIterator(ctx);
            FileItemFactory fac = getFileItemFactory();
            ...
            FileItem fileItem = fac.createItem(item.getFieldName(), item.getContentType(),
                                               item.isFormField(), fileName);
            ...
            while (iter.hasNext()) {
                ...
                try {
                    Streams.copy(item.openStream(), fileItem.getOutputStream(), true, buffer); // call to a
                }
                ...
            }
            ...
        }
        ...
    }
}
