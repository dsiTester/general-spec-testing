public interface FileItem {
    ...
    byte[] get(); // a
    ...
    String getString(); // calls b
}

public class DiskFileItem implements FileItem {

    @Override
    public String getString() { // implementation of FileItem.getString()
        byte[] rawdata = get(); // call to b
        ...
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
