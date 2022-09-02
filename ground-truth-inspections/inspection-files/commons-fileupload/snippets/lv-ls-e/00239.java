public class ParameterParser {

    public void setLowerCaseNames(boolean b) { // definition of a
        this.lowerCaseNames = b;
    }

    public Map<String, String> parse(final char[] charArray, char separator) { // definition of b
        if (charArray == null) {
            return new HashMap<String, String>();
        }
        return parse(charArray, 0, charArray.length, separator);
    }

    public Map<String, String> parse(final String str, char separator) { // called from ParameterParserTest/DiskFileItem.getCharSet()
        if (str == null) {
            return new HashMap<String, String>();
        }
        return parse(str.toCharArray(), separator); // call to b
    }

    public Map<String, String> parse(
                                     final char[] charArray,
                                     int offset,
                                     int length,
                                     char separator) { // called from b

        if (charArray == null) {
            return new HashMap<String, String>();
        }
        HashMap<String, String> params = new HashMap<String, String>();
        this.chars = charArray;
        this.pos = offset;
        this.len = length;

        String paramName = null;
        String paramValue = null;
        while (hasChar()) {
            paramName = parseToken(new char[] {
                    '=', separator });
            paramValue = null;
            ...
            if ((paramName != null) && (paramName.length() > 0)) {
                if (this.lowerCaseNames) { // calling method-a before method-b is important because of this conditional
                    paramName = paramName.toLowerCase(Locale.ENGLISH);
                }
                params.put(paramName, paramValue);
            }
        }
        return params;
    }
}

public class DiskFileItem {

    @Override
    public String getString() { // called from DefaultFileItemTest
        ...
        String charset = getCharSet(); // calls a and b
        if (charset == null) {
            charset = defaultCharset;
        }
        try {
            return new String(rawdata, charset);
        } catch (UnsupportedEncodingException e) {
            return new String(rawdata);
        }
    }

    public String getCharSet() {
        ParameterParser parser = new ParameterParser();
        parser.setLowerCaseNames(true); // call to a
        // Parameter parser can handle null input
        Map<String, String> params = parser.parse(getContentType(), ';'); // calls b
        return params.get("charset");
    }

}

public class ParameterParserTest {
    @Test
    public void testContentTypeParsing() { // validated test
        String s = "text/plain; Charset=UTF-8";
        ParameterParser parser = new ParameterParser();
        parser.setLowerCaseNames(true); // call to a
        Map<String, String> params = parser.parse(s, ';'); // calls b
        assertEquals("UTF-8", params.get("charset")); // assertion fails here
    }
}

public class DefaultFileItemTest {
    @Test
    public void testBelowThreshold() { // invalidated test
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
