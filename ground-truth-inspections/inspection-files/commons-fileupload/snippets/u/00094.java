public interface FileItemFactory {
    FileItem createItem(
            String fieldName,
            String contentType,
            boolean isFormField,
            String fileName
            ); // a
}

public class DiskFileItemFactory {

    public FileCleaningTracker getFileCleaningTracker() { // definition of b
        return fileCleaningTracker;
    }


    @Override
    public FileItem createItem(String fieldName, String contentType,
            boolean isFormField, String fileName) { // used implementation of a, calls b
        ...
        FileCleaningTracker tracker = getFileCleaningTracker(); // call to b
        if (tracker != null) {
            tracker.track(result.getTempFile(), result);
        }
        return result;
    }

}

public class DiskFileItemSerializeTest {

@Test
    public void testAboveThreshold() {
        // Create the FileItem
        byte[] testFieldValueBytes = createContentBytes(threshold + 1);
        FileItem item = createFileItem(testFieldValueBytes); // calls a
        ...
    }

    private FileItem createFileItem(byte[] contentBytes) {
        return createFileItem(contentBytes, REPO); // calls a
    }

    private FileItem createFileItem(byte[] contentBytes, File repository) {
        ...
        FileItem item = factory.createItem(
                textFieldName,
                textContentType,
                true,
                "My File Name"
        ); // call to a
        try {
            OutputStream os = item.getOutputStream(); // NullPointerException here
            os.write(contentBytes);
            os.close();
        } catch(IOException e) {
            fail("Unexpected IOException" + e);
        }
        return item;
    }

}
