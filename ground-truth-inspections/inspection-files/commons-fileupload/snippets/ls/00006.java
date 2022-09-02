public class DiskFileItemFactory {
    private int sizeThreshold = DEFAULT_SIZE_THRESHOLD; // this is overwritten by the constructor

    // constructor
    public DiskFileItemFactory() {
        this(DEFAULT_SIZE_THRESHOLD, null);
    }

    // constructor - sets sizeThreshold, so one **could** call getSizeThreshold() without calling setSizeThreshold()...
    public DiskFileItemFactory(int sizeThreshold, File repository) {
        this.sizeThreshold = sizeThreshold;
        this.repository = repository;
    }

    public void setSizeThreshold(int sizeThreshold) { // a
        this.sizeThreshold = sizeThreshold;
    }

}

public class DefaultFileItemFactory {

    @Override
    @Deprecated
    public FileItem createItem( // b
            String fieldName,
            String contentType,
            boolean isFormField,
            String fileName
            ) {
        return new DefaultFileItem(fieldName, contentType,
                isFormField, fileName, getSizeThreshold(), getRepository());
    }

}


public class DiskFileUpload {
    @Deprecated
    public void setSizeThreshold(int sizeThreshold) { 
        fileItemFactory.setSizeThreshold(sizeThreshold); // calls a
    }
}

public class DiskFileUploadTest {
    @Test
    public void testMoveFile() throws Exception {
        DiskFileUpload myUpload = new DiskFileUpload();
        myUpload.setSizeThreshold(0); // calls DiskFileUpload.setSizeThreshold(), which calls a
    	final String content = 
            "-----1234\r\n" +
            "Content-Disposition: form-data; name=\"file\";"
            + "filename=\"foo.tab\"\r\n" +
            "Content-Type: text/whatever\r\n" +
            "\r\n" +
            "This is the content of the file\n" +
            "\r\n" +
            "-----1234--\r\n";
    	final byte[] contentBytes = content.getBytes("US-ASCII");
        final HttpServletRequest request = new MockHttpServletRequest(contentBytes, Constants.CONTENT_TYPE);
        final List<FileItem> items = myUpload.parseRequest(request); // calls b
        assertNotNull(items);
        assertFalse(items.isEmpty());
        final DiskFileItem dfi = (DiskFileItem) items.get(0);
        final File out = File.createTempFile("install", ".tmp");
        dfi.write(out);
    }
}

public class FileUploadBase {
    public List<FileItem> parseRequest(RequestContext ctx)
            throws FileUploadException {
        List<FileItem> items = new ArrayList<FileItem>();
        boolean successful = false;
        try {
            FileItemIterator iter = getItemIterator(ctx);
            while (iter.hasNext()) {
                final FileItemStream item = iter.next();
                // Don't use getName() here to prevent an InvalidFileNameException.
                final String fileName = ((FileItemIteratorImpl.FileItemStreamImpl) item).name;
                FileItem fileItem = fac.createItem(item.getFieldName(), item.getContentType(), item.isFormField(), fileName); // call to b
                items.add(fileItem);
                ...
            }
            ...
    }
}
