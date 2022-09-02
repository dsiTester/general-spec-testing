public interface FileItem {
    ...
    byte[] get(); // a
    ...
    String getString(); // calls b
}

public class DiskFileItem implements FileItem {

    @Override
    public String getString() { // implementation of FileItem.getString()
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

    public void doTestAboveThreshold(File repository) {
        String textFieldValue = "01234567890123456789";
        byte[] testFieldValueBytes = textFieldValue.getBytes(); // replacement value for a
        FileItem item = factory.createItem(
                textFieldName,
                textContentType,
                true,
                null
        );
        ...
        assertTrue(Arrays.equals(item.get(), testFieldValueBytes)); // call to a
        assertEquals(item.getString(), textFieldValue); // calls b
    }

}
