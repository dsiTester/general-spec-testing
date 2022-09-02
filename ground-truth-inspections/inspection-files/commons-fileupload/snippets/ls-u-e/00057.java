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

    public File getStoreLocation() { // definition of b
        if (dfos == null) { // this would be the case if a was not called
            return null;
        }
        if (isInMemory()) { // either of these two calls will succeed if a was called
            return null;
        }
        return dfos.getFile();
    }

    @Override
    public void delete() { // calls b
        ...
        File outputFile = getStoreLocation(); // call to b
        ...
    }
}

public class StreamingTest {

    public void testFileUploadException()
            throws IOException, FileUploadException { // invalidated case
        byte[] request = newRequest();
        byte[] invalidRequest = new byte[request.length-11];
        System.arraycopy(request, 0, invalidRequest, 0, request.length-11);
        try {
            parseUpload(invalidRequest); // calls a and b
            fail("Expected EndOfStreamException");
        } catch (IOFileUploadException e) {
            assertTrue(e.getCause() instanceof MultipartStream.MalformedStreamException);
        }
    }

}

public class FileUploadBase {

    public List<FileItem> parseRequest(RequestContext ctx)
            throws FileUploadException { // transitively gets called by StreamingTest.parseUpload
        ...
        try {
            ...
            while (iter.hasNext()) {
                ...
                try {
                    Streams.copy(item.openStream(), fileItem.getOutputStream(), true, buffer); // call to a
                }
                ...
            }
            ...
        } ...
        finally {
            if (!successful) {
                for (FileItem fileItem : items) {
                    try {
                        fileItem.delete(); // calls b
                    } ...
                }
            }
        }
    }

}

public class DiskFileItemSerializeTest {

    @Test
    public void testAboveThreshold() { // unknown case
        // Create the FileItem
        byte[] testFieldValueBytes = createContentBytes(threshold + 1);
        FileItem item = createFileItem(testFieldValueBytes); // calls a
        ...
        item.delete(); // calls b
    }

    private FileItem createFileItem(byte[] contentBytes) { // calls a
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
            OutputStream os = item.getOutputStream(); // call to a
            os.write(contentBytes); // NullPointerException here
            os.close();
        }
        ...
    }


}
