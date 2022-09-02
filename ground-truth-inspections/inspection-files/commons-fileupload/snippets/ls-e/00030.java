public class DiskFileItem {
    @Override
    public byte[] get() { // definition of a
        if (isInMemory()) {
            if (cachedContent == null && dfos != null) {
                cachedContent = dfos.getData();
            }
            return cachedContent;
        }

        byte[] fileData = new byte[(int) getSize()];
        InputStream fis = null;

        try {
            fis = new FileInputStream(dfos.getFile());
            IOUtils.readFully(fis, fileData);
        } catch (IOException e) {
            fileData = null; // reason why calling method-a after method-b would be ok
        } finally {
            IOUtils.closeQuietly(fis);
        }

        return fileData;
    }

    @Override
    public void delete() { // definition of b
        cachedContent = null;
        File outputFile = getStoreLocation();
        if (outputFile != null && !isInMemory() && outputFile.exists()) {
            outputFile.delete();
        }
    }


}

public class DefaultFileItemTest {
    public void doTestAboveThreshold(File repository) {
        FileItemFactory factory = createFactory(repository);
        String textFieldName = "textField";
        String textFieldValue = "01234567890123456789";
        byte[] testFieldValueBytes = textFieldValue.getBytes(); // DSI replaces return value of a with this value.

        FileItem item = factory.createItem(textFieldName, textContentType, true,null);
        ...
        assertTrue(Arrays.equals(item.get(), testFieldValueBytes)); // call to a
        ...
        item.delete(); // call to b
    }

    @Test
    public void testAboveThresholdDefaultRepository() { // invalidated case
        doTestAboveThreshold(null);
    }

    @Test
    public void testAboveThresholdSpecifiedRepository() throws IOException { // error case
        String tempPath = System.getProperty("buildDirectory");
        String tempDirName = "testAboveThresholdSpecifiedRepository";
        File tempDir = new File(tempPath, tempDirName);
        FileUtils.forceMkdir(tempDir);
        doTestAboveThreshold(tempDir); // calls a and b
        assertTrue(tempDir.delete());
    }

}

public class ModifiedDefaultFileItemTest {
    public void doTestAboveThreshold(File repository) {
        FileItemFactory factory = createFactory(repository);
        String textFieldName = "textField";
        String textFieldValue = "01234567890123456789";
        byte[] testFieldValueBytes = textFieldValue.getBytes(); // DSI replaces return value of a with this value.

        FileItem item = factory.createItem(textFieldName, textContentType, true,null);
        ...
        // NOTE: comment out call to a
        // assertTrue(Arrays.equals(item.get(), testFieldValueBytes)); // call to a
        ...
        item.delete(); // call to b
        // NOTE: include another call to a after call to b
        assertTrue(Arrays.equals(item.get(), testFieldValueBytes)); // call to a - this assertion will fail.
    }



public class DiskFileItemSerializeTest {

    @Test
    public void testValidRepository() {
        // Create the FileItem
        byte[] testFieldValueBytes = createContentBytes(threshold);
        testInMemoryObject(testFieldValueBytes, REPO); // calls a and b
    }

    public void testInMemoryObject(byte[] testFieldValueBytes, File repository) {
        FileItem item = createFileItem(testFieldValueBytes, repository);
        ...
        compareBytes("Initial", item.get(), testFieldValueBytes); // call to a
        item.delete(); // call to b
    }
}
