public class FileSizeLimitExceededException {
    public void setFieldName(String pFieldName) { // definition of a
        fieldName = pFieldName;
    }

    public void setFileName(String pFileName) { // definition of b
        fileName = pFileName;
    }
}

public class FileItemIteratorImpl {
    private boolean findNextItem() throws IOException {
        ...
        for (;;) {
            ...
            if (currentFieldName == null) {
                // We're parsing the outer multipart
                String fieldName = getFieldName(headers);
                if (fieldName != null) {
                    ...
                    String fileName = getFileName(headers);
                    currentItem = new FileItemStreamImpl(fileName,
                                                         fieldName, headers.getHeader(CONTENT_TYPE),
                                                         fileName == null, getContentLength(headers)); // calls a and b
                    currentItem.setHeaders(headers);
                    notifier.noteItem();
                    itemValid = true;
                    return true;
                }
            }
            ...
        }
    }
}

public class FileItemStreamImpl {
    FileItemStreamImpl(String pName, String pFieldName,
                       String pContentType, boolean pFormField,
                       long pContentLength) throws IOException {
        ...
        if (fileSizeMax != -1) {
            istream = new LimitedInputStream(istream, fileSizeMax) {
                    @Override
                    protected void raiseError(long pSizeMax, long pCount)
                        throws IOException {
                        itemStream.close(true);
                        FileSizeLimitExceededException e =
                            new FileSizeLimitExceededException(
                                                               format("The field %s exceeds its maximum permitted size of %s bytes.",
                                                                      fieldName, Long.valueOf(pSizeMax)),
                                                               pCount, pSizeMax);
                        e.setFieldName(fieldName); // call to a
                        e.setFileName(name); // call to b
                        throw new FileUploadIOException(e);
                    }
                };
        }
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
        upload.setFileSizeMax(-1);
        HttpServletRequest req = new MockHttpServletRequest(
                request.getBytes("US-ASCII"), Constants.CONTENT_TYPE);
        List<FileItem> fileItems = upload.parseRequest(req);
        assertEquals(1, fileItems.size());
        FileItem item = fileItems.get(0);
        assertEquals("This is the content of the file\n", new String(item.get()));

        upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setFileSizeMax(40);
        req = new MockHttpServletRequest(request.getBytes("US-ASCII"), Constants.CONTENT_TYPE);
        fileItems = upload.parseRequest(req);
        assertEquals(1, fileItems.size());
        item = fileItems.get(0);
        assertEquals("This is the content of the file\n", new String(item.get()));

        upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setFileSizeMax(30);
        req = new MockHttpServletRequest(request.getBytes("US-ASCII"), Constants.CONTENT_TYPE);
        try {
            upload.parseRequest(req);
            fail("Expected exception.");
        } catch (FileUploadBase.FileSizeLimitExceededException e) {
            assertEquals(30, e.getPermittedSize());
        }
    }
}
