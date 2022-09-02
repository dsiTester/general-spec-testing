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
    protected File getTempFile() { // definition of b
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

    @Override
    public OutputStream getOutputStream()
        throws IOException {
        if (dfos == null) {
            File outputFile = getTempFile(); // call to b
            dfos = new DeferredFileOutputStream(sizeThreshold, outputFile);
        }
        return dfos;
    }

}

public class DiskFileItemFactory {

    @Override
    public FileItem createItem(String fieldName, String contentType,
            boolean isFormField, String fileName) { // called from DiskFileItemSerializeTest.createFileItem()
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

public class DiskFileItemSerializeTest {

    @Test
    public void testBelowThreshold() {
        // Create the FileItem
        byte[] testFieldValueBytes = createContentBytes(threshold - 1);
        testInMemoryObject(testFieldValueBytes); // calls a and b
    }

    private void testInMemoryObject(byte[] testFieldValueBytes) {
        testInMemoryObject(testFieldValueBytes, REPO); // calls a and b
    }

    public void testInMemoryObject(byte[] testFieldValueBytes, File repository) {
        FileItem item = createFileItem(testFieldValueBytes, repository); // calls a and b

        ...
        item.delete();
    }

    private FileItem createFileItem(byte[] contentBytes, File repository) {
        FileItemFactory factory = new DiskFileItemFactory(threshold, repository);
        String textFieldName = "textField";

        FileItem item = factory.createItem( // calls a
                textFieldName,
                textContentType,
                true,
                "My File Name"
        );
        try {
            OutputStream os = item.getOutputStream(); // calls b
            os.write(contentBytes);
            os.close();
        } catch(IOException e) {
            fail("Unexpected IOException" + e);
        }

        return item;

    }
}
