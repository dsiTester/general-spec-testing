public interface FileItem {
    long getSize(); // a

}

public class DiskFileItem implements FileItem {

    @Override
    public long getSize() { // implementation of a
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

    @Override
    public String getString() { // calls b?
        ...
        String charset = getCharSet(); // call to b?
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
            OutputStream os = item.getOutputStream();
            os.write(testFieldValueBytes);
            os.close();
        } catch(IOException e) {
            fail("Unexpected IOException");
        }
        assertTrue(item.isInMemory());
        assertEquals(item.getSize(), testFieldValueBytes.length); // call to a?
        assertTrue(Arrays.equals(item.get(), testFieldValueBytes));
        assertEquals(item.getString(), textFieldValue); // calls b?
    }

}
