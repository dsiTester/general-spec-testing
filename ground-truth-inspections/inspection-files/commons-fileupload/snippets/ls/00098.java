public interface FileItemStream {
    boolean isFormField(); // a
}

class FileItemStreamImpl implements FileItemStream {

    @Override
    public String getContentType() { // only implementation of a
        return contentType;
    }

}

public interface FileItemIterator {
    boolean hasNext() throws FileUploadException, IOException; // calls b
}

public class FileItemIteratorImpl implements FileItemIterator {
    void close() throws IOException { // definition of b
        stream.close();
    }

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

    @Deprecated
    public List<FileItem> parseRequest(HttpServletRequest req)
    throws FileUploadException {
        return parseRequest(new ServletRequestContext(req)); // calls a and b
    }

    public List<FileItem> parseRequest(RequestContext ctx)
            throws FileUploadException { // calls a and b
        List<FileItem> items = new ArrayList<FileItem>();
        boolean successful = false;
        try {
            FileItemIterator iter = getItemIterator(ctx);
            FileItemFactory fac = getFileItemFactory();
            ...
            while (iter.hasNext()) { // calls b ON CERTAIN CONDITIONS
                ...
                FileItem fileItem = fac.createItem(item.getFieldName(), item.getContentType(), // call to a
                                                   item.isFormField(), fileName);
                ...
            }
            ...
            return items;
        }
        ...
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
                "-----1234--\r\n"; // most likely replacement for a's return value
        ...
        final List<FileItem> items = myUpload.parseRequest(request); // calls a and b
        assertNotNull(items);
        ...
    }
}
