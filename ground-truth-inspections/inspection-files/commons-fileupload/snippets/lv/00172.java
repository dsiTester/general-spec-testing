public class FileUploadBase {
    /**
     * Reads the next header line.
     * @param headers String with all headers.
     * @param header Map where to store the current header.
     */
    private void parseHeaderLine(FileItemHeadersImpl headers, String header) { // definition of a
        final int colonOffset = header.indexOf(':');
        if (colonOffset == -1) {
            // This header line is malformed, skip it.
            return;
        }
        String headerName = header.substring(0, colonOffset).trim();
        String headerValue =
            header.substring(header.indexOf(':') + 1).trim();
        headers.addHeader(headerName, headerValue);
    }

    private String getFieldName(String pContentDisposition) { // definition of b
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

    protected FileItemHeaders getParsedHeaders(String headerPart) { // calls a
        final int len = headerPart.length();
        FileItemHeadersImpl headers = newFileItemHeaders();
        int start = 0;
        for (;;) {
            int end = parseEndOfLine(headerPart, start);
            if (start == end) {
                break;
            }
            StringBuilder header = new StringBuilder(headerPart.substring(start, end));
            start = end + 2;
            while (start < len) {
                int nonWs = start;
                while (nonWs < len) {
                    char c = headerPart.charAt(nonWs);
                    if (c != ' '  &&  c != '\t') {
                        break;
                    }
                    ++nonWs;
                }
                if (nonWs == start) {
                    break;
                }
                // Continuation line found
                end = parseEndOfLine(headerPart, nonWs);
                header.append(" ").append(headerPart.substring(nonWs, end));
                start = end + 2;
            }
            parseHeaderLine(headers, header.toString()); // call to a
        }
        return headers; // will return a new FileItemHeaders() object if a is not called
    }

    protected String getFieldName(FileItemHeaders headers) { // calls b
        return getFieldName(headers.getHeader(CONTENT_DISPOSITION)); // call to b
    }

    public List<FileItem> parseRequest(RequestContext ctx)
            throws FileUploadException { // called from StreamingTest.parseUpload()
        List<FileItem> items = new ArrayList<FileItem>();
        boolean successful = false;
        try {
            FileItemIterator iter = getItemIterator(ctx); // calls a and b
            ...
        }
        ...
    }

    public FileItemIterator getItemIterator(RequestContext ctx)
    throws FileUploadException, IOException { // called from above
        try {
            return new FileItemIteratorImpl(ctx); // calls a and b
        } catch (FileUploadIOException e) {
            // unwrap encapsulated SizeException
            throw (FileUploadException) e.getCause();
        }
    }
}

private class FileItemIteratorImpl {
    private boolean findNextItem() throws IOException {
        ...
        for (;;) {
            ...
            FileItemHeaders headers = getParsedHeaders(multi.readHeaders()); // calls a
            if (currentFieldName == null) {
                // We're parsing the outer multipart
                String fieldName = getFieldName(headers); // calls b; will return null if a is not called
                if (fieldName != null) { // this branch will not be entered if a is not called
                    String subContentType = headers.getHeader(CONTENT_TYPE);
                    if (subContentType != null
                        &&  subContentType.toLowerCase(Locale.ENGLISH)
                        .startsWith(MULTIPART_MIXED)) {
                        currentFieldName = fieldName;
                        // Multiple files associated with this field name
                        byte[] subBoundary = getBoundary(subContentType);
                        multi.setBoundary(subBoundary);
                        skipPreamble = true;
                        continue;
                    }
                    String fileName = getFileName(headers);
                    currentItem = new FileItemStreamImpl(fileName,
                                                         fieldName, headers.getHeader(CONTENT_TYPE),
                                                         fileName == null, getContentLength(headers));
                    currentItem.setHeaders(headers);
                    notifier.noteItem();
                    itemValid = true;
                    return true;
                }
            }
            ...
    }

    FileItemIteratorImpl(RequestContext ctx)
        throws FileUploadException, IOException { // called from FileUploadBase.getItemIterator, calls findNextItem().
        ...
        findNextItem(); // calls a and b
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
        Iterator<FileItem> fileIter = fileItems.iterator();
        assertTrue(fileIter.hasNext()); // assertion fails here
        ...
    }

    private List<FileItem> parseUpload(InputStream pStream, int pLength)
            throws FileUploadException { // called from above
        String contentType = "multipart/form-data; boundary=---1234";

        FileUploadBase upload = new ServletFileUpload();
        upload.setFileItemFactory(new DiskFileItemFactory());
        HttpServletRequest request = new MockHttpServletRequest(pStream,
                pLength, contentType);

        List<FileItem> fileItems = upload.parseRequest(new ServletRequestContext(request)); // calls a and b
        return fileItems;
    }
}
