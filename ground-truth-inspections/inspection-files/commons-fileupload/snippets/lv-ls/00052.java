public interface FileItem {
    OutputStream getOutputStream() throws IOException; // a

    void setFormField(boolean state); // b
}

public class DiskFileItem implements FileItem {

    @Override
    public OutputStream getOutputStream()
        throws IOException { // implementation of a
        if (dfos == null) {
            File outputFile = getTempFile();
            dfos = new DeferredFileOutputStream(sizeThreshold, outputFile);
        }
        return dfos;
    }

    @Override
    public void setFormField(boolean state) { // implementation of b
        isFormField = state;
    }

}

public class FileUploadBase {

    @Deprecated
    public List<FileItem> parseRequest(HttpServletRequest req)
    throws FileUploadException { // calls a and b - called from DiskFileUploadTest#testMoveFile
        return parseRequest(new ServletRequestContext(req));
    }

    public List<FileItem> parseRequest(RequestContext ctx)
            throws FileUploadException { // calls a and b - called from above
        ...
        try {
            FileItemIterator iter = getItemIterator(ctx);
            ...
            while (iter.hasNext()) {
                ...
                try {
                    Streams.copy(item.openStream(), fileItem.getOutputStream(), true, buffer); // call to a
                }
                ...
                final FileItemHeaders fih = item.getHeaders(); // call to b
                fileItem.setHeaders(fih);
            }
            successful = true;
            return items;
        }
        ...
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
        ...
   }

}
