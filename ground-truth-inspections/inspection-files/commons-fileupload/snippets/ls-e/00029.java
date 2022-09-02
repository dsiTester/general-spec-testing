public interface FileItem extends FileItemHeadersSupport {
    /**
     * Returns the contents of the file item as an array of bytes.
     *
     * @return The contents of the file item as an array of bytes.
     */
    byte[] get();               // a

}

public class DiskFileItem
    implements FileItem {

    /**
     * Returns the contents of the file as an array of bytes.  If the
     * contents of the file were not yet cached in memory, they will be
     * loaded from the disk storage and cached.
     *
     * @return The contents of the file as an array of bytes
     * or {@code null} if the data cannot be read
     */
    @Override
    public byte[] get() {       // only implementation of a
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

    /**
     * Returns the {@link java.io.File} object for the <code>FileItem</code>'s
     * data's temporary location on the disk. Note that for
     * <code>FileItem</code>s that have their data stored in memory,
     * this method will return <code>null</code>. When handling large
     * files, you can use {@link java.io.File#renameTo(java.io.File)} to
     * move the file to new location without copying the data, if the
     * source and destination locations reside within the same logical
     * volume.
     *
     * @return The data file, or <code>null</code> if the data is stored in
     *         memory.
     */
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
    public void doTestAboveThreshold(File repository) {
        FileItemFactory factory = createFactory(repository);
        String textFieldName = "textField";
        String textFieldValue = "01234567890123456789";
        byte[] testFieldValueBytes = textFieldValue.getBytes(); // DSI replaces return value of a with this value.

        FileItem item = factory.createItem(textFieldName, textContentType, true,null);
        ...
        assertTrue(Arrays.equals(item.get(), testFieldValueBytes)); // call to a
        ...
        DefaultFileItem dfi = (DefaultFileItem) item;
        File storeLocation = dfi.getStoreLocation(); // call to b
        assertNotNull(storeLocation);
        ...
    }

    @Test
    public void testAboveThresholdDefaultRepository() { // stage-0-failure case
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
