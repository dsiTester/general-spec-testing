public class ParameterParser {

    public Map<String, String> parse(final String str, char separator) { // definition of a
        if (str == null) {
            return new HashMap<String, String>();
        }
        return parse(str.toCharArray(), separator); // call to b
    }

    public Map<String, String> parse(final char[] charArray, char separator) { // definition of b
        if (charArray == null) {
            return new HashMap<String, String>();
        }
        return parse(charArray, 0, charArray.length, separator);
    }

}

public class DiskFileItem {

    @Override
    public String getString() {
        byte[] rawdata = get();
        String charset = getCharSet(); // calls a and b
        ...
    }

    public String getCharSet() {
        ParameterParser parser = new ParameterParser();
        parser.setLowerCaseNames(true);
        // Parameter parser can handle null input
        Map<String, String> params = parser.parse(getContentType(), ';'); // call to a; calls b
        return params.get("charset"); // NullPointerException here
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
        assertEquals(item.getSize(), testFieldValueBytes.length);
        assertTrue(Arrays.equals(item.get(), testFieldValueBytes));
        assertEquals(item.getString(), textFieldValue); // calls a and b
    }
}
