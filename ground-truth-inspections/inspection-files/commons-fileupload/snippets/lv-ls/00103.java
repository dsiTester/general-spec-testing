public interface FileItemStream {
    String getFieldName(); // a
    InputStream openStream() throws IOException; // b
}

private class FileItemStreamImpl implements FileItemStream {
    @Override
    public String getFieldName() { // only implementation of a
        return fieldName;
    }

    @Override
    public InputStream openStream() throws IOException { // only implementation of b
        if (opened) {
            throw new IllegalStateException(
                                            "The stream was already opened.");
        }
        if (((Closeable) stream).isClosed()) {
            throw new FileItemStream.ItemSkippedException();
        }
        return stream;
    }
}

public class StreamingTest {

    public void testFileUpload()
            throws IOException, FileUploadException { // validating test
        byte[] request = newRequest();
        List<FileItem> fileItems = parseUpload(request);
        Iterator<FileItem> fileIter = fileItems.iterator();
        int add = 16;
        int num = 0;
        for (int i = 0;  i < 16384;  i += add) {
            if (++add == 32) {
                add = 16;
            }
            FileItem item = fileIter.next();
            assertEquals("field" + (num++), item.getFieldName());
            byte[] bytes = item.get();
            assertEquals(i, bytes.length);
            for (int j = 0;  j < i;  j++) {
                assertEquals((byte) j, bytes[j]);
            }
        }
        assertTrue(!fileIter.hasNext());
    }

}

public class SizesTest {

    @Test
    public void testFileSizeLimitWithFakedContentLength()
            throws IOException, FileUploadException { // invalidated case
        final String request =
            "-----1234\r\n" +
            "Content-Disposition: form-data; name=\"file\"; filename=\"foo.tab\"\r\n" +
            "Content-Type: text/whatever\r\n" +
            "Content-Length: 10\r\n" +
            "\r\n" +
            "This is the content of the file\n" +
            "\r\n" +
            "-----1234--\r\n";

        ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
        ...
        HttpServletRequest req = new MockHttpServletRequest(
                request.getBytes("US-ASCII"), Constants.CONTENT_TYPE);
        List<FileItem> fileItems = upload.parseRequest(req); // calls a and b
        // NOTE that there are no checks for fieldName
        assertEquals(1, fileItems.size());
        FileItem item = fileItems.get(0);
        assertEquals("This is the content of the file\n", new String(item.get()));

        upload = new ServletFileUpload(new DiskFileItemFactory());
        ...
        req = new MockHttpServletRequest(request.getBytes("US-ASCII"), Constants.CONTENT_TYPE);
        fileItems = upload.parseRequest(req); // calls a and b
        ...

        // provided Content-Length is larger than the FileSizeMax -> handled by ctor
        upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setFileSizeMax(5);
        req = new MockHttpServletRequest(request.getBytes("US-ASCII"), Constants.CONTENT_TYPE);
        try {
            upload.parseRequest(req); // calls a and b
            fail("Expected exception.");
        } catch (FileUploadBase.FileSizeLimitExceededException e) {
            assertEquals(5, e.getPermittedSize());
        }

        // provided Content-Length is wrong, actual content is larger -> handled by LimitedInputStream
        upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setFileSizeMax(15);
        req = new MockHttpServletRequest(request.getBytes("US-ASCII"), Constants.CONTENT_TYPE);
        try {
            upload.parseRequest(req); // calls a and b
            fail("Expected exception.");
        } catch (FileUploadBase.FileSizeLimitExceededException e) {
            assertEquals(15, e.getPermittedSize());
        }
    }

}

public class ServletFileUpload {
    @Override
    public List<FileItem> parseRequest(HttpServletRequest request)
    throws FileUploadException {
        return parseRequest(new ServletRequestContext(request)); // calls a and b
    }

}

public class FileUploadBase {

    public List<FileItem> parseRequest(RequestContext ctx)
            throws FileUploadException { // calls a and b
        List<FileItem> items = new ArrayList<FileItem>();
        ...
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
                FileItem fileItem = fac.createItem(item.getFieldName(), item.getContentType(), // call to a
                                                   item.isFormField(), fileName);
                items.add(fileItem);
                try {
                    Streams.copy(item.openStream(), fileItem.getOutputStream(), true, buffer); // call to b
                }
                ...
            }
            ...
        }
        ...
    }

    // NOTE: the following method doesn't actually exist - it's just a way of showing that the spec is actually spurious.
    // NOTE: run the following with StreamingTest#testFileUpload and there will no longer be a test failure
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
                // NOTE: below 2 lines is different from the original method
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
                } finally {
                    // NOTE: below is calling a after b
                    System.out.println("GETFIELDNAME :  " + item.getFieldName());
                }

                final FileItemHeaders fih = item.getHeaders();
                fileItem.setHeaders(fih);
            }
            ...
            return items;
        }
        ...
    }


}
