public interface FileItem {
    /**
     * Deletes the underlying storage for a file item, including deleting any
     * associated temporary disk file. Although this storage will be deleted
     * automatically when the <code>FileItem</code> instance is garbage
     * collected, this method can be used to ensure that this is done at an
     * earlier time, thus preserving system resources.
     */
    void delete(); // b
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
     * Deletes the underlying storage for a file item, including deleting any
     * associated temporary disk file. Although this storage will be deleted
     * automatically when the <code>FileItem</code> instance is garbage
     * collected, this method can be used to ensure that this is done at an
     * earlier time, thus preserving system resources.
     */
    @Override
    public void delete() { // only implementation of b
        cachedContent = null;
        File outputFile = getStoreLocation();
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

        // Check state is as expected
        assertTrue("Initial: in memory", item.isInMemory());
        assertEquals("Initial: size", item.getSize(), testFieldValueBytes.length);
        compareBytes("Initial", item.get(), testFieldValueBytes);
        item.delete(); // call to b
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
