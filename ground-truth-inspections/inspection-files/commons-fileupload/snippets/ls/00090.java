public interface FileItemHeadersSupport { // FileItem extends this interface
    void setHeaders(FileItemHeaders headers); // a
}

public class DiskFileItem implements FileItem {

    @Override
    public void setHeaders(FileItemHeaders pHeaders) { // implementation of a that is invoked
        headers = pHeaders;
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

public class SizesTest {

    @Test
    public void testFileUpload()
            throws IOException, FileUploadException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ...
        List<FileItem> fileItems =
                Util.parseUpload(new ServletFileUpload(new DiskFileItemFactory()), baos.toByteArray()); // calls a
        Iterator<FileItem> fileIter = fileItems.iterator();
        ...
        for (int i = 0;  i < 16384;  i += add) {
            ...
            FileItem item = fileIter.next();
            byte[] bytes = item.get(); // calls b
            ...
        }
        assertTrue(!fileIter.hasNext());
    }
}

public class Util {

    public static List<FileItem> parseUpload(FileUpload upload, byte[] bytes) throws FileUploadException {
        return parseUpload(upload, bytes, Constants.CONTENT_TYPE); // calls a
    }

    public static List<FileItem> parseUpload(FileUpload upload, byte[] bytes, String contentType) throws FileUploadException {
        final HttpServletRequest request = new MockHttpServletRequest(bytes, contentType);
        List<FileItem> fileItems = upload.parseRequest(new ServletRequestContext(request)); // calls a
        return fileItems;
    }

}

public class FileUploadBase {

    public List<FileItem> parseRequest(RequestContext ctx)
            throws FileUploadException {
        List<FileItem> items = new ArrayList<FileItem>();
        ...
        try {
            FileItemIterator iter = getItemIterator(ctx);
            FileItemFactory fac = getFileItemFactory();
            ...
            while (iter.hasNext()) {
                final FileItemStream item = iter.next();
                // Don't use getName() here to prevent an InvalidFileNameException.
                final String fileName = ((FileItemIteratorImpl.FileItemStreamImpl) item).name;
                FileItem fileItem = fac.createItem(item.getFieldName(), item.getContentType(),
                                                   item.isFormField(), fileName);
                ...
                final FileItemHeaders fih = item.getHeaders();
                fileItem.setHeaders(fih); // call to a
            }
            ...
            return items;
        }
        ...
    }
}
