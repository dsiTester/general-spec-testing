public interface FileItem {
    String getContentType(); // a
}

public class DiskFileItem implements FileItem {

    @Override
    public String getContentType() { // only implementation of a
        return contentType;
    }

    @Override
    public boolean isFormField() { // only implementation of b
        return isFormField;
    }

}

public class DefaultFileItemTest {

    @Test
    public void testTextFieldConstruction() {
        FileItemFactory factory = createFactory(null);
        String textFieldName = "textField"; // replacement value for a
        FileItem item = factory.createItem(
                textFieldName,
                textContentType,
                true,
                null
        );
        ...
        assertEquals(item.getContentType(), textContentType); // call to a
        assertTrue(item.isFormField()); // call to b
        ...
    }

}

