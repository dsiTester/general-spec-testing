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

    @Override
    public String getContentType() { // definition of b
        return contentType;
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

}
