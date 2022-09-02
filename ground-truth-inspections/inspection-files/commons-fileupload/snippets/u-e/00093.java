public interface FileItemFactory {

    FileItem createItem(
            String fieldName,
            String contentType,
            boolean isFormField,
            String fileName
            ); // a

}

public class DiskFileItemFactory implements FileItemFactory {

    public int getSizeThreshold() { // definition of b
        return sizeThreshold;
    }

}

public class DefaultFileItemFactory extends DiskFileItemFactory {

    @Override
    @Deprecated
    public FileItem createItem( // used implementation of a
            String fieldName,
            String contentType,
            boolean isFormField,
            String fileName
            ) {
        return new DefaultFileItem(fieldName, contentType,
                isFormField, fileName, getSizeThreshold(), getRepository()); // call to b
    }

}


public class DefaultFileItemTest {
    @Test
    public void testBelowThreshold() {
        FileItemFactory factory = createFactory(null);
        String textFieldName = "textField";
        String textFieldValue = "0123456789";
        byte[] testFieldValueBytes = textFieldValue.getBytes();

        FileItem item = factory.createItem( // call to a (and calls b)
                textFieldName,
                textContentType,
                true,
                null
        );
        assertNotNull(item); // fails here
        ...
    }
}
