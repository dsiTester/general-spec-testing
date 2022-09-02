public class DiskFileItem {
    /**
     * Returns the contents of the file as an array of bytes.  If the
     * contents of the file were not yet cached in memory, they will be
     * loaded from the disk storage and cached.
     *
     * @return The contents of the file as an array of bytes
     * or {@code null} if the data cannot be read
     */
    @Override
    public byte[] get() {       // definition of a
        if (isInMemory()) {     // call to b
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
     * Provides a hint as to whether or not the file contents will be read
     * from memory.
     *
     * @return <code>true</code> if the file contents will be read
     *         from memory; <code>false</code> otherwise.
     */
    @Override
    public boolean isInMemory() { // definition of b
        if (cachedContent != null) {
            return true;
        }
        return dfos.isInMemory();
    }
}


public class SizesTest {

    @Test
    public void testFileUpload()
        throws IOException, FileUploadException {
        List<FileItem> fileItems =
            Util.parseUpload(new ServletFileUpload(new DiskFileItemFactory()), baos.toByteArray());
        ...
        Iterator<FileItem> fileIter = fileItems.iterator();
        ...
        for (int i = 0;  i < 16384;  i += add) {
            ...
            FileItem item = fileIter.next();
            ...
            byte[] bytes = item.get(); // call to a
            assertEquals(i, bytes.length);
            ...
        }
        ...
    }

}
