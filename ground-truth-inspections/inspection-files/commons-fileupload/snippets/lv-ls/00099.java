public interface FileItemStream {
    String getFieldName(); // a
    String getContentType(); // b
}

public class FileItemStreamImpl implements FileItemStream {

    @Override
    public String getFieldName() { // only implementation of a
        return fieldName;
    }

    @Override
    public String getContentType() { // only implementation of b
        return contentType;
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
                FileItem fileItem = fac.createItem(item.getFieldName(), item.getContentType(), // call to a and b
                                                   item.isFormField(), fileName);
                items.add(fileItem);
                ...
            }
            ...
            return items;
        }
        ...
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

    @Test
    public void testFileSizeLimit()
            throws IOException, FileUploadException { // invalidated case
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
        List<FileItem> fileItems = upload.parseRequest(req); // calls a and b
        assertEquals(1, fileItems.size());
        FileItem item = fileItems.get(0);
        // TODO: add below to check that the spec is actually spurious
        // System.out.println(item.getContentType());
        // System.out.println(item.getFieldName());
        ...

        upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setFileSizeMax(40);
        req = new MockHttpServletRequest(request.getBytes("US-ASCII"), Constants.CONTENT_TYPE);
        fileItems = upload.parseRequest(req); // calls a and b
        ...

        upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setFileSizeMax(30);
        req = new MockHttpServletRequest(request.getBytes("US-ASCII"), Constants.CONTENT_TYPE);
        try {
            upload.parseRequest(req); // calls a and b
            fail("Expected exception.");
        } catch (FileUploadBase.FileSizeLimitExceededException e) {
            assertEquals(30, e.getPermittedSize());
        }
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
