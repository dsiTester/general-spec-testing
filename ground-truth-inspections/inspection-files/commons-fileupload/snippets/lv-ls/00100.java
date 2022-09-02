public interface FileItemStream {
    String getFieldName(); // a
}

public class FileItemStreamImpl implements FileItemStream {

    @Override
    public String getFieldName() { // only implementation of a
        return fieldName;
    }

    @Override
    public FileItemHeaders getHeaders() { // definition of b
        return headers;
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
            final byte[] buffer = new byte[Streams.DEFAULT_BUFFER_SIZE];
            if (fac == null) {
                throw new NullPointerException("No FileItemFactory has been set.");
            }
            while (iter.hasNext()) {
                final FileItemStream item = iter.next();
                ...
                final String fileName = ((FileItemIteratorImpl.FileItemStreamImpl) item).name;
                FileItem fileItem = fac.createItem(item.getFieldName(), item.getContentType(), // call to a
                                                   item.isFormField(), fileName);
                items.add(fileItem);
                ...
                final FileItemHeaders fih = item.getHeaders(); // call to b
                fileItem.setHeaders(fih);
            }
            ...
            return items;
        }
        ...
    }

    // NOTE: the following method doesn't actually exist - it's just a way of showing that the spec is actually spurious.
    // NOTE: run the following with SizesTest#testFileUpload and there will no longer be a test failure
    public List<FileItem> modifiedParseRequest(RequestContext ctx)
            throws FileUploadException {
        List<FileItem> items = new ArrayList<FileItem>();
        boolean successful = false;
        try {
            FileItemIterator iter = getItemIterator(ctx);
            FileItemFactory fac = getFileItemFactory();
            final byte[] buffer = new byte[Streams.DEFAULT_BUFFER_SIZE];
            if (fac == null) {
                throw new NullPointerException("No FileItemFactory has been set.");
            }
            while (iter.hasNext()) {
                final FileItemStream item = iter.next();
                // Don't use getName() here to prevent an InvalidFileNameException.
                final String fileName = ((FileItemIteratorImpl.FileItemStreamImpl) item).name;
                // NOTE: below is different from the original method
                String stubField = "field" + acc;
                acc++;
                FileItem fileItem = fac.createItem(stubField, item.getContentType(),
                                                   item.isFormField(), fileName);
                items.add(fileItem);
                try {
                    Streams.copy(item.openStream(), fileItem.getOutputStream(), true, buffer);
                } catch (FileUploadIOException e) {
                    throw (FileUploadException) e.getCause();
                } catch (IOException e) {
                    throw new IOFileUploadException(format("Processing of %s request failed. %s",
                                                           MULTIPART_FORM_DATA, e.getMessage()), e);
                }
                final FileItemHeaders fih = item.getHeaders();
                // NOTE: below is calling a after b
                System.out.println("GETFIELDNAME :  " + item.getFieldName());
                fileItem.setHeaders(fih);
            }
            ...
            return items;
        }
        ...
    }


    @Deprecated
    public List<FileItem> parseRequest(HttpServletRequest req)
    throws FileUploadException { // called by DiskFileUploadTest
        return parseRequest(new ServletRequestContext(req)); // calls a and b (above)
    }
}

public class SizesTest {

    @Test
    public void testFileUpload()
            throws IOException, FileUploadException { // validated case
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ...

        List<FileItem> fileItems =
                Util.parseUpload(new ServletFileUpload(new DiskFileItemFactory()), baos.toByteArray()); // calls a and b
        Iterator<FileItem> fileIter = fileItems.iterator();
        ...
        for (int i = 0;  i < 16384;  i += add) {
            ...
            FileItem item = fileIter.next();
            assertEquals("field" + (num++), item.getFieldName()); // assertion fails here
            ...
        }
        ...
    }
}

public class Util {

    public static List<FileItem> parseUpload(FileUpload upload, byte[] bytes) throws FileUploadException { // called by SizesTest - calls a
        return parseUpload(upload, bytes, Constants.CONTENT_TYPE);
    }

    public static List<FileItem> parseUpload(FileUpload upload, byte[] bytes, String contentType) throws FileUploadException { // calls a
        final HttpServletRequest request = new MockHttpServletRequest(bytes, contentType);
        List<FileItem> fileItems = upload.parseRequest(new ServletRequestContext(request));
        return fileItems;
    }

}

public class DiskFileUploadTest {

    @Test
    public void testMoveFile() throws Exception {
        DiskFileUpload myUpload = new DiskFileUpload();
        myUpload.setSizeThreshold(0);
    	final String content =
                "-----1234\r\n" +
                "Content-Disposition: form-data; name=\"file\";"
                		+ "filename=\"foo.tab\"\r\n" +
                "Content-Type: text/whatever\r\n" +
                "\r\n" +
                "This is the content of the file\n" +
                "\r\n" +
                "-----1234--\r\n";
    	final byte[] contentBytes = content.getBytes("US-ASCII");
        final HttpServletRequest request = new MockHttpServletRequest(contentBytes, Constants.CONTENT_TYPE);
        final List<FileItem> items = myUpload.parseRequest(request); // calls a and b
        assertNotNull(items);
        assertFalse(items.isEmpty());
        final DiskFileItem dfi = (DiskFileItem) items.get(0);
        final File out = File.createTempFile("install", ".tmp");
        dfi.write(out);
    }

}
