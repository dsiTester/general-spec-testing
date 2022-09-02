public interface FileItemHeadersSupport {
    /**
     * Sets the headers read from within an item.  Implementations of
     * {@link FileItem} or {@link FileItemStream} should implement this
     * interface to be able to get the raw headers found within the item
     * header block.
     *
     * @param headers the instance that holds onto the headers
     *         for this instance.
     */
    void setHeaders(FileItemHeaders headers); // b
}

public interface FileItem extends FileItemHeadersSupport {
    ...
}

public class DiskFileItem implements FileItem {

    /**
     * Creates and returns a {@link java.io.File File} representing a uniquely
     * named temporary file in the configured repository path. The lifetime of
     * the file is tied to the lifetime of the <code>FileItem</code> instance;
     * the file will be deleted when the instance is garbage collected.
     * <p>
     * <b>Note: Subclasses that override this method must ensure that they return the
     * same File each time.</b>
     *
     * @return The {@link java.io.File File} to be used for temporary storage.
     */
    protected File getTempFile() { // definition of a
        if (tempFile == null) {
            File tempDir = repository;
            if (tempDir == null) {
                tempDir = new File(System.getProperty("java.io.tmpdir"));
            }

            String tempFileName = format("upload_%s_%s.tmp", UID, getUniqueId());

            tempFile = new File(tempDir, tempFileName);
        }
        return tempFile;
    }

    /**
     * Sets the file item headers.
     * @param pHeaders The file items headers.
     */
    @Override
    public void setHeaders(FileItemHeaders pHeaders) { // only implementation of b
        headers = pHeaders;
    }

    @Override
    public OutputStream getOutputStream()
        throws IOException {
        if (dfos == null) {
            File outputFile = getTempFile(); // call to a
            dfos = new DeferredFileOutputStream(sizeThreshold, outputFile);
        }
        return dfos;
    }

}

public class FileUploadBase {

    @Deprecated
    public List<FileItem> parseRequest(HttpServletRequest req)
    throws FileUploadException { // called from DiskFileUploadTest
        return parseRequest(new ServletRequestContext(req)); // calls a and b
    }

    /**
     * Processes an <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>
     * compliant <code>multipart/form-data</code> stream.
     *
     * @param ctx The context for the request to be parsed.
     *
     * @return A list of <code>FileItem</code> instances parsed from the
     *         request, in the order that they were transmitted.
     *
     * @throws FileUploadException if there are problems reading/parsing
     *                             the request or storing files.
     */
    public List<FileItem> parseRequest(RequestContext ctx)
        throws FileUploadException {
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
                                                   item.isFormField(), fileName);
                items.add(fileItem);
                try {
                    Streams.copy(item.openStream(), fileItem.getOutputStream(), true, buffer); // calls a
                } ...
                final FileItemHeaders fih = item.getHeaders();
                fileItem.setHeaders(fih); // call to b
            }
            successful = true;
            return items;
        }
        ...
    }

}

public class SizesTest {

    /**
     * Runs a test with varying file sizes.
     */
    @Test
    public void testFileUpload()
        throws IOException, FileUploadException { // invalidated test
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ...
        List<FileItem> fileItems =
            Util.parseUpload(new ServletFileUpload(new DiskFileItemFactory()), baos.toByteArray()); // calls a and b
        Iterator<FileItem> fileIter = fileItems.iterator();
        add = 16;
        num = 0;
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

public class DiskFileUploadTest {


    /** Proposed test for FILEUPLOAD-293. As of yet, doesn't reproduce the problem.
     */
    @Test
    public void testMoveFile() throws Exception { // unknown test
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

public class Util {

    public static List<FileItem> parseUpload(FileUpload upload, byte[] bytes) throws FileUploadException { // called from SizesTest
        return parseUpload(upload, bytes, Constants.CONTENT_TYPE); // calls a and b
    }

    public static List<FileItem> parseUpload(FileUpload upload, byte[] bytes, String contentType) throws FileUploadException {
        final HttpServletRequest request = new MockHttpServletRequest(bytes, contentType);
        List<FileItem> fileItems = upload.parseRequest(new ServletRequestContext(request)); // calls a and b
        return fileItems;
    }
}
