public interface FileItem {
    /**
     * Returns the name of the field in the multipart form corresponding to
     * this file item.
     *
     * @return The name of the form field.
     */
    String getFieldName(); // b
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
     * Returns the name of the field in the multipart form corresponding to
     * this file item.
     *
     * @return The name of the form field.
     *
     * @see #setFieldName(java.lang.String)
     *
     */
    @Override
    public String getFieldName() { // only implementation of b
        return fieldName;
    }

    @Override
    public OutputStream getOutputStream()
        throws IOException { // called by FileUploadBase.parseRequest()
        if (dfos == null) {
            File outputFile = getTempFile(); // call to a
            dfos = new DeferredFileOutputStream(sizeThreshold, outputFile);
        }
        return dfos;
    }

}

public class FileUploadBase {
    public List<FileItem> parseRequest(RequestContext ctx)
        throws FileUploadException {
        List<FileItem> items = new ArrayList<FileItem>();
        boolean successful = false;
        try {
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
                }
                ...
            }
            ...
        }
        ...
    }
}

public class Util {

    public static List<FileItem> parseUpload(FileUpload upload, byte[] bytes) throws FileUploadException { // called from test
        return parseUpload(upload, bytes, Constants.CONTENT_TYPE); // calls a
    }

    public static List<FileItem> parseUpload(FileUpload upload, byte[] bytes, String contentType) throws FileUploadException { // calls a
        final HttpServletRequest request = new MockHttpServletRequest(bytes, contentType);
        List<FileItem> fileItems = upload.parseRequest(new ServletRequestContext(request)); // calls a
        return fileItems;
    }

}

public class SizesTest {
    /**
     * Runs a test with varying file sizes.
     */
    @Test
    public void testFileUpload()
        throws IOException, FileUploadException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int add = 16;
        int num = 0;
        for (int i = 0;  i < 16384;  i += add) {
            if (++add == 32) {
                add = 16;
            }
            String header = "-----1234\r\n"
                + "Content-Disposition: form-data; name=\"field" + (num++) + "\"\r\n"
                + "\r\n";
            baos.write(header.getBytes("US-ASCII"));
            for (int j = 0;  j < i;  j++) {
                baos.write((byte) j);
            }
            baos.write("\r\n".getBytes("US-ASCII"));
        }
        baos.write("-----1234--\r\n".getBytes("US-ASCII"));

        List<FileItem> fileItems =
            Util.parseUpload(new ServletFileUpload(new DiskFileItemFactory()), baos.toByteArray()); // calls a
        Iterator<FileItem> fileIter = fileItems.iterator();
        add = 16;
        num = 0;
        for (int i = 0;  i < 16384;  i += add) {
            if (++add == 32) {
                add = 16;
            }
            FileItem item = fileIter.next();
            assertEquals("field" + (num++), item.getFieldName()); // call to b
            byte[] bytes = item.get();
            assertEquals(i, bytes.length);
            for (int j = 0;  j < i;  j++) {
                assertEquals((byte) j, bytes[j]);
            }
        }
        assertTrue(!fileIter.hasNext());
    }
}
