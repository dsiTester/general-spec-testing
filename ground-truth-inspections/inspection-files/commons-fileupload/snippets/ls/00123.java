public class FileItemStreamImpl {
    @Override
    public void setHeaders(FileItemHeaders pHeaders) { // definition of a
        headers = pHeaders;
    }

    void close() throws IOException { // definition of b
        stream.close();
    }
}

public class FileUploadBase {
    public List<FileItem> parseRequest(RequestContext ctx)
            throws FileUploadException {
        List<FileItem> items = new ArrayList<FileItem>();
        boolean successful = false;
        try {
            FileItemIterator iter = getItemIterator(ctx); // calls a
            FileItemFactory fac = getFileItemFactory();
            ...
            while (iter.hasNext()) {
                final FileItemStream item = iter.next();
                // Don't use getName() here to prevent an InvalidFileNameException.
                final String fileName = ((FileItemIteratorImpl.FileItemStreamImpl) item).name;
                FileItem fileItem = fac.createItem(item.getFieldName(), item.getContentType(),
                                                   item.isFormField(), fileName);  // call to b
                items.add(fileItem);
                ...
                // NOTE: would be interesting to see if state restoration occured by this point
                final FileItemHeaders fih = item.getHeaders();
                fileItem.setHeaders(fih);
            }
            successful = true;
            return items;
        }
        ...
    }

    public FileItemIterator getItemIterator(RequestContext ctx)
    throws FileUploadException, IOException {
        try {
            return new FileItemIteratorImpl(ctx); // calls a
        } catch (FileUploadIOException e) {
            // unwrap encapsulated SizeException
            throw (FileUploadException) e.getCause();
        }
    }
}

public class FileItemIteratorImpl {

    @Override
    public FileItemStream next() throws FileUploadException, IOException {
        if (eof  ||  (!itemValid && !hasNext())) { // hasNext calls a
            throw new NoSuchElementException();
        }
        itemValid = false;
        return currentItem;
    }

    @Override
    public boolean hasNext() throws FileUploadException, IOException { // calls a
        ...
        try {
            return findNextItem(); // calls a conditionally
        } catch (FileUploadIOException e) {
            // unwrap encapsulated SizeException
            throw (FileUploadException) e.getCause();
        }
    }

    private boolean findNextItem() throws IOException {
        if (eof) {
            return false;
        }
        if (currentItem != null) {
            currentItem.close(); // call to b
            currentItem = null;
        }
        for (;;) {
            ...
            FileItemHeaders headers = getParsedHeaders(multi.readHeaders());
            if (currentFieldName == null) {
                // We're parsing the outer multipart
                String fieldName = getFieldName(headers);
                if (fieldName != null) {
                    ...
                    currentItem = new FileItemStreamImpl(fileName,
                                                         fieldName, headers.getHeader(CONTENT_TYPE),
                                                         fileName == null, getContentLength(headers));
                    currentItem.setHeaders(headers); // call to a
                    notifier.noteItem();
                    itemValid = true;
                    return true;
                }
            }
            ...
        }
        ...
    }
}


public class ProgressListenerTest {
    @Test
    public void testProgressListener() throws Exception {
        final int NUM_ITEMS = 512;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ...
        MockHttpServletRequest request = new MockHttpServletRequest(contents, Constants.CONTENT_TYPE);
        runTest(NUM_ITEMS, contents.length, request); // calls a and b
        request = new MockHttpServletRequest(contents, Constants.CONTENT_TYPE){
            @Override
            public int getContentLength() {
                return -1;
            }
        };
        runTest(NUM_ITEMS, contents.length, request); // calls a and b
    }

    private void runTest(final int NUM_ITEMS, long pContentLength, MockHttpServletRequest request) throws FileUploadException, IOException {
        ServletFileUpload upload = new ServletFileUpload();
        ProgressListenerImpl listener = new ProgressListenerImpl(pContentLength, NUM_ITEMS);
        upload.setProgressListener(listener);
        FileItemIterator iter = upload.getItemIterator(request);
        for (int i = 0;  i < NUM_ITEMS;  i++) {
            FileItemStream stream = iter.next(); // calls a
            InputStream istream = stream.openStream(); // call to b
            for (int j = 0;  j < 16384+i;  j++) {
                /**
                 * This used to be
                 *     assertEquals((byte) j, (byte) istream.read());
                 * but this seems to trigger a bug in JRockit, so
                 * we express the same like this:
                 */
                byte b1 = (byte) j;
                byte b2 = (byte) istream.read();
                if (b1 != b2) {
                    fail("Expected " + b1 + ", got " + b2);
                }
            }
            assertEquals(-1, istream.read());
            istream.close();
        }
        assertTrue(!iter.hasNext());
        listener.checkFinished();
    }
}

public class ServletFileUpload {
    public FileItemIterator getItemIterator(HttpServletRequest request)
    throws FileUploadException, IOException { // called from ProgressListenerTest.runTest()
        return super.getItemIterator(new ServletRequestContext(request));
    }
}
