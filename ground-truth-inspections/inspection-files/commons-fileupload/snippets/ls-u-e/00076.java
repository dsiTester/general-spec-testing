public interface FileItem {
    boolean isFormField(); // a

    String getName(); // b
}

public class DiskFileItem implements FileItem {

    @Override
    public boolean isFormField() { // only implementation of a
        return isFormField;
    }

    @Override
    public String getName() { // only implementation of b
        return Streams.checkFileName(fileName);
    }

}

public class DefaultFileItemTest {

    @Test
    public void testFileFieldConstruction() { // invalidated case
        FileItemFactory factory = createFactory(null);
        String fileFieldName = "fileField";
        String fileName = "originalFileName";

        FileItem item = factory.createItem(
                fileFieldName,
                fileContentType,
                false,
                fileName
        );
        assertNotNull(item);
        assertEquals(item.getFieldName(), fileFieldName);
        assertEquals(item.getContentType(), fileContentType);
        assertFalse(item.isFormField()); // call to a
        assertEquals(item.getName(), fileName); // call to b
    }

    @Test
    public void testTextFieldConstruction() { // unknown case
        FileItemFactory factory = createFactory(null);
        String textFieldName = "textField";

        FileItem item = factory.createItem(
                textFieldName,
                textContentType,
                true,
                null
        );
        assertNotNull(item);
        assertEquals(item.getFieldName(), textFieldName);
        assertEquals(item.getContentType(), textContentType);
        assertTrue(item.isFormField()); // call to a - assertion fails here as well
        assertNull(item.getName()); // call to b
    }

}
