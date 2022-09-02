public class DiskFileItem {

    public File getStoreLocation() { // definition of a
        if (dfos == null) {
            return null;
        }
        if (isInMemory()) {
            return null;
        }
        return dfos.getFile();
    }

    @Override
    public void delete() {
        cachedContent = null;
        File outputFile = getStoreLocation();
        if (outputFile != null && !isInMemory() && outputFile.exists()) {
            outputFile.delete();
        }
    }

}

public class DefaultFileItemTest {
    @Test
    public void testAboveThresholdDefaultRepository() {
        doTestAboveThreshold(null);
    }

    @Test
    public void testAboveThresholdSpecifiedRepository() throws IOException {
        String tempPath = System.getProperty("java.io.tmpdir");
        String tempDirName = "testAboveThresholdSpecifiedRepository";
        File tempDir = new File(tempPath, tempDirName);
        FileUtils.forceMkdir(tempDir);
        doTestAboveThreshold(tempDir);
        assertTrue(tempDir.delete());
    }

    public void doTestAboveThreshold(File repository) {
        FileItemFactory factory = createFactory(repository);
        ...
        FileItem item = factory.createItem(...);
        assertNotNull(item);
        ...
        assertTrue(item instanceof DefaultFileItem);
        DefaultFileItem dfi = (DefaultFileItem) item;
        File storeLocation = dfi.getStoreLocation(); // a
        assertNotNull(storeLocation); // testAboveThresholdDefaultRepository fails here
        assertTrue(storeLocation.exists());
        assertEquals(testFieldValueBytes.length, storeLocation.length()); // testAboveThresholdSpecifiedRepository fails here
        ...
        item.delete(); // b
    }

}
