public interface FileItem {
    boolean isInMemory(); // a
}

public class DiskFileItem {

    @Override
    public boolean isInMemory() { // only implementation of a
        if (cachedContent != null) {
            return true;
        }
        return dfos.isInMemory();
    }

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
    public void delete() { // calls b
        cachedContent = null;
        File outputFile = getStoreLocation(); // call to b
        if (outputFile != null && !isInMemory() && outputFile.exists()) {
            outputFile.delete();
        }
    }

}

public class DefaultFileItemTest {

    @Test
    public void testAboveThresholdDefaultRepository() { // invalidated case
        doTestAboveThreshold(null);
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
        ...
        assertFalse(item.isInMemory()); // call to a
        ...
        item.delete(); // calls b
    }


}

public class DiskFileItemSerializeTest {

    @Test
    public void testBelowThreshold() { // unknown case
        // Create the FileItem
        byte[] testFieldValueBytes = createContentBytes(threshold - 1);
        testInMemoryObject(testFieldValueBytes);
    }

    public void testInMemoryObject(byte[] testFieldValueBytes, File repository) {
        FileItem item = createFileItem(testFieldValueBytes, repository);
        assertTrue("Initial: in memory", item.isInMemory()); // call to a
        ...
        item.delete(); // calls b
    }

}
