public interface FileItem {
    String getString(); // a
}

public class DiskFileItem implements FileItem {

    @Override
    public String getString() { // implementation of a
        byte[] rawdata = get();
        String charset = getCharSet();  // calls b
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
        item.delete();
    }

}
