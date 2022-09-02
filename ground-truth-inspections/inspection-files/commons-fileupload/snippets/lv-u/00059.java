public class DiskFileItem {

    @Override
    public OutputStream getOutputStream() // this is method a
        throws IOException {
        if (dfos == null) {
            File outputFile = getTempFile();
            // the next line is the only location where dfos is initialized
            dfos = new DeferredFileOutputStream(sizeThreshold, outputFile);
        }
        return dfos;
    }

    @Override
    public boolean isInMemory() {
        if (cachedContent != null) {
            return true;
        }
        return dfos.isInMemory();
    }

    @Override
    public void delete() {
        cachedContent = null;
        File outputFile = getStoreLocation(); // calls b
        if (outputFile != null && !isInMemory() && outputFile.exists()) {
            outputFile.delete();
        }
    }

    public File getStoreLocation() {
        if (dfos == null) {
            return null;
        }
        if (isInMemory()) { // b called here
            return null;
        }
        return dfos.getFile();
    }
}

public class SizesTest {

    @Test
    public void testFileSizeLimit()
        throws IOException, FileUploadException {
        // ...
        HttpServletRequest req = new MockHttpServletRequest(
                                                            request.getBytes("US-ASCII"), Constants.CONTENT_TYPE);
        List<FileItem> fileItems = upload.parseRequest(req); // calls a
        assertEquals(1, fileItems.size());
        FileItem item = fileItems.get(0); // calls b
        assertEquals("This is the content of the file\n", new String(item.get()));
        //...
        req = new MockHttpServletRequest(request.getBytes("US-ASCII"), Constants.CONTENT_TYPE);
        try {
            upload.parseRequest(req); // calls a and b
            fail("Expected exception.");
        } catch (FileUploadBase.FileSizeLimitExceededException e) {
            assertEquals(30, e.getPermittedSize());
        }
    }
}

public class FileUploadBase {
    public List<FileItem> parseRequest(RequestContext ctx)
        throws FileUploadException {
        List<FileItem> items = new ArrayList<FileItem>();
        ...
        try {
                FileItem fileItem = fac.createItem(item.getFieldName(), item.getContentType(),item.isFormField(), fileName);
                try {
                    Streams.copy(item.openStream(), fileItem.getOutputStream(), true, buffer); // getOutputStream is a
                }
            }
            ...
            finally {
                if (!successful) {
                    for (FileItem fileItem : items) {
                        try {
                            fileItem.delete(); // calls b in second location for org.apache.commons.fileupload2.SizesTest#testFileSizeLimit
                        } ...
                    }
                }
            }
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

        }, request.length); // calls a 
        ...
        byte[] bytes = item.get(); // calls b
        ...
    }
}
