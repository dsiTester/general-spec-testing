public interface FileItem {

    OutputStream getOutputStream() throws IOException; // a

}

public class DiskFileItem implements FileItem {


    @Override
    public OutputStream getOutputStream()
        throws IOException { // only implementation of a
        if (dfos == null) {
            File outputFile = getTempFile();
            dfos = new DeferredFileOutputStream(sizeThreshold, outputFile);
        }
        return dfos;
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
        ...
        FileItem item = factory.createItem(
                textFieldName,
                textContentType,
                true,
                null
        );
        ...
        try {
            OutputStream os = item.getOutputStream(); // call to a
            os.write(testFieldValueBytes); // NullPointerException here
            os.close();
        } catch(IOException e) {
            fail("Unexpected IOException");
        }
        ...
        assertEquals(item.getString(), textFieldValue); // calls b
    }

    @Test
    public void modifiedTestBelowThreshold() { // modified for the sake of demonstration that the test would pass if you call method-a after method-b.
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
            OutputStream os = new ByteArrayOutputStream(); // item.getOutputStream();
            os.write(testFieldValueBytes);
            os.close();
        } catch(IOException e) {
            fail("Unexpected IOException");
        }
//        assertTrue(item.isInMemory());
//        assertEquals(item.getSize(), testFieldValueBytes.length);
//        assertTrue(Arrays.equals(item.get(), testFieldValueBytes));
//        assertEquals(item.getString(), textFieldValue);
        DefaultFileItem i = (DefaultFileItem) item;
        System.out.println(i.getCharSet()); // modified call to b
        try {
            item.getOutputStream(); // modified call to a
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
