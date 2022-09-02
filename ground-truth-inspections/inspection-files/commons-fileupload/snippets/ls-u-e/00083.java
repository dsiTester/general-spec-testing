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
        String charset = getCharSet(); // call to b
        ...
    }

    public String getCharSet() { // definition of b
        ParameterParser parser = new ParameterParser();
        parser.setLowerCaseNames(true);
        // Parameter parser can handle null input
        Map<String, String> params = parser.parse(getContentType(), ';');
        return params.get("charset");
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
