public class DiskFileItem implements FileItem {

    /**
     * Sets the default charset for use when no explicit charset
     * parameter is provided by the sender.
     * @param charset the default charset
     */
    public void setDefaultCharset(String charset) { // definition of a
        defaultCharset = charset;
    }

    /**
     * Provides a hint as to whether or not the file contents will be read
     * from memory.
     *
     * @return <code>true</code> if the file contents will be read
     *         from memory; <code>false</code> otherwise.
     */
    @Override
    public boolean isInMemory() { // definition of b
        if (cachedContent != null) {
            return true;
        }
        return dfos.isInMemory();
    }

    @Override
    public byte[] get() {
        if (isInMemory()) { // call to b
            if (cachedContent == null && dfos != null) {
                cachedContent = dfos.getData();
            }
            return cachedContent;
        }

        ...

        return fileData;
    }

}

public class DiskFileItemFactory {

    @Override
    public FileItem createItem(String fieldName, String contentType,
            boolean isFormField, String fileName) { // called from FileUploadBase.parseRequest()
        DiskFileItem result = new DiskFileItem(fieldName, contentType,
                isFormField, fileName, sizeThreshold, repository);
        result.setDefaultCharset(defaultCharset); // call to a
        FileCleaningTracker tracker = getFileCleaningTracker();
        if (tracker != null) {
            tracker.track(result.getTempFile(), result);
        }
        return result;
    }

}

public class FileUploadBase {

    public List<FileItem> parseRequest(RequestContext ctx)
        throws FileUploadException { // called from StreamingTest
        List<FileItem> items = new ArrayList<FileItem>();
        boolean successful = false;
        try {
            FileItemIterator iter = getItemIterator(ctx);
            FileItemFactory fac = getFileItemFactory();
            ...
            while (iter.hasNext()) {
                final FileItemStream item = iter.next();
                // Don't use getName() here to prevent an InvalidFileNameException.
                final String fileName = ((FileItemIteratorImpl.FileItemStreamImpl) item).name;
                FileItem fileItem = fac.createItem(item.getFieldName(), item.getContentType(),
                                                   item.isFormField(), fileName); // calls a
                ...
            }
        }
    }

}

public class StreamingTest {
    public void testFileUpload()
        throws IOException, FileUploadException {
        byte[] request = newRequest();
        List<FileItem> fileItems = parseUpload(request); // calls a
        Iterator<FileItem> fileIter = fileItems.iterator();
        ...
        for (int i = 0;  i < 16384;  i += add) {
            ...
            FileItem item = fileIter.next();
            ...
            byte[] bytes = item.get(); // calls b?
            ...
        }
        assertTrue(!fileIter.hasNext());
    }

    private List<FileItem> parseUpload(InputStream pStream, int pLength)
            throws FileUploadException {
        ...
        FileUploadBase upload = new ServletFileUpload();
        upload.setFileItemFactory(new DiskFileItemFactory());
        HttpServletRequest request = new MockHttpServletRequest(pStream,
                pLength, contentType);

        List<FileItem> fileItems = upload.parseRequest(new ServletRequestContext(request)); // calls a
        return fileItems;
    }
}
