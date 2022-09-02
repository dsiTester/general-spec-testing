public class FileUploadBase {

    public FileItemIterator getItemIterator(RequestContext ctx)
        throws FileUploadException, IOException { // definition of a
        try {
            return new FileItemIteratorImpl(ctx);
        } catch (FileUploadIOException e) {
            // unwrap encapsulated SizeException
            throw (FileUploadException) e.getCause();
        }
    }

    public abstract FileItemFactory getFileItemFactory(); // b

    public List<FileItem> parseRequest(RequestContext ctx)
            throws FileUploadException { // called from Util.parseUpload
        List<FileItem> items = new ArrayList<FileItem>();
        boolean successful = false;
        try {
            FileItemIterator iter = getItemIterator(ctx); // call to a
            FileItemFactory fac = getFileItemFactory(); // call to b
            ...
            while (iter.hasNext()) { // NullPointerException here
                ...
            }
        }
        ...
    }

}

public class FileUpload extends FileUploadBase {
    @Override
    public FileItemFactory getFileItemFactory() { // used implementation of b
        return fileItemFactory;
    }

}

public class SizesTest {
    @Test
    public void testFileUpload()
            throws IOException, FileUploadException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ...
        List<FileItem> fileItems =
                Util.parseUpload(new ServletFileUpload(new DiskFileItemFactory()), baos.toByteArray()); // calls a and b
        Iterator<FileItem> fileIter = fileItems.iterator();
        ...
        for (int i = 0;  i < 16384;  i += add) {
            if (++add == 32) {
                add = 16;
            }
            FileItem item = fileIter.next();
            assertEquals("field" + (num++), item.getFieldName());
            byte[] bytes = item.get();
            assertEquals(i, bytes.length);
            for (int j = 0;  j < i;  j++) {
                assertEquals((byte) j, bytes[j]);
            }
        }
        assertTrue(!fileIter.hasNext());
    }
}

public class Util {
    public static List<FileItem> parseUpload(FileUpload upload, byte[] bytes) throws FileUploadException { // called from test
        return parseUpload(upload, bytes, Constants.CONTENT_TYPE); // calls a and b
    }

    public static List<FileItem> parseUpload(FileUpload upload, byte[] bytes, String contentType) throws FileUploadException { // called from above
        final HttpServletRequest request = new MockHttpServletRequest(bytes, contentType);
        List<FileItem> fileItems = upload.parseRequest(new ServletRequestContext(request)); // calls a and b
        return fileItems;
    }
}
