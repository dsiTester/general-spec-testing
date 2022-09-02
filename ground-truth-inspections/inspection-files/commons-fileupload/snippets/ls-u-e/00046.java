public interface FileItem {
    OutputStream getOutputStream() throws IOException; // a
    ...
    void delete(); // b
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

    @Override
    public void delete() { // only implementation of b
        cachedContent = null;
        File outputFile = getStoreLocation();
        if (outputFile != null && !isInMemory() && outputFile.exists()) {
            outputFile.delete();
        }
    }
}

public class StreamingTest {

    public void testFileUploadException()
            throws IOException, FileUploadException { // invalidated test
        ...
        try {
            parseUpload(invalidRequest); // calls a and b
            fail("Expected EndOfStreamException");
        } catch (IOFileUploadException e) {
            assertTrue(e.getCause() instanceof MultipartStream.MalformedStreamException);
        }
    }

    private List<FileItem> parseUpload(byte[] bytes) throws FileUploadException {
        return parseUpload(new ByteArrayInputStream(bytes), bytes.length); // calls a and b
    }

    private List<FileItem> parseUpload(InputStream pStream, int pLength)
            throws FileUploadException {
        ...
        List<FileItem> fileItems = upload.parseRequest(new ServletRequestContext(request)); // calls a and b - refer to FileUploadBase.parseRequest below
        return fileItems;
    }

}

public class DiskFileItemSerializeTest {

    @Test
    public void testValidRepository() { // unknown test
        // Create the FileItem
        byte[] testFieldValueBytes = createContentBytes(threshold);
        testInMemoryObject(testFieldValueBytes, REPO);
    }

    public void testInMemoryObject(byte[] testFieldValueBytes, File repository) {
        FileItem item = createFileItem(testFieldValueBytes, repository); // calls a
        ...
        item.delete(); // call to b
    }

    private FileItem createFileItem(byte[] contentBytes, File repository) { // unknown case
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
            os.write(contentBytes); // exception thrown here
            os.close();
        } catch(IOException e) {
            fail("Unexpected IOException" + e);
        }

        return item;

    }

}

public class FileUploadBase {

    public List<FileItem> parseRequest(RequestContext ctx)
        throws FileUploadException {
        List<FileItem> items = new ArrayList<FileItem>();
        boolean successful = false;
        try {
            ...
            while (iter.hasNext()) {
                ...
                try {
                    Streams.copy(item.openStream(), fileItem.getOutputStream(), true, buffer); // call to a
                } catch (FileUploadIOException e) {
                    throw (FileUploadException) e.getCause();
                } catch (IOException e) {
                    throw new IOFileUploadException(format("Processing of %s request failed. %s",
                                                           MULTIPART_FORM_DATA, e.getMessage()), e); // this exception gets thrown
                }
                ...
            }
            ...
        }
        ...
        finally {
            if (!successful) {
                for (FileItem fileItem : items) {
                    try {
                        fileItem.delete(); // call to b
                    } catch (Exception ignored) {
                        // ignored TODO perhaps add to tracker delete failure list somehow?
                    }
                }
            }
        }
    }

}
