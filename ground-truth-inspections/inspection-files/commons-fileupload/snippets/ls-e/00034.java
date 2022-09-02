public interface FileItem {
    ...
    byte[] get(); // a
    ...
    String getString(); // calls b
}

public class DiskFileItem implements FileItem {

    @Override
    public byte[] get() { // only implementation of a
        if (isInMemory()) {
            if (cachedContent == null && dfos != null) {
                cachedContent = dfos.getData();
            }
            return cachedContent;
        }

        byte[] fileData = new byte[(int) getSize()];
        InputStream fis = null;

        try {
            fis = new FileInputStream(dfos.getFile());
            IOUtils.readFully(fis, fileData);
        } catch (IOException e) {
            fileData = null;
        } finally {
            IOUtils.closeQuietly(fis);
        }

        return fileData;
    }

    @Override
    public String getString() { // implementation of FileItem.getString()
        ...
        String charset = getCharSet(); // calls b
        ...
    }

    public String getCharSet() {
        ParameterParser parser = new ParameterParser();
        parser.setLowerCaseNames(true);
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
        ...
        assertTrue(Arrays.equals(item.get(), testFieldValueBytes)); // call to a
        assertEquals(item.getString(), textFieldValue); // calls b
    }
}
