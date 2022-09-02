public class DiskFileItem implements FileItem {
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

    /**
     * Returns the content charset passed by the agent or <code>null</code> if
     * not defined.
     *
     * @return The content charset passed by the agent or <code>null</code> if
     *         not defined.
     */
    public String getCharSet() { // definition of b
        ParameterParser parser = new ParameterParser();
        parser.setLowerCaseNames(true);
        // Parameter parser can handle null input
        Map<String, String> params = parser.parse(getContentType(), ';');
        return params.get("charset");
    }

    @Override
    public String getString() { // called from test
        // NOTE: swap the next two lines to show that the spec is spurious
        byte[] rawdata = get(); // call to a
        String charset = getCharSet(); // call to b
        if (charset == null) {
            charset = defaultCharset;
        }
        try {
            return new String(rawdata, charset); // NullPointerException here
        } catch (UnsupportedEncodingException e) {
            return new String(rawdata);
        }
    }
}

public class DefaultFileItemTest {
    /**
     * Test creation of a field for which the amount of data falls above the
     * configured threshold, where no specific repository is configured.
     */
    @Test
    public void testAboveThresholdDefaultRepository() {
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
    public void doTestAboveThreshold(File repository) {
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
            os.write(testFieldValueBytes);
            os.close();
        } catch(IOException e) {
            fail("Unexpected IOException");
        }
        assertFalse(item.isInMemory());
        assertEquals(item.getSize(), testFieldValueBytes.length);
        assertTrue(Arrays.equals(item.get(), testFieldValueBytes));
        assertEquals(item.getString(), textFieldValue); // calls a and b

        assertTrue(item instanceof DefaultFileItem);
        DefaultFileItem dfi = (DefaultFileItem) item;
        File storeLocation = dfi.getStoreLocation();
        assertNotNull(storeLocation);
        assertTrue(storeLocation.exists());
        assertEquals(storeLocation.length(), testFieldValueBytes.length);

        if (repository != null) {
            assertEquals(storeLocation.getParentFile(), repository);
        }

        item.delete();
    }
}
