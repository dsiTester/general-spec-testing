public interface FileItem {
    ...
    String getFieldName(); // a
    ...
    byte[] get(); // b
    ...
}

public class DiskFileItem implements FileItem {

    @Override
    public String getFieldName() { // only implementation of a
        return fieldName;
    }

    @Override
    public byte[] get() { // only implementation of b
        if (isInMemory()) {
            if (cachedContent == null && dfos != null) {
                cachedContent = dfos.getData();
            }
            return cachedContent;
        }

        byte[] fileData = new byte[(int) getSize()];
        InputStream fis = null;

        try {
            fis = new FileInputStream(dfos.getFile());
            IOUtils.readFully(fis, fileData);
        } catch (IOException e) {
            fileData = null;
        } finally {
            IOUtils.closeQuietly(fis);
        }

        return fileData;
    }

}

public class SizesTest {

    @Test
    public void testFileUpload()
            throws IOException, FileUploadException {
        ...
        List<FileItem> fileItems =
                Util.parseUpload(new ServletFileUpload(new DiskFileItemFactory()), baos.toByteArray());
        Iterator<FileItem> fileIter = fileItems.iterator();
        ...
        for (int i = 0;  i < 16384;  i += add) {
            ...
            FileItem item = fileIter.next();
            assertEquals("field" + (num++), item.getFieldName()); // call to a - fails here
            byte[] bytes = item.get(); // call to b
            ...
        }
        assertTrue(!fileIter.hasNext());
    }

}
