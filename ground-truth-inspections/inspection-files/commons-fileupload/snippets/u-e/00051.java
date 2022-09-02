public interface FileItem {

    OutputStream getOutputStream() throws IOException; // a

    /**
     * Provides a hint as to whether or not the file contents will be read
     * from memory.
     *
     * @return <code>true</code> if the file contents will be read from memory;
     *         <code>false</code> otherwise.
     */
    boolean isInMemory();       // b
}

public class DiskFileItem implements FileItem {

    @Override
    public OutputStream getOutputStream()
        throws IOException { // only implementation of a
        if (dfos == null) {
            File outputFile = getTempFile();
            dfos = new DeferredFileOutputStream(sizeThreshold, outputFile);
        }
        return dfos;
    }

    @Override
    public boolean isInMemory() { // only implementation of b
        if (cachedContent != null) {
            return true;
        }
        return dfos.isInMemory(); // throws NullPointerException
    }


}

public class DefaultFileItemTest {

    @Test
    public void testBelowThreshold() {
        FileItemFactory factory = createFactory(null);
        String textFieldName = "textField";
        String textFieldValue = "0123456789";
        byte[] testFieldValueBytes = textFieldValue.getBytes();

        FileItem item = factory.createItem(
                textFieldName,
                textContentType,
                true,
                null
        );
        assertNotNull(item);

        try {
            OutputStream os = item.getOutputStream(); // call to a
            os.write(testFieldValueBytes); // NullPointerException here
            os.close();
        } ...
        assertTrue(item.isInMemory()); // call to b
        ...
    }

}
