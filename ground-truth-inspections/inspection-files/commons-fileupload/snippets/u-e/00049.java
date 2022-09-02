public interface FileItem {

    OutputStream getOutputStream() throws IOException; // a

    long getSize(); // b

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
    public long getSize() { // only implementation of b
        if (size >= 0) {
            return size;
        } else if (cachedContent != null) {
            return cachedContent.length;
        } else if (dfos.isInMemory()) { // this line would crash if method-a was not called before method-b
            return dfos.getData().length;
        } else {
            return dfos.getFile().length();
        }
    }

}

public class DiskFileItemSerializeTest {

    @Test
    public void testAboveThreshold() {
        // Create the FileItem
        byte[] testFieldValueBytes = createContentBytes(threshold + 1);
        FileItem item = createFileItem(testFieldValueBytes);

        // Check state is as expected
        assertFalse("Initial: in memory", item.isInMemory());
        assertEquals("Initial: size", item.getSize(), testFieldValueBytes.length); // call to b?
        compareBytes("Initial", item.get(), testFieldValueBytes);

        item.delete();
    }

    private FileItem createFileItem(byte[] contentBytes) {
        return createFileItem(contentBytes, REPO);
    }

    private FileItem createFileItem(byte[] contentBytes, File repository) {
        FileItemFactory factory = new DiskFileItemFactory(threshold, repository);
        String textFieldName = "textField";

        FileItem item = factory.createItem(
                textFieldName,
                textContentType,
                true,
                "My File Name"
        );
        try {
            OutputStream os = item.getOutputStream(); // call to a?
            os.write(contentBytes);
            os.close();
        } catch(IOException e) {
            fail("Unexpected IOException" + e);
        }

        return item;

    }
}

public class ModifiedDiskFileItemSerializeTest {

    @Test
    public void testAboveThreshold() {
        // Create the FileItem
        byte[] testFieldValueBytes = createContentBytes(threshold + 1);
        FileItem item = createFileItem(testFieldValueBytes); // calls a?

        // Check state is as expected
        // NOTE: below commented out from original test
        // assertFalse("Initial: in memory", item.isInMemory());
        assertEquals("Initial: size", item.getSize(), testFieldValueBytes.length); // call to b? - NullPointerException here
        // NOTE: following added in for experimentation
        try {
            OutputStream os = item.getOutputStream(); // call to a?
            os.write(contentBytes);
            os.close();
        } catch(IOException e) {
            fail("Unexpected IOException" + e);
        }
        compareBytes("Initial", item.get(), testFieldValueBytes);

        item.delete();
    }

    private FileItem createFileItem(byte[] contentBytes) {
        return createFileItem(contentBytes, REPO); // calls a?
    }

    private FileItem createFileItem(byte[] contentBytes, File repository) {
        FileItemFactory factory = new DiskFileItemFactory(threshold, repository);
        String textFieldName = "textField";

        FileItem item = factory.createItem(
                textFieldName,
                textContentType,
                true,
                "My File Name"
        );
        // NOTE: below commented out from original test for experimentation
        // try {
        //     OutputStream os = item.getOutputStream(); // call to a?
        //     os.write(contentBytes);
        //     os.close();
        // } catch(IOException e) {
        //     fail("Unexpected IOException" + e);
        // }

        return item;

    }
}
