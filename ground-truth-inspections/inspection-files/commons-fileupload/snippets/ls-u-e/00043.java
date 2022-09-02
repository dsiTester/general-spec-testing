public interface FileItem {
    ...
    String getFieldName(); // a
    ...
    boolean isFormField(); // b
}

public class DiskFileItem implements FileItem {

    @Override
    public String getFieldName() { // only implementation of a
        return fieldName;
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
        assertEquals(item.getFieldName(), textFieldName); // call to a
        ...
        assertTrue(item.isFormField()); // call to b
        ...
    }

    @Test
    public void testFileFieldConstruction() {
        FileItemFactory factory = createFactory(null);
        String fileFieldName = "fileField";
        String fileName = "originalFileName"; // replacement value for a
        FileItem item = factory.createItem(
                fileFieldName,
                fileContentType,
                false,
                fileName
        );
        ...
        assertEquals(item.getFieldName(), fileFieldName); // call to a
        ...
        assertFalse(item.isFormField()); // call to b
        ...
    }

}

