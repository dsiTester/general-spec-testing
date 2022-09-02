public class DiskFileItem {

    @Override
    public OutputStream getOutputStream() // definition of a
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
    public void write(File file) throws Exception { // definition of b
        if (isInMemory()) {
            ...
        } else { ... }
    }
}

public class FileUploadBase {
    ...
    public List<FileItem> parseRequest(RequestContext ctx)
        throws FileUploadException {
        ...
        FileItem fileItem = fac.createItem(item.getFieldName(), item.getContentType(),
                                           item.isFormField(), fileName);
        items.add(fileItem);
        try {
            Streams.copy(item.openStream(), fileItem.getOutputStream(), true, buffer); // l347
        }
        ...
            }
}

public class DiskFileItemTest {
    @Test
    public void testMoveFile() throws Exception {
        DiskFileUpload myUpload = new DiskFileUpload();
        ...
        final HttpServletRequest request = new MockHttpServletRequest(contentBytes, Constants.CONTENT_TYPE);
        final List<FileItem> items = myUpload.parseRequest(request); // calls a
        ...
        final DiskFileItem dfi = (DiskFileItem) items.get(0);
        final File out = File.createTempFile("install", ".tmp");
        dfi.write(out); // b
    }
}
