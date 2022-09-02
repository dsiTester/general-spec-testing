public interface FileItem {
    ...
    String getFieldName(); // a
    ...
    String getName(); // b
}

public class DiskFileItem implements FileItem {

    @Override
    public String getFieldName() { // only implementation of a
        return fieldName;
    }

    @Override
    public String getName() { // only implementation of b
        return Streams.checkFileName(fileName);
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
        assertNull(item.getName()); // call to b
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
        assertEquals(item.getName(), fileName); // call to b
    }

}

