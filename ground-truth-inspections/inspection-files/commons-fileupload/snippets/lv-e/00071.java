public interface FileItem {
    long getSize(); // a
    void delete(); // b
}

public class DiskFileItem implements FileItem {

    @Override
    public String getString() { // implementation of a
        byte[] rawdata = get(); // if b is called before a. then this call to get() returns null
        String charset = getCharSet();
        if (charset == null) {
            charset = defaultCharset;
        }
        try {
            return new String(rawdata, charset); // NullPointerException thrown here
        } catch (UnsupportedEncodingException e) {
            return new String(rawdata);
        }
    }

    @Override
    public void delete() { // implementation of b
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
        ...
        assertEquals(item.getSize(), testFieldValueBytes.length); // call to a
        ...
        item.delete(); // call to b
    }

}
