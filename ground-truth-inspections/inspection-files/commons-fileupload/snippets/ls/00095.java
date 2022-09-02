public interface FileItemStream {
    String getContentType(); // a
}

class FileItemStreamImpl implements FileItemStream {

    @Override
    public String getContentType() { // only implementation of a
        return contentType;
    }

    @Override
    public FileItemHeaders getHeaders() { // only implementation of b
        return headers;
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
            while (iter.hasNext()) {
                ...
                FileItem fileItem = fac.createItem(item.getFieldName(), item.getContentType(), // call to a
                                                   item.isFormField(), fileName); // call to b
                items.add(fileItem);
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
