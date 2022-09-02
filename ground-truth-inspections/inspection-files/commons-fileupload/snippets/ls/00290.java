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
     * Returns the {@link java.io.File} object for the <code>FileItem</code>'s
     * data's temporary location on the disk. Note that for
     * <code>FileItem</code>s that have their data stored in memory,
     * this method will return <code>null</code>. When handling large
     * files, you can use {@link java.io.File#renameTo(java.io.File)} to
     * move the file to new location without copying the data, if the
     * source and destination locations reside within the same logical
     * volume.
     *
     * @return The data file, or <code>null</code> if the data is stored in
     *         memory.
     */
    public File getStoreLocation() { // definition of b
        if (dfos == null) {
            return null;
        }
        if (isInMemory()) {
            return null;
        }
        return dfos.getFile();
    }

    @Override
    public void delete() { // called from test
        ...
        File outputFile = getStoreLocation(); // call to b
        if (outputFile != null && !isInMemory() && outputFile.exists()) {
            outputFile.delete();
        }
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
        FileItem item = createFileItem(testFieldValueBytes, repository); // calls a

        ...
        item.delete(); // calls b
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
        ...

        return item;

    }
}
