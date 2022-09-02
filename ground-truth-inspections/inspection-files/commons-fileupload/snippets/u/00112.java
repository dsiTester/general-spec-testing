public interface FileItemStream {
    InputStream openStream() throws IOException; // a
}

public class FileItemStreamImpl implements FileItemStream {
    @Override
    public InputStream openStream() throws IOException { // only implementation of a
        if (opened) {
            throw new IllegalStateException(
                                            "The stream was already opened.");
        }
        if (((Closeable) stream).isClosed()) {
            throw new FileItemStream.ItemSkippedException();
        }
        return stream;
    }

    @Override
    public FileItemHeaders getHeaders() { // definition of b
        return headers;
    }
}

public class FileUploadBase {
    @Deprecated
    public List<FileItem> parseRequest(HttpServletRequest req)
    throws FileUploadException { // called from DiskFileUploadTest
        return parseRequest(new ServletRequestContext(req)); // calls a and b
    }

    public List<FileItem> parseRequest(RequestContext ctx)
            throws FileUploadException { // calls a and b
        List<FileItem> items = new ArrayList<FileItem>();
        boolean successful = false;
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
                items.add(fileItem);
                try {
                    Streams.copy(item.openStream(), fileItem.getOutputStream(), true, buffer); // call to a, also throws NullPointerException
                }
                ...
                final FileItemHeaders fih = item.getHeaders(); // call to b
                fileItem.setHeaders(fih);
            }
            ...
        }
        ...
    }


    // NOTE: the following does not exist; just a way to show that the spec is spurious.
    public List<FileItem> modifiedParseRequest(RequestContext ctx)
            throws FileUploadException { // calls a and b
        List<FileItem> items = new ArrayList<FileItem>();
        boolean successful = false;
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
                items.add(fileItem);
                try {
                    // NOTE: below is modified code
                    byte [] n = new byte[20];
                    Streams.copy(new ByteArrayInputStream(n), fileItem.getOutputStream(), true, buffer);
                }
                ...
                final FileItemHeaders fih = item.getHeaders(); // call to b
                // NOTE: delayed call to a
                item.openStream();
                fileItem.setHeaders(fih);
            }
            ...
        }
        ...
    }
}

public class Streams {
    public static long copy(InputStream inputStream,
            OutputStream outputStream, boolean closeOutputStream,
            byte[] buffer) // throws NullPointerException
    throws IOException {
        OutputStream out = outputStream;
        InputStream in = inputStream;
        try {
            long total = 0;
            for (;;) {
                int res = in.read(buffer); // NullPointerException here
                ...
            }
        }
}

public class DiskFileUploadTest {
    @Test
    public void testMoveFile() throws Exception {
        DiskFileUpload myUpload = new DiskFileUpload();
        myUpload.setSizeThreshold(0);
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
        final List<FileItem> items = myUpload.parseRequest(request); // calls a and b
        assertNotNull(items);
        assertFalse(items.isEmpty());
        final DiskFileItem dfi = (DiskFileItem) items.get(0);
        final File out = File.createTempFile("install", ".tmp");
        dfi.write(out);
    }
}
