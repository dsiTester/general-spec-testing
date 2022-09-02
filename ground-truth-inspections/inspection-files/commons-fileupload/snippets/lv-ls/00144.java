public class FileUploadBase {
    // NOTE: Insert the following roundabout way of retrieving an input value for method-a to test that method-a can be called after method-b
    // public String m; // since FileItemIteratorImpl is defined inside of FileUploadBase, this works

    protected String getFieldName(FileItemHeaders headers) { // definition of a
        return getFieldName(headers.getHeader(CONTENT_DISPOSITION));
    }

    private String getFieldName(String pContentDisposition) { // called from a
        String fieldName = null;
        if (pContentDisposition != null
                && pContentDisposition.toLowerCase(Locale.ENGLISH).startsWith(FORM_DATA)) {
            ParameterParser parser = new ParameterParser();
            parser.setLowerCaseNames(true);
            // Parameter parser can handle null input
            Map<String, String> params = parser.parse(pContentDisposition, ';');
            fieldName = params.get("name");
            if (fieldName != null) {
                fieldName = fieldName.trim();
            }
        }
        return fieldName;
    }

    public abstract FileItemFactory getFileItemFactory(); // b

    @Deprecated
    public List<FileItem> parseRequest(HttpServletRequest req)
    throws FileUploadException {  // called by test
        return parseRequest(new ServletRequestContext(req));
    }

    public List<FileItem> parseRequest(RequestContext ctx)
        throws FileUploadException {
        List<FileItem> items = new ArrayList<FileItem>();
        boolean successful = false;
        try {
            FileItemIterator iter = getItemIterator(ctx); // call to a
            FileItemFactory fac = getFileItemFactory(); // call to b
            // NOTE: insert the following to call method-a after method-b
            // System.out.println(getFieldName(getParsedHeaders(m)));
            ...
        }
        ...
    }

    public FileItemIterator getItemIterator(RequestContext ctx)
        throws FileUploadException, IOException {
        try {
            return new FileItemIteratorImpl(ctx); // calls a and b
        } catch (FileUploadIOException e) {
            // unwrap encapsulated SizeException
            throw (FileUploadException) e.getCause();
        }
    }

}

public class FileUpload extends FileUploadBase {
    @Override
    public FileItemFactory getFileItemFactory() { // used implementation of b
        return fileItemFactory;
    }
}

public class FileItemIteratorImpl {
    FileItemIteratorImpl(RequestContext ctx)
        throws FileUploadException, IOException {
        ...
        findNextItem(); // calls a
    }

    private boolean findNextItem() throws IOException {
        ...
        for (;;) {
            ...
            FileItemHeaders headers = getParsedHeaders(multi.readHeaders());
            // NOTE: replace the above with the below commented out lines to experiment
            // m = multi.readHeaders();
            // FileItemHeaders headers = getParsedHeaders(m);
            if (currentFieldName == null) {
                String fieldName = getFieldName(headers); // calls a
                // NOTE: replace the above with the below commented out lines to experiment
                // String fieldName = "field";
                if (fieldName != null) {
                    ...
                }
                    ...
            }
        }
    }

}

public class StreamingTest {
    public void testFILEUPLOAD135()
            throws IOException, FileUploadException { // validated case
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
        Iterator<FileItem> fileIter = fileItems.iterator();
        assertTrue(fileIter.hasNext());
        FileItem item = fileIter.next();
        assertEquals("field", item.getFieldName()); // assertion fails here
        ...
    }
}

public class SizesTest {
    @Test
    public void testFileSizeLimit()
            throws IOException, FileUploadException {
        final String request =
            "-----1234\r\n" +
            "Content-Disposition: form-data; name=\"file\"; filename=\"foo.tab\"\r\n" +
            "Content-Type: text/whatever\r\n" +
            "\r\n" +
            "This is the content of the file\n" +
            "\r\n" +
            "-----1234--\r\n";

        ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
        ...
        HttpServletRequest req = new MockHttpServletRequest(
                request.getBytes("US-ASCII"), Constants.CONTENT_TYPE);
        List<FileItem> fileItems = upload.parseRequest(req); // calls a and b
        assertEquals(1, fileItems.size());
        FileItem item = fileItems.get(0);
        assertEquals("This is the content of the file\n", new String(item.get()));

        upload = new ServletFileUpload(new DiskFileItemFactory());
        ...
        req = new MockHttpServletRequest(request.getBytes("US-ASCII"), Constants.CONTENT_TYPE);
        fileItems = upload.parseRequest(req); // calls a and b
        assertEquals(1, fileItems.size());
        item = fileItems.get(0);
        assertEquals("This is the content of the file\n", new String(item.get()));

        upload = new ServletFileUpload(new DiskFileItemFactory());
        ...
        req = new MockHttpServletRequest(request.getBytes("US-ASCII"), Constants.CONTENT_TYPE);
        try {
            upload.parseRequest(req); // calls a and b
            fail("Expected exception.");
        } catch (FileUploadBase.FileSizeLimitExceededException e) {
            assertEquals(30, e.getPermittedSize());
        }
    }
}
