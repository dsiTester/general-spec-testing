public class FileUploadBase {

    public List<FileItem> parseRequest(RequestContext ctx)
            throws FileUploadException { // definition of a
        List<FileItem> items = new ArrayList<FileItem>();
        boolean successful = false;
        try {
            FileItemIterator iter = getItemIterator(ctx);
            FileItemFactory fac = getFileItemFactory(); // call to b
            ...
        }
        ...
    }
}

public class FileUpload extends FileUploadBase {
    @Override
    public FileItemFactory getFileItemFactory() { // definition of b
        return fileItemFactory;
    }
}

public class StreamingTest {
    public void testFILEUPLOAD135()
            throws IOException, FileUploadException {
        byte[] request = newShortRequest();
        final ByteArrayInputStream bais = new ByteArrayInputStream(request);
        List<FileItem> fileItems = parseUpload(new InputStream() {
            @Override
            public int read()
            throws IOException
            {
                return bais.read();
            }
            @Override
            public int read(byte b[], int off, int len) throws IOException
            {
                return bais.read(b, off, Math.min(len, 3));
            }

        }, request.length); // calls a and b
        Iterator<FileItem> fileIter = fileItems.iterator(); // NullPointerException here
        assertTrue(fileIter.hasNext());
        ...
    }

    private List<FileItem> parseUpload(InputStream pStream, int pLength)
            throws FileUploadException { // called from above
        String contentType = "multipart/form-data; boundary=---1234";

        FileUploadBase upload = new ServletFileUpload();
        upload.setFileItemFactory(new DiskFileItemFactory());
        HttpServletRequest request = new MockHttpServletRequest(pStream,
                pLength, contentType);

        List<FileItem> fileItems = upload.parseRequest(new ServletRequestContext(request)); // call to a; calls b
        return fileItems;
    }
}
