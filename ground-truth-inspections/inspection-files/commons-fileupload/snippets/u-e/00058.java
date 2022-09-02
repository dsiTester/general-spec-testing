public interface FileItem {

    OutputStream getOutputStream() throws IOException; // a

}

public class DiskFileItem implements FileItem {

    @Override
    public OutputStream getOutputStream()
        throws IOException { // only implementation of a
        if (dfos == null) {
            File outputFile = getTempFile(); // call to b
            dfos = new DeferredFileOutputStream(sizeThreshold, outputFile);
        }
        return dfos;
    }

    protected File getTempFile() { // definition of b
        if (tempFile == null) {
            File tempDir = repository;
            if (tempDir == null) {
                tempDir = new File(System.getProperty("java.io.tmpdir"));
            }

            String tempFileName = format("upload_%s_%s.tmp", UID, getUniqueId());

            tempFile = new File(tempDir, tempFileName);
        }
        return tempFile;
    }

}

public class DiskFileItemSerializeTest {

    @Test
    public void testAboveThreshold() { // unknown case
        // Create the FileItem
        byte[] testFieldValueBytes = createContentBytes(threshold + 1);
        FileItem item = createFileItem(testFieldValueBytes); // calls a and b
    }

    private FileItem createFileItem(byte[] contentBytes) { // calls a and b
        return createFileItem(contentBytes, REPO);
    }

    private FileItem createFileItem(byte[] contentBytes, File repository) {
        FileItemFactory factory = new DiskFileItemFactory(threshold, repository);
        String textFieldName = "textField";

        FileItem item = factory.createItem(
                textFieldName,
                textContentType,
                true,
                "My File Name"
        );
        try {
            OutputStream os = item.getOutputStream(); // call to a (and calls b)
            os.write(contentBytes); // NullPointerException here
            os.close();
        }
        ...
    }


}
