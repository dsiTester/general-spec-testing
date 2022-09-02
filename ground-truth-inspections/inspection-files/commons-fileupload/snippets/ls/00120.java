public class FileItemStreamImpl {
    @Override
    public void setHeaders(FileItemHeaders pHeaders) { // definition of a
        headers = pHeaders;
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
            FileItemIterator iter = getItemIterator(ctx); // calls a
            FileItemFactory fac = getFileItemFactory();
            ...
            while (iter.hasNext()) {
                final FileItemStream item = iter.next();
                // Don't use getName() here to prevent an InvalidFileNameException.
                final String fileName = ((FileItemIteratorImpl.FileItemStreamImpl) item).name;
                FileItem fileItem = fac.createItem(item.getFieldName(), item.getContentType(),
                                                   item.isFormField(), fileName);
                items.add(fileItem);
                ...
                final FileItemHeaders fih = item.getHeaders(); // call to b
                // NOTE: to check the effects of delaying a, uncomment the below
                // System.out.println(fih);
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
                // NOTE: to check the effects of delaying a, comment out the below - the print statement immediately after method-b should show that the output is null.
                currentItem.setHeaders(headers); // call to a
                notifier.noteItem();
                itemValid = true;
                return true;
            }
        }
        ...
    }
}

public class DiskFileUploadTest {
    @Test
    public void testMoveFile() throws Exception {
        ...
        final List<FileItem> items = myUpload.parseRequest(request); // calls a and b
        // NOTE: none of the below assertions check for headers
        assertNotNull(items);
        assertFalse(items.isEmpty());
        final DiskFileItem dfi = (DiskFileItem) items.get(0);
        final File out = File.createTempFile("install", ".tmp");
        dfi.write(out);
    }
}
