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
    public OutputStream getOutputStream()
        throws IOException { // called from tests and FileUploadBase.parseRequest()
        if (dfos == null) {
            File outputFile = getTempFile(); // call to a
            dfos = new DeferredFileOutputStream(sizeThreshold, outputFile);
        }
        return dfos;
    }

    @Override
    public void write(File file) throws Exception {
        if (isInMemory()) { // caller of a is a precondition to this call
            ...
        } else {
            File outputFile = getStoreLocation(); // call to b
            // NOTE: uncomment below to call a after b
            // System.out.println(getTempFile().getName());
            if (outputFile != null) {
                ...
            } else {
                /*
                 * For whatever reason we cannot write the
                 * file to disk.
                 */
                throw new FileUploadException(
                    "Cannot write uploaded file to disk!");
            }
        }
    }

    @Override
    public void delete() { // called from DiskFileItemSerializeTest.testInMemoryObject() and DefaultFileItemTest.doTestAboveThreshold(); called from validating test when DSI replacement value causes a NullPointerException
        cachedContent = null;
        File outputFile = getStoreLocation(); // call to b
        if (outputFile != null && !isInMemory() && outputFile.exists()) {
            outputFile.delete();
        }
    }

}

public class FileUploadBase {

    @Deprecated
    public List<FileItem> parseRequest(HttpServletRequest req)
    throws FileUploadException { // called from DiskFileUploadTest#testMoveFile
        return parseRequest(new ServletRequestContext(req)); // calls a
    }

    public List<FileItem> parseRequest(RequestContext ctx)
            throws FileUploadException {
        List<FileItem> items = new ArrayList<FileItem>();
        boolean successful = false;
        try {
            ...
            while (iter.hasNext()) {
                ...
                FileItem fileItem = fac.createItem(item.getFieldName(), item.getContentType(),
                                                   item.isFormField(), fileName);
                items.add(fileItem);
                try {
                    Streams.copy(item.openStream(), fileItem.getOutputStream(), true, buffer); // calls a; NullPointerException here
                }
                ...
            }
            ...
        }
        ...
        finally {
            if (!successful) {
                for (FileItem fileItem : items) {
                    try {
                        fileItem.delete(); // calls method-b
                    } catch (Exception ignored) {
                        // ignored TODO perhaps add to tracker delete failure list somehow?
                    }
                }
            }
        }
    }
}


public class DiskFileUploadTest {

    @Test
    public void testMoveFile() throws Exception { // validated test
        DiskFileUpload myUpload = new DiskFileUpload();
        myUpload.setSizeThreshold(0);
    	final String content = ...;
    	final byte[] contentBytes = content.getBytes("US-ASCII");
        final HttpServletRequest request = new MockHttpServletRequest(contentBytes, Constants.CONTENT_TYPE);
        final List<FileItem> items = myUpload.parseRequest(request); // calls a; throws NullPointerException
        ...
        final File out = File.createTempFile("install", ".tmp");
        dfi.write(out); // calls b
    }

}

public class DiskFileItemSerializeTest {

    @Test
    public void testBelowThreshold() { // invalidated test
        // Create the FileItem
        byte[] testFieldValueBytes = createContentBytes(threshold - 1);
        testInMemoryObject(testFieldValueBytes);
    }

    private void testInMemoryObject(byte[] testFieldValueBytes) {
        testInMemoryObject(testFieldValueBytes, REPO);
    }

    public void testInMemoryObject(byte[] testFieldValueBytes, File repository) {
        FileItem item = createFileItem(testFieldValueBytes, repository); // calls a

        // Check state is as expected
        assertTrue("Initial: in memory", item.isInMemory());
        assertEquals("Initial: size", item.getSize(), testFieldValueBytes.length);
        compareBytes("Initial", item.get(), testFieldValueBytes);
        item.delete(); // calls b
    }

    private FileItem createFileItem(byte[] contentBytes, File repository) {
        ...
        FileItem item = factory.createItem(
                textFieldName,
                textContentType,
                true,
                "My File Name"
        );
        try {
            OutputStream os = item.getOutputStream(); // calls a
            os.write(contentBytes); // why doesn't this throw a NullPointerException?
            os.close();
        } catch(IOException e) {
            fail("Unexpected IOException" + e);
        }

        return item;

    }
}

public class DefaultFileItemTest {

    /**
     * Test creation of a field for which the amount of data falls above the
     * configured threshold, where no specific repository is configured.
     */
    @Test
    public void testAboveThresholdDefaultRepository() { // unknown test
        doTestAboveThreshold(null);
    }

    /**
     * Common code for cases where the amount of data is above the configured
     * threshold, but the ultimate destination of the data has not yet been
     * determined.
     *
     * @param repository The directory within which temporary files will be
     *                   created.
     */
    public void doTestAboveThreshold(File repository) { // unknown verdict test
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

        try {
            OutputStream os = item.getOutputStream();
            os.write(testFieldValueBytes); // NullPointerException here
            os.close();
        } catch(IOException e) {
            fail("Unexpected IOException");
        }
        ...

        item.delete(); // calls b
    }
}

