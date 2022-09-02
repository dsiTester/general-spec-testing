public interface FileItemStream {
    String getFieldName();
}

public class FileItemStreamImpl implements FileItemStream {

    @Override
    public String getFieldName() { // only implementation of a
        return fieldName;
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
                FileItem fileItem = fac.createItem(item.getFieldName(), item.getContentType(), // call to a
                                                   item.isFormField(), fileName);
                ...
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

public class StreamingTest {

    public void testFileUpload()
            throws IOException, FileUploadException {
        byte[] request = newRequest();
        List<FileItem> fileItems = parseUpload(request); // calls a and b
        Iterator<FileItem> fileIter = fileItems.iterator();
        ...
        for (int i = 0;  i < 16384;  i += add) {
            ...
            FileItem item = fileIter.next();
            assertEquals("field" + (num++), item.getFieldName()); // fails here
            ...
        }
        ...
    }

    private List<FileItem> parseUpload(byte[] bytes) throws FileUploadException {
        return parseUpload(new ByteArrayInputStream(bytes), bytes.length); // calls a and b
    }

    private List<FileItem> parseUpload(InputStream pStream, int pLength)
            throws FileUploadException {
        String contentType = "multipart/form-data; boundary=---1234";

        FileUploadBase upload = new ServletFileUpload();
        upload.setFileItemFactory(new DiskFileItemFactory());
        HttpServletRequest request = new MockHttpServletRequest(pStream,
                pLength, contentType);

        List<FileItem> fileItems = upload.parseRequest(new ServletRequestContext(request)); // calls a and b
        return fileItems;
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

public class SizesTest {
    @Test
    public void testMaxSizeLimitUnknownContentLength()
            throws IOException, FileUploadException { // unknown case
        final String request = // replacement value for method-a
            "-----1234\r\n" +
            "Content-Disposition: form-data; name=\"file1\"; filename=\"foo1.tab\"\r\n" +
            "Content-Type: text/whatever\r\n" +
            "Content-Length: 10\r\n" +
            "\r\n" +
            "This is the content of the file\n" +
            "\r\n" +
            "-----1234\r\n" +
            "Content-Disposition: form-data; name=\"file2\"; filename=\"foo2.tab\"\r\n" +
            "Content-Type: text/whatever\r\n" +
            "\r\n" +
            "This is the content of the file\n" +
            "\r\n" +
            "-----1234--\r\n";

        ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
        ...
        MockHttpServletRequest req = new MockHttpServletRequest(
                request.getBytes("US-ASCII"), Constants.CONTENT_TYPE);
        ...

        FileItemIterator it = upload.getItemIterator(req);
        ...
        FileItemStream item = it.next();
        assertFalse(item.isFormField());
        assertEquals("file1", item.getFieldName()); // call to a - this assertion fails
        assertEquals("foo1.tab", item.getName());

        {
            InputStream stream = item.openStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Streams.copy(stream, baos, true);
        }

        // the second item is over the size max, thus we expect an error
        try {
            // the header is still within size max -> this shall still succeed
            assertTrue(it.hasNext()); // calls b
        }
        ...
    }

    // modified version to prove that the spec is spurious.
    @Test
    public void modifiedTestMaxSizeLimitUnknownContentLength()
            throws IOException, FileUploadException { // unknown case
        final String request = // replacement value for method-a
            "-----1234\r\n" +
            "Content-Disposition: form-data; name=\"file1\"; filename=\"foo1.tab\"\r\n" +
            "Content-Type: text/whatever\r\n" +
            "Content-Length: 10\r\n" +
            "\r\n" +
            "This is the content of the file\n" +
            "\r\n" +
            "-----1234\r\n" +
            "Content-Disposition: form-data; name=\"file2\"; filename=\"foo2.tab\"\r\n" +
            "Content-Type: text/whatever\r\n" +
            "\r\n" +
            "This is the content of the file\n" +
            "\r\n" +
            "-----1234--\r\n";

        ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
        ...
        MockHttpServletRequest req = new MockHttpServletRequest(
                request.getBytes("US-ASCII"), Constants.CONTENT_TYPE);
        ...

        FileItemIterator it = upload.getItemIterator(req);
        ...
        FileItemStream item = it.next();
        assertFalse(item.isFormField());
        // NOTE: comment the next line out to delay call from a
        // assertEquals("file1", item.getFieldName()); // call to a - this assertion fails
        assertEquals("foo1.tab", item.getName());

        {
            InputStream stream = item.openStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Streams.copy(stream, baos, true);
        }

        // the second item is over the size max, thus we expect an error
        try {
            // the header is still within size max -> this shall still succeed
            assertTrue(it.hasNext()); // calls b
            // NOTE: delayed call of a
            assertEquals("file1", item.getFieldName());
        }
        ...
    }
}
