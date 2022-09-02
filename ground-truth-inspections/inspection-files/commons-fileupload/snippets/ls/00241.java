public class ParameterParser {

    public void setLowerCaseNames(boolean b) { // definition of a
        this.lowerCaseNames = b;
    }

    private String parseQuotedToken(final char[] terminators) { // definition of b
        char ch;
        i1 = pos;
        i2 = pos;
        boolean quoted = false;
        boolean charEscaped = false;
        while (hasChar()) {
            ch = chars[pos];
            if (!quoted && isOneOf(ch, terminators)) {
                break;
            }
            if (!charEscaped && ch == '"') {
                quoted = !quoted;
            }
            charEscaped = (!charEscaped && ch == '\\');
            i2++;
            pos++;

        }
        return getToken(true);
    }

    public Map<String, String> parse(final String str, char separator) { // called from ParameterParserTest/DiskFileItem.getCharSet()
        if (str == null) {
            return new HashMap<String, String>();
        }
        return parse(str.toCharArray(), separator); // calls b
    }

    public Map<String, String> parse(final char[] charArray, char separator) {
        if (charArray == null) {
            return new HashMap<String, String>();
        }
        return parse(charArray, 0, charArray.length, separator); // calls b
    }

    public Map<String, String> parse(
                                     final char[] charArray,
                                     int offset,
                                     int length,
                                     char separator) {

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
            if (hasChar() && (charArray[pos] == '=')) {
                pos++; // skip '='
                paramValue = parseQuotedToken(new char[] { // call to b
                        separator });
                // NOTE: state probably gets restored here
                ...
            }
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

public class ParameterParserTest {
    @Test
    public void testContentTypeParsing() {
        String s = "text/plain; Charset=UTF-8";
        ParameterParser parser = new ParameterParser();
        parser.setLowerCaseNames(true); // call to a
        Map<String, String> params = parser.parse(s, ';'); // calls b
        assertEquals("UTF-8", params.get("charset"));
    }
}
