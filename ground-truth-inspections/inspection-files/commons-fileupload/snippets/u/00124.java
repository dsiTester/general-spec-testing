public interface FileItemIterator {
    FileItemStream next() throws FileUploadException, IOException; // b
}

public class FileItemIteratorImpl implements FileItemIterator {
    private long getContentLength(FileItemHeaders pHeaders) { // definition of a
        try {
            return Long.parseLong(pHeaders.getHeader(CONTENT_LENGTH));
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public FileItemStream next() throws FileUploadException, IOException { // only implementation of b
        if (eof  ||  (!itemValid && !hasNext())) {
            throw new NoSuchElementException();
        }
        itemValid = false;
        return currentItem;
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

        }, request.length); // a and b maybe called here?
        Iterator<FileItem> fileIter = fileItems.iterator(); // this iterator is unrelated
        assertTrue(fileIter.hasNext());
        FileItem item = fileIter.next();
        assertEquals("field", item.getFieldName());
        byte[] bytes = item.get();
        assertEquals(3, bytes.length);
        assertEquals((byte)'1', bytes[0]);
        assertEquals((byte)'2', bytes[1]);
        assertEquals((byte)'3', bytes[2]);
        assertTrue(!fileIter.hasNext());
    }

}
