public class FileItemStreamImpl implements FileItemStream {

    @Override
    public FileItemHeaders getHeaders() { // definition of a
        return headers;
    }

    void close() throws IOException { // definition of b
        stream.close();
    }

}

public class FileItemIteratorImpl {

    @Override
    public boolean hasNext() throws FileUploadException, IOException { // calls b
        if (eof) {
            return false;
        }
        if (itemValid) {
            return true;
        }
        try {
            return findNextItem(); // calls b
        } catch (FileUploadIOException e) {
            // unwrap encapsulated SizeException
            throw (FileUploadException) e.getCause();
        }
    }

    private boolean findNextItem() throws IOException { // calls b
        if (eof) {
            return false;
        }
        if (currentItem != null) {
            currentItem.close(); // call to b
            currentItem = null;
        }
        ...
    }

}

public class FileUploadBase {

    public List<FileItem> parseRequest(RequestContext ctx)
            throws FileUploadException {
        List<FileItem> items = new ArrayList<FileItem>();
        boolean successful = false;
        try {
            FileItemIterator iter = getItemIterator(ctx);
            FileItemFactory fac = getFileItemFactory();
            ...
            while (iter.hasNext()) { // calls b on certain conditions
                final FileItemStream item = iter.next();
                ...
                final FileItemHeaders fih = item.getHeaders(); // call to a
                fileItem.setHeaders(fih);
            }
            ...
        }
        ...
    }

    @Deprecated
    public List<FileItem> parseRequest(HttpServletRequest req)
    throws FileUploadException { // called from DiskFileUploadTest#testMoveFile
        return parseRequest(new ServletRequestContext(req)); // calls a and b (above)
    }

}

public class DiskFileUploadTest {
    @Test
    public void testMoveFile() throws Exception {
        DiskFileUpload myUpload = new DiskFileUpload();
        ...
        final HttpServletRequest request = new MockHttpServletRequest(contentBytes, Constants.CONTENT_TYPE);
        final List<FileItem> items = myUpload.parseRequest(request); // calls a and b
        assertNotNull(items);
        assertFalse(items.isEmpty());
        final DiskFileItem dfi = (DiskFileItem) items.get(0);
        final File out = File.createTempFile("install", ".tmp");
        dfi.write(out);
    }
}
