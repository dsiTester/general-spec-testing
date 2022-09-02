public interface FileItem {
    /**
     * Provides a hint as to whether or not the file contents will be read
     * from memory.
     *
     * @return <code>true</code> if the file contents will be read from memory;
     *         <code>false</code> otherwise.
     */
    boolean isInMemory(); // b
}

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
    public boolean isInMemory() { // only implementation of b
        if (cachedContent != null) {
            return true;
        }
        return dfos.isInMemory();
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

        assertTrue("Initial: in memory", item.isInMemory()); // call to b
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
        ...

        return item;

    }
}
