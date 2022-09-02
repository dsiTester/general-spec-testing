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
            fileData = null;
        } finally {
            IOUtils.closeQuietly(fis);
        }

        return fileData;
    }

    @Override
    public String getString() { // definition of b
        byte[] rawdata = get(); // a is called in the very first line...
        ...
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
        assertEquals(item.getString(), textFieldValue); // call to b
        ...
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
