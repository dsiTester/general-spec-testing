public interface FileItem {
    String getFieldName(); // a
}

public class DiskFileItem implements FileItem {

    @Override
    public String getFieldName() { // only implementation of a
        return fieldName;
    }

    @Override
    public boolean isInMemory() { // definition of b
        if (cachedContent != null) {
            return true;
        }
        return dfos.isInMemory();
    }

    @Override
    public byte[] get() { // calls b
        if (isInMemory()) { // call to b
            ...
        }
        ...
    }

}

public class StreamingTest {

    public void testFILEUPLOAD135()
            throws IOException, FileUploadException {
        byte[] request = newShortRequest();
        final ByteArrayInputStream bais = new ByteArrayInputStream(request);
        List<FileItem> fileItems = parseUpload(new InputStream() {
            ...
        }, request.length);
        Iterator<FileItem> fileIter = fileItems.iterator();
        assertTrue(fileIter.hasNext());
        FileItem item = fileIter.next();
        assertEquals("field", item.getFieldName()); // call to a
        byte[] bytes = item.get(); // calls b
        ...
    }

}
