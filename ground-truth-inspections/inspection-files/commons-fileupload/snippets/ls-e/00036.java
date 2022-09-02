public interface FileItem {
    ...
    void delete(); // calls b
    ...
}


public class DiskFileItem implements FileItem {

    @Override
    public void delete() { // implements FileItem.delete()
        cachedContent = null;
        File outputFile = getStoreLocation(); // call to b
        if (outputFile != null && !isInMemory() && outputFile.exists()) {
            outputFile.delete();
        }
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


public class DiskFileItemSerializeTest {

    @Test
    public void testAboveThreshold() {
        // Create the FileItem
        byte[] testFieldValueBytes = createContentBytes(threshold + 1);
        FileItem item = createFileItem(testFieldValueBytes);
        ...
        compareBytes("Initial", item.get(), testFieldValueBytes); // call to a

        item.delete(); // calls b
    }

}
