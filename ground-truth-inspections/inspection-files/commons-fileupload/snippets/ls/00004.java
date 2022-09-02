public class DiskFileItemFactory implements FileItemFactory {

    public void setSizeThreshold(int sizeThreshold) { // definition of a
        this.sizeThreshold = sizeThreshold;
    }

    public File getRepository() { // definition of b
        return repository;
    }
}

@Deprecated
public class DiskFileUpload
    extends FileUploadBase {

    @Deprecated
    public void setSizeThreshold(int sizeThreshold) { // called from test
        fileItemFactory.setSizeThreshold(sizeThreshold);
    }
}

public class DefaultFileItemFactory {
    @Override
    @Deprecated
    public FileItem createItem(
            String fieldName,
            String contentType,
            boolean isFormField,
            String fileName
            ) { // calls b
        return new DefaultFileItem(fieldName, contentType,
                isFormField, fileName, getSizeThreshold(), getRepository()); // getRepository() is b
    }
}

public class FileUploadBase {
    ...
    public List<FileItem> parseRequest(RequestContext ctx)
        throws FileUploadException {
        ...
        FileItem fileItem = fac.createItem(item.getFieldName(), item.getContentType(), // calls DefaultFileItemFactory.createItem()
                                           item.isFormField(), fileName);
        ...
            }
}

public class DiskFileUploadTest {
    @Test
    public void testMoveFile() throws Exception {
        DiskFileUpload myUpload = new DiskFileUpload();
        myUpload.setSizeThreshold(0); // calls a - comment out for invalidating test
        ...
        final List<FileItem> items = myUpload.parseRequest(request); // calls b
        assertNotNull(items);
        assertFalse(items.isEmpty());
        ...
    }

}
