public class DefaultFileItemFactory {

    @Override
    @Deprecated
    public FileItem createItem(String fieldName, String contentType, boolean isFormField, String fileName) {
        return new DefaultFileItem(fieldName, contentType,
                                   isFormField, fileName, getSizeThreshold(), getRepository()); // a and b are called here
    }

    // definition of a
    public int getSizeThreshold() {
        return sizeThreshold;
    }

    // definition of b
    public File getRepository() {
        return repository;
    }

}

public class DefaultFileItemTest {

    public void doTestAboveThreshold(File repository) { // called from invalidating test
        FileItemFactory factory = createFactory(repository); // initialize DefaultFileItemFactory
        ...
        FileItem item = factory.createItem(textFieldName, textContentType, true, null); // calls a and b
        assertNotNull(item);
        ...
    }

    @Test
    public void testAboveThresholdDefaultRepository() { // invalidating test
        doTestAboveThreshold(null);
    }

    @Test
    public void testBelowThreshold() { // validating test
        FileItemFactory factory = createFactory(null);
        String textFieldName = "textField";
        String textFieldValue = "0123456789"; // writes 10 characters? Here
        byte[] testFieldValueBytes = textFieldValue.getBytes();

        FileItem item = factory.createItem(textFieldName, textContentType, true, null); // calls a and b
        assertNotNull(item);

        try {
            OutputStream os = item.getOutputStream();
            os.write(testFieldValueBytes); // later, there will be a check for whether the amount written exceeds the threshold.
            os.close();
        }
        ...
        assertTrue(item.isInMemory()); // fails here for validating test, because the amount written is greater than the new threshold (the default value 0)
        ...
    }
}
