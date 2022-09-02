public interface FileItem {
    boolean isInMemory(); // a

    void delete(); // b
}

public class DiskFileItem implements FileItem {

    @Override
    public boolean isInMemory() { // only implmentation of a
        if (cachedContent != null) {
            return true;
        }
        return dfos.isInMemory();
    }

    @Override
    public void delete() { // only implementation of b
        cachedContent = null;
        File outputFile = getStoreLocation();
        if (outputFile != null && !isInMemory() && outputFile.exists()) {
            outputFile.delete();
        }
    }

}

public class DefaultFileItemTest {

    @Test
    public void testAboveThresholdDefaultRepository() { // invalidated case
        doTestAboveThreshold(null); // calls a and b
    }

    public void doTestAboveThreshold(File repository) {
        FileItemFactory factory = createFactory(repository);
        String textFieldName = "textField";
        String textFieldValue = "01234567890123456789";
        byte[] testFieldValueBytes = textFieldValue.getBytes();

        FileItem item = factory.createItem(
                textFieldName,
                textContentType,
                true,
                null
        );
        assertNotNull(item);
        ...
        assertFalse(item.isInMemory()); // call to a
        ...
        item.delete(); // call to b
    }

}

public class DiskFileItemSerializeTest {

    @Test
    public void testBelowThreshold() { // unknown case
        // Create the FileItem
        byte[] testFieldValueBytes = createContentBytes(threshold - 1);
        testInMemoryObject(testFieldValueBytes); // calls a and b
    }

    public void testInMemoryObject(byte[] testFieldValueBytes, File repository) {
        FileItem item = createFileItem(testFieldValueBytes, repository);

        // Check state is as expected
        assertTrue("Initial: in memory", item.isInMemory()); // call to a
        assertEquals("Initial: size", item.getSize(), testFieldValueBytes.length);
        compareBytes("Initial", item.get(), testFieldValueBytes);
        item.delete(); // call to b
    }

}
