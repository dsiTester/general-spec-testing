public class DiskFileItem implements FileItem {
    /**
     * Returns the size of the file.
     *
     * @return The size of the file, in bytes.
     */
    @Override
    public long getSize() { // definition of a
        if (size >= 0) {
            return size;
        } else if (cachedContent != null) {
            return cachedContent.length;
        } else if (dfos.isInMemory()) {
            return dfos.getData().length;
        } else {
            return dfos.getFile().length();
        }
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

    @Override
    public byte[] get() {
        if (isInMemory()) {
            if (cachedContent == null && dfos != null) {
                cachedContent = dfos.getData();
            }
            return cachedContent;
        }

        byte[] fileData = new byte[(int) getSize()]; // call to a?
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
    public void delete() { // called from test
        cachedContent = null;
        File outputFile = getStoreLocation(); // call to b?
        // NOTE: uncomment the following to call a after b
        // System.out.println(getSize());
        if (outputFile != null && !isInMemory() && outputFile.exists()) {
            outputFile.delete();
        }
    }
}

public class DiskFileItemSerializeTest {
    /**
     * Test creation of a field for which the amount of data falls above the
     * configured threshold.
     */
    @Test
    public void testAboveThreshold() {
        // Create the FileItem
        byte[] testFieldValueBytes = createContentBytes(threshold + 1);
        FileItem item = createFileItem(testFieldValueBytes);

        // Check state is as expected
        assertFalse("Initial: in memory", item.isInMemory());
        assertEquals("Initial: size", item.getSize(), testFieldValueBytes.length);
        compareBytes("Initial", item.get(), testFieldValueBytes); // calls a?

        // NOTE: calling a before b here:
        // System.out.println(item.getSize());
        item.delete(); // call to b?
        // NOTE: calling a after b here:
        // System.out.println(item.getSize());

        // The two values will be different
    }

}
