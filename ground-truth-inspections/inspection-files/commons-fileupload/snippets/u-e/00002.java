public class DefaultFileItemTest {

    public void doTestAboveThreshold(File repository) {
        ...
        FileItem item = factory.createItem(textFieldName, textContentType, true, null);
        DefaultFileItem dfi = (DefaultFileItem) item;
        File storeLocation = dfi.getStoreLocation(); // a
        assertNotNull(storeLocation); // fails here for stage-0-failure case
        assertTrue(storeLocation.exists());
        ...
        item.delete(); // calls b
    }

    @Test
    public void testAboveThresholdSpecifiedRepository() throws IOException {
        String tempPath = System.getProperty("java.io.tmpdir"); // this test results in a sanity check fail because of this line.
        String tempDirName = "testAboveThresholdSpecifiedRepository";
        File tempDir = new File(tempPath, tempDirName);
        FileUtils.forceMkdir(tempDir);
        doTestAboveThreshold(tempDir);
        assertTrue(tempDir.delete()); // assertion fails here for error case
    }

    /*
      NOTE: The below methods do not exist in the original commons-fileupload.
      One possible way to avoid the STATE_POLLUTION_BY_DSI...
      We need to completely clean up the testAboveThresholdSpecifiedRepository directory.
    */

    boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    @After
    public void cleanUp() {
        String tempPath = System.getProperty("buildDirectory");
        File dir = new File(tempPath);
        File[] b = dir.listFiles();
        for (File f : b) {
            if (f.getName().equals("testAboveThresholdSpecifiedRepository")) {
                deleteDirectory(f);
            }
        }
    }

}

public class DiskFileItem {
    public File getStoreLocation() { // both a and b, since DefaultFileItem does not implement getStoreLocation
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
        File outputFile = getStoreLocation(); // b
        if (outputFile != null && !isInMemory() && outputFile.exists()) {
            outputFile.delete();
        }
    }
}
