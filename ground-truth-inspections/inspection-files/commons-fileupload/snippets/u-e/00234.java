public class ParameterParser {

    public Map<String, String> parse(
                                     final char[] charArray,
                                     int offset,
                                     int length,
                                     char separator) { // definition of a

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
                    '=', separator }); // call to b
            paramValue = null;
            if (hasChar() && (charArray[pos] == '=')) {
                pos++; // skip '='
                paramValue = parseQuotedToken(new char[] {
                        separator });

                if (paramValue != null) {
                    try {
                        paramValue = MimeUtility.decodeText(paramValue);
                    } catch (UnsupportedEncodingException e) {
                        // let's keep the original value in this case
                    }
                }
            }
            if (hasChar() && (charArray[pos] == separator)) {
                pos++; // skip separator
            }
            if ((paramName != null) && (paramName.length() > 0)) {
                if (this.lowerCaseNames) {
                    paramName = paramName.toLowerCase(Locale.ENGLISH);
                }

                params.put(paramName, paramValue);
            }
        }
        return params;
    }

    private String parseToken(final char[] terminators) { // definition of b
        char ch;
        i1 = pos;
        i2 = pos;
        while (hasChar()) {
            ch = chars[pos];
            if (isOneOf(ch, terminators)) {
                break;
            }
            i2++;
            pos++;
        }
        return getToken(false);
    }

    public Map<String, String> parse(final String str, char separator) { // called from DiskFileItem.getCharSet()
        if (str == null) {
            return new HashMap<String, String>();
        }
        return parse(str.toCharArray(), separator); // calls a and b
    }

    public Map<String, String> parse(final char[] charArray, char separator) {
        if (charArray == null) {
            return new HashMap<String, String>();
        }
        return parse(charArray, 0, charArray.length, separator); // calls a and b
    }

}

public class DiskFileItem {

    @Override
    public String getString() { // called from test
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
        parser.setLowerCaseNames(true);
        // Parameter parser can handle null input
        Map<String, String> params = parser.parse(getContentType(), ';'); // calls a and b
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
