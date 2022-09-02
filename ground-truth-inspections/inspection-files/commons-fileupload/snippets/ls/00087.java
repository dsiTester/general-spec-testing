public interface FileItemHeadersSupport {
    void setHeaders(FileItemHeaders headers); // a
}

public interface FileItem extends FileItemHeadersSupport {
    byte[] get(); // b
}

public class DiskFileItem implements FileItem {

    @Override
    public void setHeaders(FileItemHeaders pHeaders) { // implementation of a that is invoked
        headers = pHeaders;
    }

    @Override
    public byte[] get() { // only implementation of b
        if (isInMemory()) {
            if (cachedContent == null && dfos != null) {
                cachedContent = dfos.getData();
            }
            return cachedContent;
        }

        byte[] fileData = new byte[(int) getSize()];
        InputStream fis = null;

        try {
            fis = new FileInputStream(dfos.getFile());
            IOUtils.readFully(fis, fileData);
        } catch (IOException e) {
            fileData = null;
        } finally {
            IOUtils.closeQuietly(fis);
        }

        return fileData;
    }
}

public class SizesTest {

    @Test
    public void testFileSizeLimit()
            throws IOException, FileUploadException {
        final String request =
            "-----1234\r\n" +
            "Content-Disposition: form-data; name=\"file\"; filename=\"foo.tab\"\r\n" +
            "Content-Type: text/whatever\r\n" +
            "\r\n" +
            "This is the content of the file\n" +
            "\r\n" +
            "-----1234--\r\n";

        ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setFileSizeMax(-1);
        HttpServletRequest req = new MockHttpServletRequest(
                request.getBytes("US-ASCII"), Constants.CONTENT_TYPE);
        List<FileItem> fileItems = upload.parseRequest(req); // calls a
        ...
        FileItem item = fileItems.get(0);
        assertEquals("This is the content of the file\n", new String(item.get())); // call to b
        upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setFileSizeMax(40);
        req = new MockHttpServletRequest(request.getBytes("US-ASCII"), Constants.CONTENT_TYPE);
        fileItems = upload.parseRequest(req); // calls a
        ...
        item = fileItems.get(0);
        assertEquals("This is the content of the file\n", new String(item.get())); // call to b
        ...
    }

}

public class ServletFileUpload {
    @Override
    public List<FileItem> parseRequest(HttpServletRequest request)
    throws FileUploadException {
        return parseRequest(new ServletRequestContext(request)); // calls a
    }
}

public class FileUploadBase {

    public List<FileItem> parseRequest(RequestContext ctx)
            throws FileUploadException {
        List<FileItem> items = new ArrayList<FileItem>();
        ...
        try {
            FileItemIterator iter = getItemIterator(ctx);
            FileItemFactory fac = getFileItemFactory();
            ...
            while (iter.hasNext()) {
                final FileItemStream item = iter.next();
                // Don't use getName() here to prevent an InvalidFileNameException.
                final String fileName = ((FileItemIteratorImpl.FileItemStreamImpl) item).name;
                FileItem fileItem = fac.createItem(item.getFieldName(), item.getContentType(),
                                                   item.isFormField(), fileName);
                ...
                final FileItemHeaders fih = item.getHeaders();
                fileItem.setHeaders(fih); // call to a
            }
            ...
            return items;
        }
        ...
    }
}
