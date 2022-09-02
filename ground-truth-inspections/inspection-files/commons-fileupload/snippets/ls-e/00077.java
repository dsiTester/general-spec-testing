public interface FileItem {
    boolean isInMemory(); // a
}

public class DiskFileItem implements FileItem {

    @Override
    public boolean isInMemory() { // only implmentation of a
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

}

public class DefaultFileItemTest {

    @Test
    public void testAboveThresholdDefaultRepository() {
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

        assertTrue(item instanceof DefaultFileItem);
        DefaultFileItem dfi = (DefaultFileItem) item;
        File storeLocation = dfi.getStoreLocation(); // call to b
        assertNotNull(storeLocation);
        assertTrue(storeLocation.exists());
        assertEquals(storeLocation.length(), testFieldValueBytes.length);
        ...
    }

}
