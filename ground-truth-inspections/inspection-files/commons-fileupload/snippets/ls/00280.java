public class DiskFileItem {

    /**
     * Creates and returns a {@link java.io.File File} representing a uniquely
     * named temporary file in the configured repository path. The lifetime of
     * the file is tied to the lifetime of the <code>FileItem</code> instance;
     * the file will be deleted when the instance is garbage collected.
     * <p>
     * <b>Note: Subclasses that override this method must ensure that they return the
     * same File each time.</b>
     *
     * @return The {@link java.io.File File} to be used for temporary storage.
     */
    protected File getTempFile() { // definition of a
        if (tempFile == null) {
            File tempDir = repository;
            if (tempDir == null) {
                tempDir = new File(System.getProperty("java.io.tmpdir"));
            }

            String tempFileName = format("upload_%s_%s.tmp", UID, getUniqueId());

            tempFile = new File(tempDir, tempFileName);
        }
        return tempFile;
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

    @Override
    public OutputStream getOutputStream()
        throws IOException { // called from FileUploadBase.parseRequest()
        if (dfos == null) {
            File outputFile = getTempFile(); // call to a
            dfos = new DeferredFileOutputStream(sizeThreshold, outputFile);
        }
        return dfos;
    }

    @Override
    public byte[] get() {
        if (isInMemory()) { // call to b
            ...
            return cachedContent;
        }
        ...
        return fileData;
    }

}

public class SizesTest {
    @Test
    public void testFileUpload()
        throws IOException, FileUploadException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ...
        List<FileItem> fileItems =
            Util.parseUpload(new ServletFileUpload(new DiskFileItemFactory()), baos.toByteArray()); // calls a
        Iterator<FileItem> fileIter = fileItems.iterator();
        ...
        for (int i = 0;  i < 16384;  i += add) {
            ...
            byte[] bytes = item.get(); // calls b?
            assertEquals(i, bytes.length);
            ...
        }
        assertTrue(!fileIter.hasNext());
    }
}
