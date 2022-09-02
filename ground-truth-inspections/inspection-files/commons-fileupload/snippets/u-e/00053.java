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

    @Override
    public byte[] get() { // definition of b
        if (isInMemory()) { // NullPointerException thrown here
            ...
        }
        ...
    }

    @Override
    public boolean isInMemory() {
        if (cachedContent != null) {
            return true;
        }
        return dfos.isInMemory(); // precise point in code at which NullPointerException was thrown
    }

}

public class FileUploadBase {


    public List<FileItem> parseRequest(RequestContext ctx)
            throws FileUploadException { // calls a
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
            successful = true;
            return items;
        }
        ...
    }

}

public class Util {

    public static List<FileItem> parseUpload(FileUpload upload, byte[] bytes) throws FileUploadException { // called by SizesTest - calls a
        return parseUpload(upload, bytes, Constants.CONTENT_TYPE);
    }

    public static List<FileItem> parseUpload(FileUpload upload, byte[] bytes, String contentType) throws FileUploadException { // calls a
        final HttpServletRequest request = new MockHttpServletRequest(bytes, contentType);
        List<FileItem> fileItems = upload.parseRequest(new ServletRequestContext(request));
        return fileItems;
    }

}

public class SizesTest {

    @Test
    public void testFileUpload()
            throws IOException, FileUploadException { // validating case
        ByteArrayOutputStream baos = new ByteArrayOutputStream(); // possible replacement value for the return value of method-a
        ...
        List<FileItem> fileItems =
                Util.parseUpload(new ServletFileUpload(new DiskFileItemFactory()), baos.toByteArray()); // calls a
        ...
        for (int i = 0;  i < 16384;  i += add) {
            byte[] bytes = item.get(); // call to b - also NullPointerException here
            ...
        }
        ...
    }

}

public class DefaultFileItemTest { // unknown case

    @Test
    public void testBelowThreshold() {
        FileItemFactory factory = createFactory(null);
        String textFieldName = "textField";
        String textFieldValue = "0123456789";
        byte[] testFieldValueBytes = textFieldValue.getBytes();

        FileItem item = factory.createItem(
                textFieldName,
                textContentType,
                true,
                null
        );
        assertNotNull(item);

        try {
            OutputStream os = item.getOutputStream(); // call to a
            os.write(testFieldValueBytes); // NullPointerException here
            os.close();
        } catch(IOException e) {
            fail("Unexpected IOException");
        }
        ...
        assertTrue(Arrays.equals(item.get(), testFieldValueBytes)); // call to b
        ...
    }

}
