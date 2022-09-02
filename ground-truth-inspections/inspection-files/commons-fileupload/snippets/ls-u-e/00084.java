public interface FileItem {
    boolean isInMemory(); // a
}

public class DiskFileItem implements FileItem {

    @Override
    public boolean isInMemory() { // only implmentation of a
        if (cachedContent != null) {
            return true;
        }
        return dfos.isInMemory();
    }

    @Override
    public String getString() {
        ...
        String charset = getCharSet(); // calls b
        ...
    }

    public String getCharSet() { // calls b
        ...
        Map<String, String> params = parser.parse(getContentType(), ';'); // call to b
        return params.get("charset");
    }

    @Override
    public String getContentType() { // definition of b
        return contentType;
    }

}

public class DefaultFileItemTest {

    @Test
    public void testAboveThresholdDefaultRepository() { // invalidated case
        doTestAboveThreshold(null); // calls a and b
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
        assertNotNull(item);
        ...
        assertFalse(item.isInMemory()); // call to a
        ...
        assertEquals(item.getString(), textFieldValue); // calls b
        ...
    }

    @Test
    public void testBelowThreshold() { // unknown case
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
        ...
        assertTrue(item.isInMemory()); // call to a
        ...
        assertEquals(item.getString(), textFieldValue); // calls b
    }

}
