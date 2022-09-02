public interface FileItemStream {
    String getFieldName(); // b
}

public class FileItemStreamImpl {
    @Override
    public void setHeaders(FileItemHeaders pHeaders) { // definition of a
        headers = pHeaders;
    }

    @Override
    public String getFieldName() { // only implementation of b
        return fieldName;
    }
}

public class FileUploadBase {
    public List<FileItem> parseRequest(RequestContext ctx)
            throws FileUploadException {
        List<FileItem> items = new ArrayList<FileItem>();
        boolean successful = false;
        try {
            FileItemIterator iter = getItemIterator(ctx); // calls a
            FileItemFactory fac = getFileItemFactory();
            ...
            while (iter.hasNext()) {
                final FileItemStream item = iter.next();
                // Don't use getName() here to prevent an InvalidFileNameException.
                final String fileName = ((FileItemIteratorImpl.FileItemStreamImpl) item).name;
                FileItem fileItem = fac.createItem(item.getFieldName(), item.getContentType(), // call to b
                                                   item.isFormField(), fileName);
                items.add(fileItem);
                ...
                // NOTE: would be interesting to see if state restoration occured by this point
                final FileItemHeaders fih = item.getHeaders();
                fileItem.setHeaders(fih);
            }
            successful = true;
            return items;
        }
        ...
    }

    public FileItemIterator getItemIterator(RequestContext ctx)
    throws FileUploadException, IOException {
        try {
            return new FileItemIteratorImpl(ctx); // calls b
        } catch (FileUploadIOException e) {
            // unwrap encapsulated SizeException
            throw (FileUploadException) e.getCause();
        }
    }
}

public class FileItemIteratorImpl {

    FileItemIteratorImpl(RequestContext ctx)
        throws FileUploadException, IOException {
        ...
        findNextItem(); // calls b
    }

    private boolean findNextItem() throws IOException {
        ...
        FileItemHeaders headers = getParsedHeaders(multi.readHeaders());
        if (currentFieldName == null) {
            // We're parsing the outer multipart
            String fieldName = getFieldName(headers);
            if (fieldName != null) {
                ...
                currentItem = new FileItemStreamImpl(fileName,
                                                     fieldName, headers.getHeader(CONTENT_TYPE),
                                                     fileName == null, getContentLength(headers));
                currentItem.setHeaders(headers); // call to a
                notifier.noteItem();
                itemValid = true;
                return true;
            }
        }
        ...
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
        List<FileItem> fileItems = upload.parseRequest(req); // calls a and b
        assertEquals(1, fileItems.size());
        FileItem item = fileItems.get(0);
        assertEquals("This is the content of the file\n", new String(item.get()));

        upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setFileSizeMax(40);
        req = new MockHttpServletRequest(request.getBytes("US-ASCII"), Constants.CONTENT_TYPE);
        fileItems = upload.parseRequest(req); // calls a and b
        assertEquals(1, fileItems.size());
        item = fileItems.get(0);
        assertEquals("This is the content of the file\n", new String(item.get()));

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
