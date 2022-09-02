public class DiskFileItem {
    @Override
    public String getContentType() { // definition of a
        return contentType;
    }

    @Override
    public String getName() { // definition of b
        return Streams.checkFileName(fileName);
    }
}


public class DefaultFileItemTest {
    @Test
    public void testFileFieldConstruction() {
        FileItemFactory factory = createFactory(null);
        String fileFieldName = "fileField";
        String fileName = "originalFileName";

        FileItem item = factory.createItem(
                fileFieldName,
                fileContentType,
                false,
                fileName
        );
        ...
        assertEquals(item.getContentType(), fileContentType); // call to a
        ...
        assertEquals(item.getName(), fileName); // call to b
    }

}
